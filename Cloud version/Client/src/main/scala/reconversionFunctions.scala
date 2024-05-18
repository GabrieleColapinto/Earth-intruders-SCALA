package ops {

  import ops.Parameters.{CellContents, Conversion_Values, ADD_CLOUD_PROBABILITY}

  class reconversionFunctions {

    private val rand = new scala.util.Random

    /*
      To reconvert the ships we need to first detect the ships which can be reconverted.
      Each possible reconversion adds an element to the list of the reconversion of the turn.
      After filling the list, reconversions are applied to the matrix one at a time.
      This means that for each reconversion we generate a new matrix.

      The function to detect the ships which can be reconverted uses a growing integer
      which is used to calculate the coordinates of the considered element.
      If this is among the ones which can be reconverted we check if the reconversion is possible.
      If it is we add an element to the list.
    */
    def detectReconversions(stageMatrix: List[List[Int]], numRows: Int, numColumns: Int, numElement: Int): List[conversion_Element] = {
      if (numElement < numRows * numColumns) {
        val row: Int = numElement / numColumns
        val col: Int = numElement % numColumns
        if (stageMatrix(row)(col) == CellContents.alien) {
          /*
            We check if the first reconversion is possible.

            The first reconversion is possible if:
              A) There is a cell on the left of the one we are considering and it contains an alien
              B) There is a cell on top of the one we are considering and it coins an alien
              C) There is a cell on the right of the one we are considering and it contains an alien
              D) There is a cell on the bottom of the one we are considering and it contains an alien
          */
          val A: Boolean = col - 1 >= 0 && stageMatrix(row)(col - 1) == CellContents.alien
          val B: Boolean = row - 1 >= 0 && stageMatrix(row - 1)(col) == CellContents.alien
          val C: Boolean = col + 1 < numColumns && stageMatrix(row)(col + 1) == CellContents.alien
          val D: Boolean = row + 1 < numRows && stageMatrix(row + 1)(col) == CellContents.alien

          if (A && B && C && D) {
            new conversion_Element(Conversion_Values.alien_to_cloud, row, col) :: detectReconversions(stageMatrix, numRows, numColumns, numElement + 1)
          } else {
            detectReconversions(stageMatrix, numRows, numColumns, numElement + 1)
          }
        } else {
          if (stageMatrix(row)(col) == CellContents.cloud) {
            /*
              We check if the second reconversion is possible.

              The first reconversion is possible if:
                A) There is a cell on the left of the one we are considering and it contains an alien
                B) There is a cell on top of the one we are considering and it coins an alien
                C) There is a cell on the right of the one we are considering and it contains an alien
                D) There is a cell on the bottom of the one we are considering and it contains an alien
            */
            val A: Boolean = col - 1 >= 0 && stageMatrix(row)(col - 1) == CellContents.alien
            val B: Boolean = row - 1 >= 0 && stageMatrix(row - 1)(col) == CellContents.alien
            val C: Boolean = col + 1 < numColumns && stageMatrix(row)(col + 1) == CellContents.alien
            val D: Boolean = row + 1 < numRows && stageMatrix(row + 1)(col) == CellContents.alien

            if (A && B && C && D) {
              new conversion_Element(Conversion_Values.cloud_to_cephalopod, row, col) :: detectReconversions(stageMatrix, numRows, numColumns, numElement + 1)
            } else {
              detectReconversions(stageMatrix, numRows, numColumns, numElement + 1)
            }
          } else {
            if (stageMatrix(row)(col) == CellContents.commander) {
              /*
                We try to perform the third reconversion.
                The third reconversion happens with a rate of 10%.
                In theory, we should check if there are cells available in the surrounding of the commander
                but in practice we do not have to.
                There can be a reconversion before this one that empties a cell. Considering that for now
                we are not changing the matrix but we are just analyzing it we limit ourselves to
                calculate a random number and classify it.
              */
              val random = rand.between(1, 101)
              if (random <= ADD_CLOUD_PROBABILITY) {
                new conversion_Element(Conversion_Values.add_cloud, row, col) :: detectReconversions(stageMatrix, numRows, numColumns, numElement + 1)
              } else {
                detectReconversions(stageMatrix, numRows, numColumns, numElement + 1)
              }
            } else {
              // We skip to the next recursion
              detectReconversions(stageMatrix, numRows, numColumns, numElement + 1)
            }
          }
        }
      } else {
        Nil
      }
    }

    def reconversion_manager(stageMatrix: List[List[Int]], reconversions_List: List[conversion_Element], numRows: Int, numColumns: Int): List[List[Int]] = {
      reconversions_List match {
        case head :: tail =>
          reconversion_manager(applyReconversion(stageMatrix, reconversions_List.head, numRows, currRow = 0, numColumns), reconversions_List.tail, numRows, numColumns)
        case Nil =>
          stageMatrix
      }
    }

    /*
      When we reconvert the stage matrix we need to consider that all the rows up to the one
      right before the one stored in the support element will not be affected. The same goes for the ones
      after the the row below the one referenced. We use a similar reasoning for the columns.
    */
    private def applyReconversion(stageMatrix: List[List[Int]], reconversion_element: conversion_Element, numRows: Int, currRow: Int, numColumns: Int): List[List[Int]] = {
      if (currRow < numRows) {
        if (currRow == reconversion_element.getRow - 1) {
          // We are in the top row of the reconversion
          topRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn = 0) :: applyReconversion(stageMatrix, reconversion_element, numRows, currRow + 1, numColumns)
        } else {
          if (currRow == reconversion_element.getRow) {
            // We are in the central row of the reconversion
            centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn = 0) :: applyReconversion(stageMatrix, reconversion_element, numRows, currRow + 1, numColumns)
          } else {
            if (currRow == reconversion_element.getRow + 1) {
              // We are in the bottom row
              bottomRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn = 0) :: applyReconversion(stageMatrix, reconversion_element, numRows, currRow + 1, numColumns)
            } else {
              /*
                These cells do not get affected by the reconversion.
                We can return the row without changing it.
              */
              stageMatrix(currRow) :: applyReconversion(stageMatrix, reconversion_element, numRows, currRow + 1, numColumns)
            }
          }
        }
      } else {
        Nil
      }
    }

    private def topRowReconversion(stageMatrix: List[List[Int]], reconversion_element: conversion_Element, numColumns: Int, currColumn: Int): List[Int] = {
      if (currColumn < numColumns) {
        if (currColumn == reconversion_element.getColumn) {
          // We have to elaborate the top element according to the type of reconversion
          if (reconversion_element.getValue == Conversion_Values.alien_to_cloud || reconversion_element.getValue == Conversion_Values.cloud_to_cephalopod) {
            // In this case the top element is empty
            CellContents.empty :: topRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
          } else {
            // In this case we apply the third reconversion and we need to check if the cell is empty
            if (stageMatrix(reconversion_element.getRow - 1)(reconversion_element.getColumn) == CellContents.empty) {
              // We add a cloud
              CellContents.cloud :: topRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
            } else {
              // We leave the content as it is
              stageMatrix(reconversion_element.getRow - 1)(reconversion_element.getColumn) :: topRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
            }
          }
        } else {
          stageMatrix(reconversion_element.getRow - 1)(currColumn) :: topRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
        }
      } else {
        Nil
      }
    }

    private def centralRowReconversion(stageMatrix: List[List[Int]], reconversion_element: conversion_Element, numColumns: Int, currColumn: Int): List[Int] = {
      if (currColumn < numColumns) {
        if (currColumn == reconversion_element.getColumn - 1) {
          /*
            We are in the left cell of the reconversion.
            The first two reconversions make this cell empty
            while the third one fills it with a cloud if it was
            formerly empty.
          */
          if (reconversion_element.getValue == Conversion_Values.alien_to_cloud || reconversion_element.getValue == Conversion_Values.cloud_to_cephalopod) {
            CellContents.empty :: centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
          } else {
            if (stageMatrix(reconversion_element.getRow)(currColumn) == CellContents.empty) {
              CellContents.cloud :: centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
            } else {
              // We leave the cell as is
              stageMatrix(reconversion_element.getRow)(currColumn) :: centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
            }
          }
        } else {
          if (currColumn == reconversion_element.getColumn) {
            /*
              We are in the central cell of the reconversion.
              The first two reconversions change the content of the cell while
              the third one leaves it as it is.
            */
            if (reconversion_element.getValue == Conversion_Values.alien_to_cloud) {
              CellContents.cloud :: centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
            } else {
              if (reconversion_element.getValue == Conversion_Values.cloud_to_cephalopod) {
                CellContents.cephalopod :: centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
              } else {
                // In this case we leave the cell as it is
                stageMatrix(reconversion_element.getRow)(currColumn) :: centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
              }
            }
          } else {
            if (currColumn == reconversion_element.getColumn + 1) {
              /*
                We are in the right cell of the reconversion.
                The first two reconversions make this cell empty
                while the third one fills it with a cloud if it was
                formerly empty.
              */
              if (reconversion_element.getValue == Conversion_Values.alien_to_cloud || reconversion_element.getValue == Conversion_Values.cloud_to_cephalopod) {
                CellContents.empty :: centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
              } else {
                if (stageMatrix(reconversion_element.getRow)(currColumn) == CellContents.empty) {
                  CellContents.cloud :: centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
                } else {
                  // We leave the cell as is
                  stageMatrix(reconversion_element.getRow)(currColumn) :: centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
                }
              }
            } else {
              // We are in a cell which does not get affected by the reconversion
              stageMatrix(reconversion_element.getRow)(currColumn) :: centralRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
            }
          }
        }
      } else {
        Nil
      }
    }

    private def bottomRowReconversion(stageMatrix: List[List[Int]], reconversion_element: conversion_Element, numColumns: Int, currColumn: Int): List[Int] = {
      if (currColumn < numColumns) {
        if (currColumn == reconversion_element.getColumn) {
          // We have to elaborate the bottom element according to the type of reconversion
          if (reconversion_element.getValue == Conversion_Values.alien_to_cloud || reconversion_element.getValue == Conversion_Values.cloud_to_cephalopod) {
            // In this case the bottom element is empty
            CellContents.empty :: bottomRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
          } else {
            // In this case we apply the third reconversion and we need to check if the cell is empty
            if (stageMatrix(reconversion_element.getRow + 1)(reconversion_element.getColumn) == CellContents.empty) {
              // We add a cloud
              CellContents.cloud :: bottomRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
            } else {
              // We leave the content as it is
              stageMatrix(reconversion_element.getRow + 1)(reconversion_element.getColumn) :: bottomRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
            }
          }
        } else {
          stageMatrix(reconversion_element.getRow + 1)(currColumn) :: bottomRowReconversion(stageMatrix, reconversion_element, numColumns, currColumn + 1)
        }
      } else {
        Nil
      }
    }

  }
}