'use strict';

var functions = require('firebase-functions');
var admin = require('firebase-admin');
var googleCloud = require('google-cloud');
const storage = googleCloud.storage({
    projectId: 'hood-e0e7b',
    keyFilename: 'serviceAccountKey.json'
})
admin.initializeApp(functions.config().firebase);
const Vision = require('@google-cloud/vision');
const vision = Vision();

exports.annotateImage = functions.database.ref('/motion-logs/{id}')
.onCreate(event => {
  const original = event.data.val();
  console.log('AnnotatingImage', event.params.id, original);
  const fileName = 'gs://hood-e0e7b.appspot.com' + original.imageRef;
  console.log('Filename:' , fileName)
  const request = {
    source: {
      imageUri: fileName
    }
  };
  return vision.faceDetection(request)
    .then((results) => {
      const faces = results[0].faceAnnotations;
      console.log('Faces:');
      faces.forEach((face, i) => {
        console.log(`  Face #${i + 1}:`);
        console.log(`    Joy: ${face.joyLikelihood}`);
        console.log(`    Anger: ${face.angerLikelihood}`);
        console.log(`    Sorrow: ${face.sorrowLikelihood}`);
        console.log(`    Surprise: ${face.surpriseLikelihood}`);
      }); 
      var containsFace = faces.length > 0;
      const updatedLog = { "imageRef" : original.imageRef, "timestamp" : original.timestamp, "containsFace": containsFace}
      console.log("updatedLog:", updatedLog)
      event.data.ref.parent.child(event.params.id).update(updatedLog);
      if (containsFace){
            //send notification of intruder!
            // The topic name can be optionally prefixed with "/topics/".
          var topic = "/topics/intruders";

          var payload = {
            data: {
              title: "Intruder Alert!",
              body: "An intruder has been detected",
              imageRef: original.imageRef,
              timestamp: original.timestamp.toString()
            }
          };
          admin.messaging().sendToTopic(topic, payload)
            .then(function(response) {
              // See the MessagingTopicResponse reference documentation for the
              // contents of response.
              console.log("Successfully sent message:", response);
            })
          .catch(function(error) {
            console.log("Error sending message:", error);
          });
      }
    })
    .catch((err) => {
      console.error('ERROR:', err);
    });
  }
);