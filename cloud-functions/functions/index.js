'use strict';

var functions = require('firebase-functions');
var admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.annotateImage = functions.database.ref('/motion-logs/{id}')
  .onCreate(event => {
    const original = event.data.val();
    console.log('AnnotatingImage', event.params.id, original);
    const fileName = 'gs://hood-e0e7b.appspot.com' + original.imageRef;
    console.log('Filename:', fileName)
    const request = {
      source: {
        imageUri: fileName
      }
    };

    var topic = "/topics/intruders";

    var payload = {
      data: {
        title: "Intruder Alert!",
        body: "An intruder has been detected",
        imageRef: original.imageRef,
        timestamp: original.timestamp.toString()
      }
    };
    return admin.messaging().sendToTopic(topic, payload)
      .then(function (response) {
        // See the MessagingTopicResponse reference documentation for the
        // contents of response.
        console.log("Successfully sent message:", response);
      })
      .catch(function (error) {
        console.log("Error sending message:", error);
      });
  }
  );