package ops {

  import scala.annotation.tailrec
  import Console.RESET
  import ops.Parameters.{CellColors, CellCharacters}

  class printingFunctions {
    // Function to print a row of the matrix
    @tailrec
    private def printRow(Row: List[Int]): Unit = {
      Row match {
        case head :: tail => {
          print(s"${CellColors(Row.head)}${CellCharacters(Row.head)}$RESET ")
          printRow(Row.tail)
        }
        case _ => print("\n")
      }
    }

    // Function to print the matrix
    @tailrec
    private def printMatrix(Matrix: List[List[Int]]): Unit = {
      Matrix match {
        case head :: tail => {
          printRow(Matrix.head)
          printMatrix(Matrix.tail)
        }
        case _ => print("\n")
      }
    }

    def printStage(Matrix: List[List[Int]], score: Int, lives: Int): Unit = {
      printMatrix(Matrix)
      println("Score = " + score + "\nLives = " + lives)
    }
  }
}