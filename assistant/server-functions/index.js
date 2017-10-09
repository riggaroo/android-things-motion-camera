var admin = require("firebase-admin");
var serviceAccount = require("./serviceAccountKey.json");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://motion-sensor-camera.firebaseio.com"
});

exports.remoteControl = function(req, res) {
    let authToken = req.headers.authorization ? req.headers.authorization.split(' ')[1] : null;
    console.log(`/remoteControl called by Bearer ${authToken}`, req.body);
    // Here you can use the given authToken to determine which user is accessing the resource

    let reqdata = req.body;
    if (!reqdata.inputs) { showError(res, "missing inputs"); return; }

    for (let i = 0; i < reqdata.inputs.length; i++) {
        let input = reqdata.inputs[i];
        let intent = input.intent || "";
        switch (intent) {
            case "action.devices.SYNC":
                sync(reqdata, res);
                return;
            case "action.devices.QUERY":
                query(reqdata, res);
                return;
            case "action.devices.EXECUTE":
                execute(reqdata, res);
                return;
        }
    }
    showError(res, "missing intent");
}

function sync(reqdata, res) {
    let deviceProps = {
        requestId: reqdata.requestId,
        payload: {
            devices: [{
                id: "1",
                type: "action.devices.types.SWITCH",
                traits: [
                    "action.devices.traits.OnOff"
                ],
                name: {
                    name: "Motion Alarm"
                },
                willReportState: true
            }]
        }
    };
    res.status(200).json(deviceProps);
}

function query(reqdata, res) {
    getDevicesDataFromFirebase(devices => {
        let deviceStates = {
            requestId: reqdata.requestId,
            payload: {
                devices: {
                    "1": {
                        on: devices.alarm.on,
                        online: true
                    }
                }
            }
        };
        res.status(200).json(deviceStates);
    });
}

function execute(reqdata, res) {
    getDevicesDataFromFirebase(devices => {
        let reqCommands = reqdata.inputs[0].payload.commands
        let respCommands = [];

        for (let i = 0; i < reqCommands.length; i++) {
            let curCommand = reqCommands[i];
            for (let j = 0; j < curCommand.execution.length; j++) {
                let curExec = curCommand.execution[j];

                if (curExec.command === "action.devices.commands.OnOff") {
                    for (let k = 0; k < curCommand.devices.length; k++) {
                        let curDevice = curCommand.devices[k];
                        if (curDevice.id === "1") {
                            devices.alarm.on = curExec.params.on;
                        }
                        respCommands.push({ids: [ curDevice.id ], status: "SUCCESS"});
                    }
                } 
            }
        }

        persistDevicesDataToFirebase(devices);

        let resBody = {
            requestId: reqdata.requestId,
            payload: {
                commands: respCommands
            }
        };
        res.status(200).json(resBody);
    });
}

function showError(res, message) {
    res.status(401).set({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization'
    }).json({error: message});
}

function getDevicesDataFromFirebase(action) {
    admin.database().ref().once("value", snapshot => {
        let devices = snapshot.val();
        action(devices);
    });
}

function persistDevicesDataToFirebase(data) {
    admin.database().ref().set(data);
}
