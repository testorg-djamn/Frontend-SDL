package at.aau.serg.sdlapp.model.board


object BoardData {

    val board: List<Field> = listOf(
        // x größer ist rechts und y rauf ist kleiner
        Field(index = 1, x = 0.1f, y = 0.64f, nextFields = listOf(1,2,3,4,5,6,7,8,9,20), type = FieldType.STARTNORMAL),
        Field(index = 2, x = 0.145f, y = 0.6205f, nextFields = listOf(2,3,4,5,6,7,8,9,20,21), type = FieldType.ZAHLTAG),
        Field(index = 3, x = 0.18f, y = 0.6f, nextFields = listOf(3), type = FieldType.AKTION),
        Field(index = 4, x = 0.2105f, y = 0.571f, nextFields = listOf(4), type = FieldType.ANLAGE),
        Field(index = 5, x = 0.243f, y = 0.598f, nextFields = listOf(5), type = FieldType.AKTION),
        Field(index = 6, x = 0.278f, y = 0.57f, nextFields = listOf(20), type = FieldType.AKTION),
        Field(index = 7, x = 0.308f, y = 0.548f, nextFields = listOf(7), type = FieldType.AKTION),
        Field(index = 8, x = 0.339f, y = 0.57f, nextFields = listOf(8), type = FieldType.BERUF),
        Field(index = 9, x = 0.3715f, y = 0.599f, nextFields = listOf(9), type = FieldType.ZAHLTAG),

        Field(index = 10, x = 0.1f, y = 0.8f, nextFields = listOf(18), type = FieldType.STARTUNI),
        Field(index = 11, x = 0.145f, y = 0.785f, nextFields = listOf(19), type = FieldType.AKTION),
        Field(index = 12, x = 0.178f, y = 0.806f, nextFields = listOf(6), type = FieldType.FREUND),
        Field(index = 13, x = 0.2102f, y = 0.788f, nextFields = listOf(20), type = FieldType.AKTION),
        Field(index = 14, x = 0.243f, y = 0.7605f, nextFields = listOf(6), type = FieldType.FREUND),
        Field(index = 15, x = 0.275f, y = 0.735f, nextFields = listOf(20), type = FieldType.AKTION),
        Field(index = 16, x = 0.308f, y = 0.712f, nextFields = listOf(20), type = FieldType.AKTION),
        Field(index = 17, x = 0.339f, y = 0.738f, nextFields = listOf(6), type = FieldType.FREUND),
        Field(index = 18, x = 0.372f, y = 0.71f, nextFields = listOf(20), type = FieldType.AKTION),
        Field(index = 19, x = 0.405f, y = 0.68f, nextFields = listOf(5), type = FieldType.EXAMEN),

        Field(index = 20, x = 0.405f, y = 0.618f, nextFields = listOf(10), type = FieldType.AKTION),
        Field(index = 21, x = 0.55f, y = 0.31f, nextFields = listOf(11), type = FieldType.HAUS),
        Field(index = 22, x = 0.60f, y = 0.27f, nextFields = listOf(12), type = FieldType.AKTION),
        Field(index = 23, x = 0.65f, y = 0.23f, nextFields = listOf(13), type = FieldType.ZAHLTAG),
        Field(index = 24, x = 0.70f, y = 0.19f, nextFields = listOf(14), type = FieldType.AKTION),
        Field(index = 25, x = 0.75f, y = 0.15f, nextFields = listOf(15), type = FieldType.FREUND),
        Field(index = 26, x = 0.10f, y = 0.11f, nextFields = listOf(16), type = FieldType.AKTION),
        Field(index = 27, x = 0.85f, y = 0.07f, nextFields = listOf(16), type = FieldType.HEIRAT)


    )
}
