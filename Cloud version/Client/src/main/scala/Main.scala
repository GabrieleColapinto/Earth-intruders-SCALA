import ops.Parameters.{GameModes, WAIT_TIME}
import ops._

import scala.Console.{RED, RESET}

object Main {
  private val printingFun: printingFunctions = new printingFunctions()
  private val initializationFun: initializationFunctions = new initializationFunctions()
  private val reconversionFun: reconversionFunctions = new reconversionFunctions()
  private val aliensGenerator: aliensGenerator = new aliensGenerator()
  private val CE_Fun: collateralEffectsFunctions = new collateralEffectsFunctions()
  private val advanceFun: advanceFunctions = new advanceFunctions()
  private val movementFun: movementFunctions = new movementFunctions()
  private val inputFun: inputFunctions = new inputFunctions()
  private val networkFun: networkFunctions = new networkFunctions()

  def main(args: Array[String]): Unit = {

    val initialScore: Int = 0
    val initialLives: Int = 5

    // Input of the game settings
    println("Insert the game settings:\n")
    val numRows: Int = inputFun.inputRows()
    val numColumns: Int = inputFun.inputColumns()
    val gameMode: Char = inputFun.inputMode()

    // Input the name of the player
    val playerName: String = inputFun.inputName()

    val initialPlayerPosition: Int = java.lang.Math.ceil(numColumns / 2).asInstanceOf[Int]

    val baseStageMatrix = initializationFun.initializeStageMatrix(numRows, numRows, numColumns, initialPlayerPosition)
    val initialStageMatrix = aliensGenerator.addAliens(baseStageMatrix, numColumns)

    print("\n")

    // We start the game
    val startTime: Long = System.currentTimeMillis()
    val gameScore: Int = turnFunction(numRows, numColumns, gameMode, initialStageMatrix, initialScore, initialLives)
    val endTime: Long = System.currentTimeMillis()
    val gameDuration: Int = ((endTime - startTime) / 1000).asInstanceOf[Int]

    // When we end the game we upload the data to the database
    networkFun.uploadData(numRows, numColumns, gameMode, gameDuration, gameScore, playerName)
  }

  /*
    In the turn function we perform all the operation we would normally perform in a while loop.
    Considering that we cannot update the stage matrix, the score and the lives we create new constant
    variables to store the new values. For the stage matrix we create a new variable every time and we
    use this as the input of the following step.

    As for the damage check, we cannot update the number of lives inside a branch of an if statement using
    a constant variable and be able to use it later so we perform the damage check in the end.
    If the player has received damage we call the recursive function removing 1 life from the player.

    In the cloud version of the project we need the function to return the final score of the game.
    We change the return type of the function to Int and return the score of the game in the base case.
  */
  private def turnFunction(numRows: Int, numColumns: Int, gameMode: Char, stageMatrix: List[List[Int]], score: Int, lives: Int): Int = {
    printingFun.printStage(stageMatrix, score, lives)
    if (lives > 0) {
      val playerColumn: Int = movementFun.findPlayer(stageMatrix, numRows, numColumns, currColumn = 0)
      try {
        val direction: Char = inputFun.getDirection(stageMatrix, gameMode, numRows, numColumns, playerColumn)

        // Movement of the player
        val movementDamage: Boolean = movementFun.computeMovementDamage(stageMatrix, numRows, direction, playerColumn)
        val stageMatrix_1 = movementFun.playerMovement(stageMatrix, playerColumn, numColumns, direction)

        // Reconversions
        val reconversions_List: List[conversion_Element] = reconversionFun.detectReconversions(stageMatrix_1, numRows, numColumns, numElement = 0)
        val stageMatrix_2 = reconversionFun.reconversion_manager(stageMatrix_1, reconversions_List, numRows, numColumns)

        // We detect the collateral effects before the advancement of the ships
        val CE_List: List[CE_object] = CE_Fun.detectCollateralEffects(stageMatrix_2, numColumns, numRows, numElement = 0)

        // Advancement
        val turnScore: Int = advanceFun.computeTurnScore(stageMatrix_2, numRows, currColumn = 0, numColumns)
        val turnLives: Int = advanceFun.computeTurnLives(stageMatrix_2, numRows, currColumn = 0, numColumns)
        val advanceDamage: Boolean = advanceFun.computeAdvanceDamage(stageMatrix_2, numRows, currColumn = 0, numColumns)
        val stageMatrix_3 = advanceFun.advance(stageMatrix_2, numRows, currRow = 0, numColumns)
        val newScore = score + turnScore
        val newLives = lives + turnLives

        // Collateral effects
        val collateralDamage: Boolean = CE_Fun.computeCollateralDamage(stageMatrix_3, CE_List, numRows, numColumns)
        val stageMatrix_4 = CE_Fun.CE_activator(stageMatrix_3, CE_List, numRows, numColumns)

        // Generation of the aliens
        val stageMatrix_final = aliensGenerator.addAliens(stageMatrix_4, numColumns)

        // If the game runs in automatic mode we wait before refreshing the screen
        if (gameMode == GameModes.automatic) {
          Thread.sleep(WAIT_TIME)
        }

        // Damage check
        if (movementDamage || advanceDamage || collateralDamage) {
          turnFunction(numRows, numColumns, gameMode, stageMatrix_final, newScore, newLives - 1)
        } else {
          turnFunction(numRows, numColumns, gameMode, stageMatrix_final, newScore, newLives)
        }
      } catch {
        case e: Exception =>
          println(s"$RED${e.getMessage}$RESET")
          // In case of wrong input we use the same function without changing values
          turnFunction(numRows, numColumns, gameMode, stageMatrix, score, lives)
      }
    } else {
      // Base case
      println("\nGAME OVER\n")
      score
    }
  }

}