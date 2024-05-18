package ops {

  import ops.Parameters.{BoundaryValues, CellContents}

  class aliensGenerator {
    private val rand = new scala.util.Random

    /*
		  To generate the aliens we generate a random number and we check
		  in which range it falls.
		  The ranges are defined by the enumeration "boundary_values".
	  */
    private def generateAliens(numColumns: Int): List[Int] = {
      if (numColumns > 0) {
        /*
          The random number generator excludes the upper bound.
          If we want to generate a number between 1 and 100 we
          have to add 1 to the upper bound.
        */
        val random: Int = rand.between(1, 101)
        if (random <= BoundaryValues.alien_val) {
          CellContents.alien :: generateAliens(numColumns - 1)
        } else {
          if (random <= BoundaryValues.cloud_val) {
            CellContents.cloud :: generateAliens(numColumns - 1)
          } else {
            if (random <= BoundaryValues.cephalopod_val) {
              CellContents.cephalopod :: generateAliens(numColumns - 1)
            } else {
              if (random <= BoundaryValues.destroyer_val) {
                CellContents.destroyer :: generateAliens(numColumns - 1)
              } else {
                if (random <= BoundaryValues.cruiser_val) {
                  CellContents.cruiser :: generateAliens(numColumns - 1)
                } else {
                  CellContents.commander :: generateAliens(numColumns - 1)
                }
              }
            }
          }
        }
      } else {
        Nil
      }
    }

    // This function substitutes the first row of the matrix
    def addAliens(Matrix: List[List[Int]], numColumns: Int): List[List[Int]] = {
      generateAliens(numColumns) :: Matrix.tail
    }

  }
}