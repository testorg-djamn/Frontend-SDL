package at.aau.serg.sdlapp.model.board


class Board(val fields: List<Field>) {

    private val playerPositions = mutableMapOf<Int, Int>() // playerId → field index

    fun addPlayer(playerId: Int, startFieldIndex: Int) {
        playerPositions[playerId] = startFieldIndex
    }

    fun movePlayer(playerId: Int, steps: Int) {
        var currentField = playerPositions[playerId] ?: 0

        repeat(steps) {
            val field = fields[currentField]
            if (field.nextFields.isEmpty()) {
                // Am Ziel oder Sackgasse
                return
            }
            // Bei mehreren Möglichkeiten stoppen (Player muss dann auswählen)
            if (field.nextFields.size > 1) {
                return
            }
            currentField = field.nextFields.first()
        }

        playerPositions[playerId] = currentField
    }

    fun getPlayerField(playerId: Int): Field {
        val index = playerPositions[playerId] ?: 0
        return fields[index]
    }

    fun manualMoveTo(playerId: Int, nextFieldIndex: Int) {
        // WICHTIG: Absicherung, dass der Move erlaubt ist
        val currentField = fields[playerPositions[playerId] ?: 0]
        if (nextFieldIndex in currentField.nextFields) {
            playerPositions[playerId] = nextFieldIndex
        }
    }
}
