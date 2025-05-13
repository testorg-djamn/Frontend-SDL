package at.aau.serg.sdlapp.model.board


object BoardData {

    val board: List<Field> = listOf(
        // x größer ist rechts und y rauf ist kleiner
        Field(index = 1, x = 0.115f, y = 0.65f, nextFields = listOf(1), type = FieldType.STARTNORMAL),
        Field(index = 2, x = 0.15f, y = 0.617f, nextFields = listOf(2), type = FieldType.ZAHLTAG),
        Field(index = 3, x = 0.184f, y = 0.6f, nextFields = listOf(3), type = FieldType.AKTION),
        Field(index = 4, x = 0.21f, y = 0.58f, nextFields = listOf(4), type = FieldType.ANLAGE),
        Field(index = 5, x = 0.25f, y = 0.55f, nextFields = listOf(5), type = FieldType.AKTION),
        Field(index = 6, x = 0.30f, y = 0.51f, nextFields = listOf(6), type = FieldType.FREUND),
        Field(index = 7, x = 0.35f, y = 0.47f, nextFields = listOf(7), type = FieldType.AKTION),
        Field(index = 8, x = 0.40f, y = 0.43f, nextFields = listOf(8), type = FieldType.BERUF),
        Field(index = 9, x = 0.45f, y = 0.39f, nextFields = listOf(9), type = FieldType.ZAHLTAG),
        Field(index = 10, x = 0.50f, y = 0.35f, nextFields = listOf(10), type = FieldType.AKTION),
        Field(index = 11, x = 0.55f, y = 0.31f, nextFields = listOf(11), type = FieldType.HAUS),
        Field(index = 12, x = 0.60f, y = 0.27f, nextFields = listOf(12), type = FieldType.AKTION),
        Field(index = 13, x = 0.65f, y = 0.23f, nextFields = listOf(13), type = FieldType.ZAHLTAG),
        Field(index = 14, x = 0.70f, y = 0.19f, nextFields = listOf(14), type = FieldType.AKTION),
        Field(index = 15, x = 0.75f, y = 0.15f, nextFields = listOf(15), type = FieldType.FREUND),
        Field(index = 16, x = 0.10f, y = 0.11f, nextFields = listOf(16), type = FieldType.AKTION),
        Field(index = 17, x = 0.85f, y = 0.07f, nextFields = listOf(16), type = FieldType.HEIRAT),
        
        // Uni-Start-Feld
        Field(index = 18, x = 0.115f, y = 0.75f, nextFields = listOf(18), type = FieldType.STARTUNI),
        Field(index = 19, x = 0.30f, y = 0.72f, nextFields = listOf(19), type = FieldType.ZAHLTAG),
        Field(index = 20, x = 0.35f, y = 0.70f, nextFields = listOf(5), type = FieldType.EXAMEN)

    )
}
