package ops {

  import ops.Parameters.SERVER_URL

  class networkFunctions {
    def uploadData(numRows: Int, numColumns: Int, gameMode: Char,
                         gameDuration: Int, gameScore: Int, playerName: String): Unit = {

      // Convert the data to a JSON object
      val json = ujson.Obj(
        "rows" -> numRows,
        "columns" -> numColumns,
        // We have to convert the mode to string or else Firebase interprets it as a number
        "mode" -> gameMode.toString(),
        "duration" -> gameDuration,
        "score" -> gameScore,
        "player" -> playerName
      )

      // Send a POST request to the server with the JSON data
      try {
        val r = requests.post(SERVER_URL, data = json)
        assert(r.statusCode == 200)
        println(r.data)
      } catch {
        case e: Exception => println("There was an error sending the data:\n\t" + e.getMessage)
      }
    }

  }
}