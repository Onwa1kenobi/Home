
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const rp = require('request-promise');
const promisePool = require('es6-promise-pool');
const PromisePool = promisePool.PromisePool;
const secureCompare = require('secure-compare');

// cron.key = 656b6116b438193f32d4bf994b52c942b074b1d7

// Create and Deploy Your First Cloud Functions
// https://firebase.google.com/docs/functions/write-firebase-functions

/**
 * When requested this Function will check for pins that are ON and deduct their respective power loss
 * from the total reference power.
 * The request needs to be authorized by passing a 'key' query parameter in the URL. This key must
 * match a key set as an environment variable using `firebase functions:config:set cron.key="YOUR_KEY"`.
 */
exports.runMap = functions.https.onRequest((req, res) => {
    // URL == https://us-central1-home-b39d9.cloudfunctions.net/runMap?key=656b6116b438193f32d4bf994b52c942b074b1d7
  const key = req.query.key;

  // Exit if the keys don't match
  if (!secureCompare(key, functions.config().cron.key)) {
    console.log('The key provided in the request does not match the key set in the environment. Check that', key,
        'matches the cron.key attribute in `firebase env:get`');
    res.status(403).send('Security key does not match. Make sure your "key" URL query parameter matches the ' +
        'cron.key environment variable.');
    return;
  }

  var data = admin.database().ref('/Julius');

  var ping = true;

  data.once("value", function(dataSnapshot) {
        var aggregatePowerUsage = dataSnapshot.child("aggregatePowerUsage").val();
        var todayPowerUsage = dataSnapshot.child("todayPowerUsage").val();
        var totalReferencePower = dataSnapshot.child("totalReferencePower").val();

        var registeredIDsIndex = dataSnapshot.child("registeredIDs");

        var pin0 = registeredIDsIndex.child("home_id_0").val();
        var pin1 = registeredIDsIndex.child("home_id_1").val();
        var pin2 = registeredIDsIndex.child("home_id_2").val();

        var pin0Rating = registeredIDsIndex.child("home_id_0_rating").val();
        var pin1Rating = registeredIDsIndex.child("home_id_1_rating").val();
        var pin2Rating = registeredIDsIndex.child("home_id_2_rating").val();

        var powerUsed = 0;

        if(pin0) {
            powerUsed += pin0Rating;
        }

        if(pin1) {
            powerUsed += pin1Rating;
        }

        if(pin2) {
            powerUsed += pin2Rating;
        }

        powerUsed = powerUsed / (60 * 1000);

        aggregatePowerUsage += powerUsed;
        todayPowerUsage += powerUsed;
        totalReferencePower -= powerUsed;

        data.update({
            "aggregatePowerUsage": aggregatePowerUsage,
            "todayPowerUsage": todayPowerUsage,
            "totalReferencePower": totalReferencePower
        }, function(error) {
                if (error) {
                    res.send("Data could not be saved. " + error);
                } else {
                    res.send("Data saved successfully. with power used = " + powerUsed);
                }
            }
        );
  }, function(errorObject) {
      console.log("The read failed: " + errorObject.code);
  });
});
