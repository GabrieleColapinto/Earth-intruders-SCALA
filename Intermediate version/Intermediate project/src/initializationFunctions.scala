package ops {

  import ops.Parameters.{CellContents, SHIELD_ROW, SHIELD_PROBABILITY}

  class initializationFunctions {

    private val rand = new scala.util.Random

    // This function initializes rows which are not special
    private def initializeStageRow(numColumns: Int): List[Int] = {
      if (numColumns > 0) {
        CellContents.empty :: initializeStageRow(numColumns - 1)
      } else {
        Nil
      }
    }

    /*
      This function initializes the first row of the matrix.
      It puts the player in the initial player position and the
      empty character in the other cells.
    */
    private def initializeRowZero(numColumns: Int, currColumns: Int, initialPlayerPosition: Int): List[Int] = {
      if (currColumns > 0) {
        // We need to add 1 to insert the player in the right position
        if (currColumns == numColumns - initialPlayerPosition) {
          CellContents.player :: initializeRowZero(numColumns, currColumns - 1, initialPlayerPosition)
        } else {
          CellContents.empty :: initializeRowZero(numColumns, currColumns - 1, initialPlayerPosition)
        }
      } else {
        Nil
      }
    }

    /*
      This function initializes the shield row.
      To avoid generating 4 consecutive shield blocks this
      function uses a counter to count the consecutive
      shield blocks. If the counter reaches 3 the considered
      cell cannot contain a shield and the counter is set to 0.

      If the cell can contain a shield block we generate a random
      number between 1 and 100 and check if it is lesser or equal
      to 15.
    */
    private def initializeShieldRow(numColumns: Int, shieldStreak: Int): List[Int] = {
      if (numColumns > 0) {
        if (shieldStreak == 3) {
          CellContents.empty :: initializeShieldRow(numColumns - 1, shieldStreak = 0)
        } else {
          /*
            The random number generator excludes the upper bound.
            If we want to generate a number between 1 and 100 we
            have to add 1 to the upper bound.
          */
          if (rand.between(1, 101) <= SHIELD_PROBABILITY) {
            CellContents.shield :: initializeShieldRow(numColumns - 1, shieldStreak + 1)
          } else {
            CellContents.empty :: initializeShieldRow(numColumns - 1, shieldStreak = 0)
          }
        }
      } else {
        Nil
      }
    }

    def initializeStageMatrix(numRows: Int, currRows: Int, numColumns: Int, initialPlayerPosition: Int): List[List[Int]] = {
      if (currRows > 0) {
        if (currRows == 1) {
          /*
            The initial number of columns of the matrix is
            initially set to the total number of columns to
            cover the whole row.
          */
          initializeRowZero(numColumns, numColumns, initialPlayerPosition) :: Nil
        } else {
          if (currRows == SHIELD_ROW + 1) {
            // The initial value of shieldStreak is 0
            initializeShieldRow(numColumns, shieldStreak = 0) :: initializeStageMatrix(numRows, currRows - 1, numColumns, initialPlayerPosition)
          } else {
            initializeStageRow(numColumns) :: initializeStageMatrix(numRows, currRows - 1, numColumns, initialPlayerPosition)
          }
        }
      } else {
        Nil
      }
    }
  }
}