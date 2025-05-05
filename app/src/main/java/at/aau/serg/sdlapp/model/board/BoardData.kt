package at.aau.serg.sdlapp.model.board


object BoardData {

    val board: List<Field> = listOf(
        // x größer ist rechts und y rauf ist kleiner
        Field(index = 0, x = 0.115f, y = 0.65f, nextFields = listOf(1,2,3,4,5,6,7,8,9,10), type = FieldType.STARTNORMAL),
        Field(index = 1, x = 0.15f, y = 0.617f, nextFields = listOf(2,3,4,5,6,7,8,9,10,11), type = FieldType.ZAHLTAG),
        Field(index = 2, x = 0.184f, y = 0.6f, nextFields = listOf(3,4,5,6,7,8,9,10,11,12), type = FieldType.AKTION),
        Field(index = 3, x = 0.2f, y = 0.58f, nextFields = listOf(4,5,6,7,8,9,10,11,12,13), type = FieldType.ANLAGE),
        Field(index = 4, x = 0.05f, y = 0.20f, nextFields = listOf(5,6,7,8,9,10,11,12,13,14), type = FieldType.AKTION),
        Field(index = 5, x = 0.05f, y = 0.20f, nextFields = listOf(6,7,8,9,10,11,12,13,14,15), type = FieldType.FREUND),
        Field(index = 6, x = 0.05f, y = 0.20f, nextFields = listOf(7,8,9,10,11,12,13,14,15,16), type = FieldType.AKTION),
        Field(index = 7, x = 0.05f, y = 0.20f, nextFields = listOf(8,9,10,11,12,13,14,15,16), type = FieldType.BERUF),
        Field(index = 8, x = 0.165f, y = 0.295f, nextFields = listOf(9,10,11,12,13,14,15,16), type = FieldType.ZAHLTAG),
        Field(index = 9, x = 0.405f, y = 0.62f, nextFields = listOf(10,11,12,13,14,15,16), type = FieldType.AKTION),
        Field(index = 10, x = 0.05f, y = 1.20f, nextFields = listOf(11,12,13,14,15,16), type = FieldType.HAUS),
        Field(index = 11, x = 0.05f, y = 0.20f, nextFields = listOf(12,13,14,15,16), type = FieldType.AKTION),
        Field(index = 12, x = 0.05f, y = 0.20f, nextFields = listOf(13,14,15,16), type = FieldType.ZAHLTAG),
        Field(index = 13, x = 0.05f, y = 0.20f, nextFields = listOf(14,15,16), type = FieldType.AKTION),
        Field(index = 14, x = 0.05f, y = 0.20f, nextFields = listOf(15,16), type = FieldType.FREUND),
        Field(index = 15, x = 0.05f, y = 0.20f, nextFields = listOf(16), type = FieldType.AKTION),

        Field(index = 16, x = 0.05f, y = 0.20f, nextFields = listOf(16), type = FieldType.HEIRAT),

    )
}
//