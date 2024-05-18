package ops {
  //This class contains the parameters of the project.

  object Parameters {
    // Minimum sizes of the stage matrix
    val MIN_COLUMNS: Int = 10
    val MIN_ROWS: Int = 15

    // Default player name
    val DEFAULT_PLAYER: String = "DEFAULT"

    // Row that contains the shield
    val SHIELD_ROW: Int = 4

    //Radius of the explosion of the destroyer
    val EXPLOSION_RADIUS: Int = 5

    // Milliseconds to wait between each round in automatic mode
    val WAIT_TIME: Int = 1500

    // Probability of generating a shield
    val SHIELD_PROBABILITY: Int = 15

    // Game modes
    object GameModes extends Enumeration {
      val manual: Char = 'M'
      val automatic: Char = 'A'
    }

    // Movement directions
    object Directions extends Enumeration {
      val left: Char = 'A'
      val right: Char = 'D'
    }

    // Cell contents of the stage matrix
    object CellContents extends Enumeration {
      val empty: Int = 0
      val alien: Int = 1
      val cloud: Int = 2
      val cephalopod: Int = 3
      val destroyer: Int = 4
      val cruiser: Int = 5
      val commander: Int = 6
      val shield: Int = 7
      val player: Int = 8
    }

    val CellCharacters: List[Char] = List(
      ' ', // Character associated to the empty cell
      'A', // Character associated to the alien
      'N', // Character associated to the cloud
      'C', // Character associated to the cephalopod
      'D', // Character associated to the destroyer
      'R', // Character associated to the cruiser
      'X', // Character associated to the commander
      'B', // Character associated to the shield
      'W' // Character associated to the player
    )

    val CellColors: List[String] = List(
      "\u001b[38;2;255;255;255m", // The empty character is white
      "\u001b[38;2;48;158;20m", // The alien is green
      "\u001b[38;2;255;154;0m", // The cloud is orange
      "\u001b[38;2;0;0;255m", // The cephalopod is blue
      "\u001b[38;2;252;55;179m", // The destroyer is pink
      "\u001b[38;2;255;255;0m", // The cruiser is yellow
      "\u001b[38;2;255;0;0m", // The commander is red
      "\u001b[38;2;0;0;0;48;2;255;255;255m", // The shield has a black writing and a white background
      "\u001b[38;2;0;188;255m" // The player is azure
    )

    object Scores extends Enumeration {
      val alien_score: Int = 5
      val cloud_score: Int = 25
      val cephalopod_score: Int = 15
      val destroyer_score: Int = 5
      val cruiser_score: Int = 13
      val commander_score: Int = 100
      val commander_lives: Int = 1
    }

    // Boundary values to automatically generate the row of aliens
    object BoundaryValues extends Enumeration {
      val empty_val: Int = 0
      val alien_val: Int = 40 // alien = [1, 40]
      val cloud_val: Int = 65 // cloud = [41, 65]
      val cephalopod_val: Int = 80 // cephalopod = [66, 80]
      val destroyer_val: Int = 85 // destroyer = [81, 85]
      val cruiser_val: Int = 98 // cruiser = [86, 98]
      val commander_val: Int = 100 // commander = [99, 100]
    }

    // Values of the support matrix
    object Conversion_Values extends Enumeration {
      val alien_to_cloud: Int = 1 // Conversion from alien to cloud
      val cloud_to_cephalopod: Int = 2 // Conversion from alien to cephalopod
      val add_cloud: Int = 3 // Add a cloud in the surrounding of the commander
    }

    // Probability of applying the third reconversion
    val ADD_CLOUD_PROBABILITY: Int = 10

    // Directions of the explosion of the cruiser
    object Cruiser_explosion_directions extends Enumeration {
      val row: Int = 0
      val column: Int = 1
    }

    // URL to which send the POST request
    val SERVER_URL= "https://write-izfxzfjmiq-uc.a.run.app"

  }
}