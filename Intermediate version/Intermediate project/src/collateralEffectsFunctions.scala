package ops {

  import ops.Parameters.{CellContents, Cruiser_explosion_directions, EXPLOSION_RADIUS, SHIELD_ROW}

  import scala.annotation.tailrec

  class collateralEffectsFunctions {

    private val rand = new scala.util.Random

    /*
      If we want to respect the constraints of the project we cannot
      implement two functions to distinguish the cases of the shield
      row and the bottom row. In such case we would have to use the
      list concatenation operator (:::) and it is not allowed.
      We have to implement a more complicated function that includes
      both cases.

      We have to scan two rows: the bottom one and the shield one.
      We scan the bottom row first then we pass to the shield one.
      To do so we use a mathematical trick:
      We use an integer that goes from 0 to 2 * numColumns - 1.
      And we use the fact that the integer result of the division
      of this integer by numColumns is either 0 or 1.
    */
    def detectCollateralEffects(stageMatrix: List[List[Int]], numColumns: Int, numRows: Int, numElement: Int): List[CE_object] = {
      if (numElement < 2 * numColumns) {
        val row: Int = numRows - 1 - (numElement / numColumns) * SHIELD_ROW
        val column: Int = numElement % numColumns
        if (row == numRows - SHIELD_ROW - 1) {
          // We are in the shield row
          val topCell = stageMatrix(row - 1)(column)
          val currCell = stageMatrix(row)(column)
          if (topCell == CellContents.destroyer && currCell == CellContents.shield) {
            // We store the collateral effect of the destroyer
            destroyer_CE_object(row, column) :: detectCollateralEffects(stageMatrix, numColumns, numRows, numElement + 1)
          } else {
            if (topCell == CellContents.cruiser && currCell == CellContents.shield) {
              /*
                We store the collateral effect of the cruiser.
                We need to first generate a direction.
              */
              val random: Int = rand.between(1, 101)
              val direction: Int = random % 2
              cruiser_CE_object(row, column, direction) :: detectCollateralEffects(stageMatrix, numColumns, numRows, numElement + 1)
            } else {
              detectCollateralEffects(stageMatrix, numColumns, numRows, numElement + 1)
            }
          }
        } else {
          // We are in the bottom row
          val currCell = stageMatrix(numRows - 1)(column)
          if (currCell == CellContents.destroyer) {
            // We store the collateral effect of the destroyer
            destroyer_CE_object(row, column) :: detectCollateralEffects(stageMatrix, numColumns, numRows, numElement + 1)
          } else {
            if (currCell == CellContents.cruiser) {
              /*
                We store the collateral effect of the cruiser.
                We need to first generate a direction.
              */
              val random: Int = rand.between(1, 101)
              val direction: Int = random % 2
              cruiser_CE_object(row, column, direction) :: detectCollateralEffects(stageMatrix, numColumns, numRows, numElement + 1)
            } else {
              detectCollateralEffects(stageMatrix, numColumns, numRows, numElement + 1)
            }
          }
        }
      } else {
        Nil
      }
    }

    // Functions to apply the list of collateral effects to the stage matrix
    def CE_activator(stageMatrix: List[List[Int]], CE_List: List[CE_object], numRows: Int, numColumns: Int): List[List[Int]] = {
      CE_List match {
        case head :: tail =>
          CE_List.head match {
            case d: destroyer_CE_object =>
              val nextStepStage: List[List[Int]] = destroyerExplosion(stageMatrix, numRows, currRow = 0, numColumns, d.getRow, d.getColumn)
              CE_activator(nextStepStage, CE_List.tail, numRows, numColumns)
            case c: cruiser_CE_object =>
              val nextStepStage: List[List[Int]] = cruiser_CE_Manager(stageMatrix, numRows, numColumns, c.getRow, c.getColumn, c.getDirection)
              CE_activator(nextStepStage, CE_List.tail, numRows, numColumns)
          }
        case _ =>
          stageMatrix
      }
    }

    /*
      Collateral effect of the destroyer

      The collateral effect of the destroyer is applied to the
      stage matrix as a matrix of positions.
      The function takes in input the stage matrix before the
      explosion and returns the stage matrix after the explosion.

      Given the position of the center of the explosion, the cells
      involved in the explosion have the values of the rows and the columns
      in the following ranges:
      row in [explosionRow - EXPLOSION_RADIUS, explosionRow + EXPLOSION_RADIUS]
      column in [explosionColumn - EXPLOSION_RADIUS, explosionColumn + EXPLOSION_RADIUS]
    */
    private def destroyerExplosion(stageMatrix: List[List[Int]], numRows: Int, currRow: Int, numColumns: Int, explosionRow: Int, explosionColumn: Int): List[List[Int]] = {
      if (currRow < numRows) {
        if (currRow >= explosionRow - EXPLOSION_RADIUS && currRow <= explosionRow + EXPLOSION_RADIUS) {
          // The current column is initialized to 0 to cover the whole row
          destroyer_explosion_row(stageMatrix, numColumns, currColumn = 0, explosionColumn, currRow) :: destroyerExplosion(stageMatrix, numRows, currRow + 1, numColumns, explosionRow, explosionColumn)
        } else {
          // If the row is not involved in the explosion we can return the same column
          stageMatrix(currRow) :: destroyerExplosion(stageMatrix, numRows, currRow + 1, numColumns, explosionRow, explosionColumn)
        }
      } else {
        Nil
      }
    }

    /*
      We need to set to empty all the cells involved in the explosion but the one
      containing the player.
    */
    private def destroyer_explosion_row(stageMatrix: List[List[Int]], numColumns: Int, currColumn: Int, explosionColumn: Int, currRow: Int): List[Int] = {
      if (currColumn < numColumns) {
        if (currColumn >= explosionColumn - EXPLOSION_RADIUS && currColumn <= explosionColumn + EXPLOSION_RADIUS) {
          if (stageMatrix(currRow)(currColumn) == CellContents.player) {
            stageMatrix(currRow)(currColumn) :: destroyer_explosion_row(stageMatrix, numColumns, currColumn + 1, explosionColumn, currRow)
          } else {
            // We set the content of the cell to empty
            CellContents.empty :: destroyer_explosion_row(stageMatrix, numColumns, currColumn + 1, explosionColumn, currRow)
          }
        } else {
          stageMatrix(currRow)(currColumn) :: destroyer_explosion_row(stageMatrix, numColumns, currColumn + 1, explosionColumn, currRow)
        }
      } else {
        Nil
      }
    }

    /*
      Collateral effect of the cruiser

      The cruiser can have two possible collateral effects:
      it can either destroy the row or the column.
      For this reason we need functions for both cases and
      we have to decide which one to use by checking the value
      of the direction stored in the object.

      If the explosion is horizontal, the row of the matrix is
      substituted with an empty one.
      In case the explosion is vertical, we have to process all
      the rows of the matrix one by one and set to empty the
      cells in the column of the explosion.
      If the player is involved in the explosion he/she takes
      collateral damage.

      The function takes in input the stage matrix before the
      explosion and returns the stage matrix after the explosion
      inside an advanceReturn object.
    */
    private def cruiser_CE_Manager(stageMatrix: List[List[Int]], numRows: Int, numColumns: Int, explosionRow: Int, explosionColumn: Int, explosionDirection: Int): List[List[Int]] = {
      if (explosionDirection == Cruiser_explosion_directions.row) {
        /*
          We delete the row.
          We initialize currRow to 0 to cover the whole matrix.
        */
        cruiser_explosion_row_manager(stageMatrix, numRows, currRow = 0, numColumns, explosionRow)
      } else {
        /*
          We delete the column.
          We initialize currRow to 0 to cover the whole matrix
        */
        cruiser_explosion_column_manager(stageMatrix, numRows, currRow = 0, numColumns, explosionColumn)
      }
    }

    private def cruiser_explosion_row_manager(stageMatrix: List[List[Int]], numRows: Int, currRow: Int, numColumns: Int, explosionRow: Int): List[List[Int]] = {
      if (currRow < numRows) {
        if (currRow == explosionRow) {
          cruiser_explosion_row(stageMatrix, explosionRow, numColumns, currColumn = 0) :: cruiser_explosion_row_manager(stageMatrix, numRows, currRow + 1, numColumns, explosionRow)
        } else {
          stageMatrix(currRow) :: cruiser_explosion_row_manager(stageMatrix, numRows, currRow + 1, numColumns, explosionRow)
        }
      } else {
        Nil
      }
    }

    private def cruiser_explosion_row(stageMatrix: List[List[Int]], explosionRow: Int, numColumns: Int, currColumn: Int): List[Int] = {
      if (currColumn < numColumns) {
        val cell_content = stageMatrix(explosionRow)(currColumn)
        // We set to empty the cells involved in the explosion but the one containing the player
        if (cell_content == CellContents.player) {
          cell_content :: cruiser_explosion_row(stageMatrix, explosionRow, numColumns, currColumn + 1)
        } else {
          CellContents.empty :: cruiser_explosion_row(stageMatrix, explosionRow, numColumns, currColumn + 1)
        }
      } else {
        Nil
      }
    }

    private def cruiser_explosion_column_manager(stageMatrix: List[List[Int]], numRows: Int, currRow: Int, numColumns: Int, explosionColumn: Int): List[List[Int]] = {
      if (currRow < numRows) {
        // We initialize currColumn to 0 to cover the whole matrix
        cruiser_explosion_column(stageMatrix, currRow, numColumns, currColumn = 0, explosionColumn) :: cruiser_explosion_column_manager(stageMatrix, numRows, currRow + 1, numColumns, explosionColumn)
      } else {
        Nil
      }
    }

    private def cruiser_explosion_column(stageMatrix: List[List[Int]], currRow: Int, numColumns: Int, currColumn: Int, explosionColumn: Int): List[Int] = {
      if (currColumn < numColumns) {
        val cell_content = stageMatrix(currRow)(currColumn)
        if (currColumn == explosionColumn) {
          // We set to empty all the cells involved in the explosion but the one containing the player
          if (cell_content == CellContents.player) {
            cell_content :: cruiser_explosion_column(stageMatrix, currRow, numColumns, currColumn + 1, explosionColumn)
          } else {
            CellContents.empty :: cruiser_explosion_column(stageMatrix, currRow, numColumns, currColumn + 1, explosionColumn)
          }
        } else {
          cell_content :: cruiser_explosion_column(stageMatrix, currRow, numColumns, currColumn + 1, explosionColumn)
        }
      } else {
        Nil
      }
    }

    /*
      Functions to detect if the player receives collateral damage.
      Considering that the player can be involved in more than one explosion and they can receive at most 1 damage,
      the function runs until either the list of collateral effects is empty or the player receives damage.
    */
    def computeCollateralDamage(stageMatrix: List[List[Int]], CE_List: List[CE_object], numRows: Int, numColumns: Int): Boolean = {
      val movementFun: movementFunctions = new movementFunctions
      val playerColumn: Int = movementFun.findPlayer(stageMatrix, numRows, numColumns, currColumn = 0)
      CE_List_manager(CE_List, playerColumn, numRows)
    }

    @tailrec
    private def CE_List_manager(CE_List: List[CE_object], playerColumn: Int, numRows: Int): Boolean = {
      CE_List match {
        case head :: tail =>
          if (checkCollateralDamage(CE_List.head, playerColumn, numRows)) {
            true
          } else {
            CE_List_manager(CE_List.tail, playerColumn, numRows)
          }
        case Nil =>
          // We analyzed all the collateral effects in the list and the player did not receive damage
          false
      }
    }

    private def checkCollateralDamage(CE_Element: CE_object, playerColumn: Int, numRows: Int): Boolean = {
      CE_Element match {
        case d: destroyer_CE_object =>
          /*
            Considering that the explosion either occurs in the bottom row or the shield row, it is reasonable to assume
            that the bottom row is always involved in the explosion so we only need to check the value of the column.
            The player is involved in the explosion if:
            explosionColumn - EXPLOSION_RADIUS <= playerColumn <= explosionColumn + EXPLOSION_RADIUS
          */
          d.getColumn - EXPLOSION_RADIUS <= playerColumn && playerColumn <= d.getColumn + EXPLOSION_RADIUS
        case c: cruiser_CE_object =>
          /*
            In case of cruiser explosion we need to distinguish the cases in which the explosion affects the row or the
            column. The player receives damage if:
            A) The explosion affects the row and its value is numRows - 1 (bottom row).
            B) The explosion affects the column and it is the one containing the player.
          */
          val A: Boolean = c.getDirection == Cruiser_explosion_directions.row && c.getRow == numRows - 1
          val B: Boolean = c.getDirection == Cruiser_explosion_directions.column && c.getColumn == playerColumn
          A || B
      }
    }


  }
}