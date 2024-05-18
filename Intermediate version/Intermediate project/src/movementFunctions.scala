package ops {

  import ops.Parameters.{CellContents, Directions}

  class movementFunctions {
    private val rand = new scala.util.Random

    /*
      To move the player we need to first find the position of the
      player in the bottom row of the matrix.
      Considering that the row is known we need to find the column.
    */
    def findPlayer(stageMatrix: List[List[Int]], numRows: Int, numColumns: Int, currColumn: Int): Int = {
      /*
        The player is necessarily in the bottom row so we do not need to consider the case
        in which we do not find it.
      */
      if (stageMatrix(numRows - 1)(currColumn) == CellContents.player) {
        currColumn
      } else {
        findPlayer(stageMatrix, numRows, numColumns, currColumn + 1)
      }
    }

    def validMovement(direction: Char, numColumns: Int, playerColumn: Int): Boolean = {
      /*
        The movement is not allowed if:
        A) The player is in the left corner and tries to go to the left
        B) The player is in the right corner and tries to move right

        By the laws of De Morgan !(A || B) = !A && !B
      */
      val A: Boolean = playerColumn == 0 && direction == Directions.left
      val B: Boolean = playerColumn == numColumns - 1 && direction == Directions.right
      !A && !B
    }

    /*
      Function for the actual movement of the player.
      We copy all the rows until the second last and we change
      the last one to apply the movement.
    */
    def playerMovement(stageMatrix: List[List[Int]], playerColumn: Int, numColumns: Int, direction: Char): List[List[Int]] = {
      stageMatrix match {
        case head :: Nil =>
          // We are in the last row and we need to apply the movement
          applyMovement(stageMatrix.head, playerColumn, numColumns, currColumn = 0, direction) :: Nil
        case _ =>
          stageMatrix.head :: playerMovement(stageMatrix.tail, playerColumn, numColumns, direction)
      }
    }

    private def applyMovement(bottomRow: List[Int], playerColumn: Int, numColumns: Int, currColumn: Int, direction: Char): List[Int] = {
      if (currColumn < numColumns) {
        /*
          If the direction is "left" we need to change the cells indexed by
          playerColumn - 1 and playerColumn.

          If the direction is "right" we need to change the cells indexed by
          playerColumn and playerColumn + 1.
        */
        if (direction == Directions.left) {
          if (currColumn == playerColumn - 1) {
            // We change the content of the cells.
            CellContents.player :: CellContents.empty :: applyMovement(bottomRow, playerColumn, numColumns, currColumn + 2, direction)
          } else {
            // We do not change the content of the cells and we move on
            bottomRow(currColumn) :: applyMovement(bottomRow, playerColumn, numColumns, currColumn + 1, direction)
          }
        } else {
          if (currColumn == playerColumn) {
            // We change the content of the cells.
            CellContents.empty :: CellContents.player :: applyMovement(bottomRow, playerColumn, numColumns, currColumn + 2, direction)
          } else {
            // We do not change the content of the cells and we move on
            bottomRow(currColumn) :: applyMovement(bottomRow, playerColumn, numColumns, currColumn + 1, direction)
          }
        }
      } else {
        Nil
      }
    }

    /*
      Function to choose the direction of the movement.

      First of all, we need to consider that if the player is in a corner of the matrix we need to
      move them towards the center. If they are not in a corner we can choose the direction
      in a way that prevents them from receiving damage.

      To choose the direction for the movement, we need to consider the last two rows of the matrix.
      If we exclude the cell containing the player, the cells in the last row and the second last
      row can either be empty or contain an alien.
      Furthermore, we need to consider that if a player moves on top of an alien they will receive
      movement damage but if they move under an alien they will receive advance damage shortly after.
      So, when deciding where to move it is important to consider the bottom row and the second last
      row as equally important.

      Moving left is safe if in the column at the left of the player both the last cell and the
      second last cell are empty. The same goes for moving right.

      If it is safe to move in both directions or it is not safe to move in both directions we
      move in a random direction.
    */
    def automaticDirection(stageMatrix: List[List[Int]], numRows: Int, numColumns: Int, playerColumn: Int): Char = {
      if (playerColumn == 0) {
        Directions.right
      } else {
        if (playerColumn == numColumns - 1) {
          Directions.left
        } else {
          // We have to decide the direction
          val leftSafe: Boolean = stageMatrix(numRows - 1)(playerColumn - 1) == CellContents.empty && stageMatrix(numRows - 2)(playerColumn - 1) == CellContents.empty
          val rightSafe: Boolean = stageMatrix(numRows - 1)(playerColumn + 1) == CellContents.empty && stageMatrix(numRows - 2)(playerColumn + 1) == CellContents.empty

          // If both directions are not safe or safe we choose the direction randomly
          if (leftSafe == rightSafe) {
            /*
              To generate a random direction we generate a random number between 1 and 100,
              we calculate its carry by 2 and we use the following convention:
                0 = Left
                1 = Right
            */
            val directionNumber: Int = rand.between(1, 101) % 2
            if (directionNumber == 0) {
              Directions.left
            } else {
              Directions.right
            }
          } else {
            if (leftSafe) {
              Directions.left
            } else {
              Directions.right
            }
          }
        }
      }
    }

    def computeMovementDamage(stageMatrix: List[List[Int]], numRows: Int, direction: Char, playerColumn: Int): Boolean = {
      /*
        The player receives damage if:
        A) He/she moves to the left and the cell at his/her left is not empty
        B) He/she moves to the right and the cell at his/her right is not empty
      */
      val A: Boolean = direction == Directions.left && stageMatrix(numRows - 1)(playerColumn - 1) != CellContents.empty
      val B: Boolean = direction == Directions.right && stageMatrix(numRows - 1)(playerColumn + 1) != CellContents.empty
      A || B
    }

  }
}