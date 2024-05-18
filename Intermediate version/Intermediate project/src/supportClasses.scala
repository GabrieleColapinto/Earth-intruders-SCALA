package ops {
  /*
    This object stores the information we need to
    apply the reconversions.
  */
  class conversion_Element(value: Int, row: Int, column: Int) {
    def getValue: Int = {
      value
    }

    def getRow: Int = {
      row
    }

    def getColumn: Int = {
      column
    }
  }

  sealed trait CE_object

  case class destroyer_CE_object(row: Int, column: Int) extends CE_object {
    def getRow: Int = {
      row
    }

    def getColumn: Int = {
      column
    }
  }

  case class cruiser_CE_object(row: Int, column: Int, direction: Int) extends CE_object {
    def getRow: Int = {
      row
    }

    def getColumn: Int = {
      column
    }

    def getDirection: Int = {
      direction
    }
  }

}