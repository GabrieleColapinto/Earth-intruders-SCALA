package ops {

  import ops.Parameters.{CellContents, SHIELD_ROW, Scores}

  class advanceFunctions {

    def advance(stageMatrix: List[List[Int]], numRows: Int, currRow: Int, numColumns: Int): List[List[Int]] = {
      if (currRow == 0) {
        /*
          We generate the top row of the result matrix.
          The top row of the result matrix has to be filled
          with empty cells.
        */
        generateEmptyRow(numColumns) :: advance(stageMatrix, numRows, currRow = 1, numColumns)
      } else {
        if (currRow == numRows - SHIELD_ROW - 1) {
          // The variable currColumns is initialized to 0 to cover the whole row
          generateShieldRow(stageMatrix, numRows, currColumn = 0, numColumns) :: advance(stageMatrix, numRows, currRow + 1, numColumns)
        } else {
          if (currRow == numRows - SHIELD_ROW) {
            /*
              In this case we need to make sure that we do not copy the shield.
              The variable currColumns is initialized to 0 to cover the whole row.
            */
            generateRowBelowShield(stageMatrix, numRows, currColumn = 0, numColumns) :: advance(stageMatrix, numRows, currRow + 1, numColumns)
          } else {
            if (currRow == numRows - 1) {
              // The variable currColumns is initialized to 0 to cover the whole row
              generateBottomRow(stageMatrix, numRows, currColumn = 0, numColumns) :: Nil
            } else {
              // The variable currColumns is initialized to 0 to cover the whole row
              generateGenericalRow(stageMatrix, currRow, currColumn = 0, numColumns) :: advance(stageMatrix, numRows, currRow + 1, numColumns)
            }
          }
        }
      }
    }

    private def generateEmptyRow(numColumns: Int): List[Int] = {
      if (numColumns > 0) {
        CellContents.empty :: generateEmptyRow(numColumns - 1)
      } else {
        Nil
      }
    }

    private def generateShieldRow(stageMatrix: List[List[Int]], numRows: Int, currColumn: Int, numColumns: Int): List[Int] = {
      if (currColumn < numColumns) {
        val topCell = stageMatrix(numRows - SHIELD_ROW - 2)(currColumn)
        val currCell = stageMatrix(numRows - SHIELD_ROW - 1)(currColumn)
        if (currCell == CellContents.shield) {
          // In this case we need to consider that the cruiser and the commander can destroy the shield
          if (topCell == CellContents.cruiser || topCell == CellContents.commander) {
            CellContents.empty :: generateShieldRow(stageMatrix, numRows, currColumn + 1, numColumns)
          } else {
            /*
              In this case the content of the row above the shield does not destroy it.
              The current cell already contains a shield so we can use its value
              for the return argument. There is no need to reference the enumeration.
            */
            currCell :: generateShieldRow(stageMatrix, numRows, currColumn + 1, numColumns)
          }
        } else {
          // In this case we can simply make the content advance
          topCell :: generateShieldRow(stageMatrix, numRows, currColumn + 1, numColumns)
        }
      } else {
        Nil
      }
    }

    private def generateGenericalRow(stageMatrix: List[List[Int]], currRow: Int, currColumn: Int, numColumns: Int): List[Int] = {
      if (currColumn < numColumns) {
        val topCell = stageMatrix(currRow - 1)(currColumn)
        topCell :: generateGenericalRow(stageMatrix, currRow, currColumn + 1, numColumns)
      } else {
        Nil
      }
    }

    private def generateRowBelowShield(stageMatrix: List[List[Int]], numRows: Int, currColumn: Int, numColumns: Int): List[Int] = {
      if (currColumn < numColumns) {
        val topCell = stageMatrix(numRows - SHIELD_ROW - 1)(currColumn)
        if (topCell == CellContents.shield) {
          CellContents.empty :: generateRowBelowShield(stageMatrix, numRows, currColumn + 1, numColumns)
        } else {
          topCell :: generateRowBelowShield(stageMatrix, numRows, currColumn + 1, numColumns)
        }
      } else {
        Nil
      }
    }

    /*
      When we generate the row at the bottom of the matrix we need to check if
      an alien ship collides with the player.
    */
    private def generateBottomRow(stageMatrix: List[List[Int]], numRows: Int, currColumn: Int, numColumns: Int): List[Int] = {
      if (currColumn < numColumns) {
        val currCell = stageMatrix(numRows - 1)(currColumn)
        val topCell = stageMatrix(numRows - 2)(currColumn)
        if (currCell == CellContents.player) {
          /*
            It is reasonable to assume that if the cell on top of the
            player is not empty it contain an alien ship.
          */
          currCell :: generateBottomRow(stageMatrix, numRows, currColumn + 1, numColumns)
        } else {
          topCell :: generateBottomRow(stageMatrix, numRows, currColumn + 1, numColumns)
        }
      } else {
        Nil
      }
    }

    def computeTurnScore(stageMatrix: List[List[Int]], numRows: Int, currColumn: Int, numColumns: Int): Int = {
      if (currColumn < numColumns) {
        val currCell = stageMatrix(numRows - 1)(currColumn)
        if (currCell != CellContents.empty && currCell != CellContents.player) {
          if (currCell == CellContents.alien) {
            Scores.alien_score + computeTurnScore(stageMatrix, numRows, currColumn + 1, numColumns)
          } else {
            if (currCell == CellContents.cloud) {
              Scores.cloud_score + computeTurnScore(stageMatrix, numRows, currColumn + 1, numColumns)
            } else {
              if (currCell == CellContents.cephalopod) {
                Scores.cephalopod_score + computeTurnScore(stageMatrix, numRows, currColumn + 1, numColumns)
              } else {
                if (currCell == CellContents.destroyer) {
                  Scores.destroyer_score + computeTurnScore(stageMatrix, numRows, currColumn + 1, numColumns)
                } else {
                  if (currCell == CellContents.cruiser) {
                    Scores.cruiser_score + computeTurnScore(stageMatrix, numRows, currColumn + 1, numColumns)
                  } else {
                    Scores.commander_score + computeTurnScore(stageMatrix, numRows, currColumn + 1, numColumns)
                  }
                }
              }
            }
          }
        } else {
          computeTurnScore(stageMatrix, numRows, currColumn + 1, numColumns)
        }
      } else {
        // Base case
        0
      }
    }

    def computeTurnLives(stageMatrix: List[List[Int]], numRows: Int, currColumn: Int, numColumns: Int): Int = {
      if (currColumn < numColumns) {
        if (stageMatrix(numRows - 1)(currColumn) == CellContents.commander) {
          Scores.commander_lives + computeTurnLives(stageMatrix, numRows, currColumn + 1, numColumns)
        } else {
          computeTurnLives(stageMatrix, numRows, currColumn + 1, numColumns)
        }
      } else {
        // Base case
        0
      }
    }

    def computeAdvanceDamage(stageMatrix: List[List[Int]], numRows: Int, currColumn: Int, numColumns: Int): Boolean = {
      if (currColumn < numColumns) {
        val currCell = stageMatrix(numRows - 1)(currColumn)
        val topCell = stageMatrix(numRows - 2)(currColumn)
        /*
          It is reasonable to assume that if the cell on top of the
          player is not empty it contain an alien ship.
        */
        if (currCell == CellContents.player && topCell != CellContents.empty) {
          true
        } else {
          computeAdvanceDamage(stageMatrix, numRows, currColumn + 1, numColumns)
        }
      } else {
        /*
          If we could run through the whole row without detecting a collision it meas that the player
          does not receive damage.
        */
        false
      }
    }

  }
}