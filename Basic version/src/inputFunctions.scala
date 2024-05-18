package ops {

  import ops.Parameters.{Directions, GameModes, MIN_COLUMNS, MIN_ROWS}

  class inputFunctions {

    // Input of the rows of the stage matrix
    def inputRows(): Int = {
      print(s"Insert the number of rows of the matrix (min = $MIN_ROWS): ")
      try {
        val numRows = scala.io.StdIn.readInt()
        if (numRows >= MIN_ROWS) {
          numRows
        } else {
          println(s"The minimum number of rows is $MIN_ROWS. The number of rows has been set to this value.")
          MIN_ROWS
        }
      } catch {
        case e: Exception =>
          println(s"The input is invalid. The number of rows has been set to $MIN_ROWS")
          MIN_ROWS
      }
    }

    // Input of the columns of the stage matrix
    def inputColumns(): Int = {
      print(s"Insert the number of columns of the matrix (min = $MIN_COLUMNS): ")
      try {
        val numColumns: Int = scala.io.StdIn.readInt()
        if (numColumns >= MIN_COLUMNS) {
          numColumns
        } else {
          println(s"The minimum number of columns is $MIN_COLUMNS. The number of columns has been set to this value.")
          MIN_COLUMNS
        }
      } catch {
        case e: Exception =>
          println(s"The input is invalid. The number of columns has been set to $MIN_COLUMNS")
          MIN_COLUMNS
      }
    }

    // Input of the game mode
    def inputMode(): Char = {
      print(s"Select the mode of the game. Insert '${GameModes.manual}' or '${GameModes.manual.toLower}' for manual and '${GameModes.automatic}' or '${GameModes.automatic.toLower}' for automatic (default = ${GameModes.automatic}): ")
      try {
        val gameMode: Char = scala.io.StdIn.readChar().toUpper
        if (gameMode != GameModes.manual && gameMode != GameModes.automatic) {
          println("The selected mode is not valid. The mode has been set to automatic by default.")
          GameModes.automatic
        } else {
          gameMode
        }
      } catch {
        case e: Exception =>
          println("The input is invalid. The mode has been set to automatic by default.")
          GameModes.automatic
      }
    }

    // Input of the direction of the movement
    private def inputDirection(): Char = {
      print(s"\rInsert the direction of the movement ('${Directions.left}' or '${Directions.left.toLower}' for left and  '${Directions.right}' or '${Directions.right.toLower}' for right): ")
      try {
        val direction = scala.io.StdIn.readChar().toUpper
        if (direction != Directions.left && direction != Directions.right) {
          println("The inserted direction is not valid.")
          inputDirection()
        } else {
          direction
        }
      } catch {
        case e: Exception =>
          println("The inserted direction is not valid.")
          inputDirection()
      }
    }

    def getDirection(stageMatrix: List[List[Int]], gameMode: Char, numRows: Int, numColumns: Int, playerColumn: Int): Char = {
      val movementFun: movementFunctions = new movementFunctions()
      if(gameMode == GameModes.manual){
        val direction: Char = inputDirection()
        if(movementFun.validMovement(direction, numColumns, playerColumn)){
          // The direction is valid so we can return it
          direction
        } else {
          // The direction is not valid and we return an error message
          throw new Exception("\nTHE INSERTED DIRECTION IS NOT VALID.\n")
        }
      } else {
        movementFun.automaticDirection(numColumns, playerColumn)
      }
    }

  }
}