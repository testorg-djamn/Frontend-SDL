package at.aau.serg.sdlapp.model


data class Field(
    val index: Int,
    val x: Float,
    val y: Float,
    val nextFields: List<Int>,
    val type: FieldType = FieldType.AKTION
)
