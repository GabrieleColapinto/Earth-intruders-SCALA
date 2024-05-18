// The Cloud Functions for Firebase SDK to create Cloud Functions and triggers.
const {onRequest} = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

// The Firebase Admin SDK to access Firestore.
const {initializeApp, applicationDefault} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");

initializeApp({
  credential: applicationDefault(),
});

const db = getFirestore();

const COLLECTION_NAME = "gameData";

// We define a path to show the scoreboard
exports.read = onRequest((req, res) => {
  const queryResult = db.collection(COLLECTION_NAME).
      orderBy("time", "desc").limit(10);

  let queryResultTable = ``;
  queryResult.get().then((querySnapshot) => {
    // We generate the result table
    querySnapshot.forEach((document) => {
      const gameDate = new Date(document.data().time._seconds * 1000 +
        document.data().time._nanoseconds / 1000000).
          toLocaleDateString("es-Es");
      const gameTime = new Date(document.data().time._seconds * 1000 +
        document.data().time._nanoseconds / 1000000).
          toLocaleTimeString("es-ES");

      queryResultTable += `<tr><td>${gameDate}</td><td>${gameTime}</td>
      <td>${document.data().rows}</td><td>${document.data().columns}</td>
      <td>${document.data().mode}</td><td>${document.data().player}</td>
      <td>${document.data().duration}</td>
      <td>${document.data().score}</td></tr>`;
    });

    const outputMessage = `
    <html>
      <head>
        <title>Earth intruders</title>
        <style>
          table {
            text-align: center;
            border-collapse: collapse;
          }
          td, th {border: 1px solid black;}
          caption {font-size: 20px}
        </style>
      </head>
      <body>
        <h1>Earth intruders</h1>
        <table>
          <caption>Scoreboard</caption>
          <tr>
            <th>Date</th>
            <th>Time</th>
            <th>Rows</th>
            <th>Columns</th>
            <th>Mode</th>
            <th>Player</th>
            <th>Duration (s)</th>
            <th>Score</th>
          </tr>
          ${queryResultTable}
        </table>
      </body>
    </html>`;

    // We send the response to the user
    res.send(outputMessage);
  });
});

// We define a path to write data into the database
exports.write = onRequest((req, res) => {
  // We retrieve the body of the request
  const data = req.body;

  /*
  The data is in JSON format so we can add the timestamp
  using the dot notation.
  */
  data.time = admin.firestore.FieldValue.serverTimestamp();

  // Push the new message into Firestore using the Firebase Admin SDK.
  db.collection(COLLECTION_NAME).add(data);

  // Send a response code
  res.send("Data inserted correctly!");
});
