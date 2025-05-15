package at.aau.serg.sdlapp.model.board

/**
 * BoardHelper bietet Hilfsfunktionen für die Arbeit mit dem Spielbrett
 */
object BoardHelper {
    
    /**
     * Findet ein Feld anhand seiner ID
     * 
     * @param fieldId Die ID des zu findenden Felds
     * @return Das gefundene Feld oder null, wenn die ID nicht existiert
     */
    fun getFieldById(fieldId: Int): Field? {
        return BoardData.board.find { it.index == fieldId }
    }
    
    /**
     * Holt die Koordinaten eines Feldes anhand seiner ID
     * 
     * @param fieldId Die ID des Feldes
     * @return Ein Pair mit den x/y-Koordinaten oder null, wenn das Feld nicht gefunden wurde
     */
    fun getFieldCoordinates(fieldId: Int): Pair<Float, Float>? {
        val field = getFieldById(fieldId) ?: return null
        return Pair(field.x, field.y)
    }
}
