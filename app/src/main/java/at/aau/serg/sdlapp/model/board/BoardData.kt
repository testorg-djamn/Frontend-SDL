package at.aau.serg.sdlapp.model.board


object BoardData {

    val board: List<Field> = listOf(
        // x größer ist rechts und y größer ist runter
        //StartNormal
        Field(index = 1, x = 0.09f, y = 0.63f, nextFields = listOf(34), type = FieldType.STARTNORMAL),
        Field(index = 2, x = 0.145f, y = 0.6205f, nextFields = listOf(3), type = FieldType.ZAHLTAG),
        Field(index = 3, x = 0.18f, y = 0.6f, nextFields = listOf(4), type = FieldType.AKTION),
        Field(index = 4, x = 0.2105f, y = 0.571f, nextFields = listOf(5), type = FieldType.ANLAGE),
        Field(index = 5, x = 0.243f, y = 0.598f, nextFields = listOf(6), type = FieldType.AKTION),
        Field(index = 6, x = 0.275f, y = 0.571f, nextFields = listOf(7), type = FieldType.FREUND),
        Field(index = 7, x = 0.308f, y = 0.548f, nextFields = listOf(8), type = FieldType.AKTION),
        Field(index = 8, x = 0.339f, y = 0.57f, nextFields = listOf(9), type = FieldType.BERUF),
        Field(index = 9, x = 0.3715f, y = 0.599f, nextFields = listOf(20), type = FieldType.AKTION),

// x größer ist rechts und y größer ist runter
        //StartUni
        Field(index = 10, x = 0.09f, y = 0.795f, nextFields = listOf(11), type = FieldType.STARTUNI),
        Field(index = 11, x = 0.145f, y = 0.785f, nextFields = listOf(12), type = FieldType.AKTION),
        Field(index = 12, x = 0.178f, y = 0.806f, nextFields = listOf(13), type = FieldType.FREUND),
        Field(index = 13, x = 0.2102f, y = 0.788f, nextFields = listOf(14), type = FieldType.AKTION),
        Field(index = 14, x = 0.243f, y = 0.7605f, nextFields = listOf(15), type = FieldType.FREUND),
        Field(index = 15, x = 0.275f, y = 0.735f, nextFields = listOf(16), type = FieldType.AKTION),
        Field(index = 16, x = 0.308f, y = 0.712f, nextFields = listOf(17), type = FieldType.AKTION),
        Field(index = 17, x = 0.339f, y = 0.738f, nextFields = listOf(18), type = FieldType.FREUND),
        Field(index = 18, x = 0.372f, y = 0.71f, nextFields = listOf(19), type = FieldType.AKTION),
        Field(index = 19, x = 0.405f, y = 0.68f, nextFields = listOf(20), type = FieldType.EXAMEN),
// x größer ist rechts und y größer ist runter
        //Gemeinsamer Weg zur Heirat
        Field(index = 20, x = 0.405f, y = 0.62f, nextFields = listOf(21), type = FieldType.ZAHLTAG),
        Field(index = 21, x = 0.435f, y = 0.595f, nextFields = listOf(22), type = FieldType.AKTION),
        Field(index = 22, x = 0.47f, y = 0.62f, nextFields = listOf(23), type = FieldType.HAUS),
        Field(index = 23, x = 0.5f, y = 0.595f, nextFields = listOf(24), type = FieldType.AKTION),
        Field(index = 24, x = 0.5f, y = 0.548f, nextFields = listOf(25), type = FieldType.ZAHLTAG),
        Field(index = 25, x = 0.47f, y = 0.523f, nextFields = listOf(26), type = FieldType.AKTION),
        Field(index = 26, x = 0.435f, y = 0.498f, nextFields = listOf(27), type = FieldType.FREUND),
        Field(index = 27, x = 0.405f, y = 0.473f, nextFields = listOf(28), type = FieldType.AKTION),
        Field(index = 28, x = 0.37f, y = 0.44f, nextFields = listOf(29), type = FieldType.HEIRAT),
// x größer ist rechts und y größer ist runter
        // Heirat Ja
        Field(index = 29, x = 0.37f, y = 0.44f, nextFields = listOf(30), type = FieldType.HEIRAT_JA),
        Field(index = 30, x = 0.335f, y = 0.445f, nextFields = listOf(31), type = FieldType.AKTION),
        Field(index = 31, x = 0.305f, y = 0.465f, nextFields = listOf(32), type = FieldType.AKTION),
        Field(index = 32, x = 0.275f, y = 0.49f, nextFields = listOf(33), type = FieldType.AKTION),
        Field(index = 33, x = 0.245f, y = 0.49f, nextFields = listOf(34), type = FieldType.ZAHLTAG),
        Field(index = 34, x = 0.215f, y = 0.463f, nextFields = listOf(35), type = FieldType.AKTION),
        Field(index = 35, x = 0.18f, y = 0.435f, nextFields = listOf(36), type = FieldType.HAUS),
        Field(index = 36, x = 0.18f, y = 0.385f, nextFields = listOf(37), type = FieldType.AKTION),
        Field(index = 37, x = 0.18f, y = 0.333f, nextFields = listOf(38), type = FieldType.ANLAGE),
// x größer ist rechts und y größer ist runter
        // Heirat Nein
        Field(index = 38, x = 0.405f, y = 0.618f, nextFields = listOf(39), type = FieldType.HEIRAT_NEIN),
        Field(index = 39, x = 0.405f, y = 0.618f, nextFields = listOf(40), type = FieldType.BERUF),
        Field(index = 40, x = 0.405f, y = 0.618f, nextFields = listOf(41), type = FieldType.AKTION),
        Field(index = 41, x = 0.60f, y = 0.27f, nextFields = listOf(42), type = FieldType.ZAHLTAG),
        Field(index = 42, x = 0.65f, y = 0.23f, nextFields = listOf(43), type = FieldType.AKTION),
// x größer ist rechts und y größer ist runter
        // Gemeinsamer Weg zum Kind
        Field(index = 43, x = 0.70f, y = 0.19f, nextFields = listOf(44), type = FieldType.ANLAGE),
        Field(index = 44, x = 0.55f, y = 0.31f, nextFields = listOf(45), type = FieldType.AKTION),
        Field(index = 45, x = 0.10f, y = 0.11f, nextFields = listOf(46), type = FieldType.HAUS),
        Field(index = 46, x = 0.85f, y = 0.07f, nextFields = listOf(47), type = FieldType.AKTION),
// x größer ist rechts und y größer ist runter
        // Kinder Ja
        Field(index = 47, x = 0.405f, y = 0.618f, nextFields = listOf(48), type = FieldType.KINDER_JA),
        Field(index = 48, x = 0.405f, y = 0.618f, nextFields = listOf(49), type = FieldType.AKTION),
        Field(index = 49, x = 0.405f, y = 0.618f, nextFields = listOf(50), type = FieldType.BABY),
        Field(index = 50, x = 0.60f, y = 0.27f, nextFields = listOf(51), type = FieldType.AKTION),
        Field(index = 51, x = 0.65f, y = 0.23f, nextFields = listOf(52), type = FieldType.ZAHLTAG),
        Field(index = 52, x = 0.70f, y = 0.19f, nextFields = listOf(53), type = FieldType.AKTION),
        Field(index = 53, x = 0.55f, y = 0.31f, nextFields = listOf(54), type = FieldType.HAUS),
        Field(index = 54, x = 0.10f, y = 0.11f, nextFields = listOf(55), type = FieldType.AKTION),
        Field(index = 55, x = 0.85f, y = 0.07f, nextFields = listOf(56), type = FieldType.ZWILLINGE),
        Field(index = 56, x = 0.405f, y = 0.618f, nextFields = listOf(57), type = FieldType.AKTION),
        Field(index = 57, x = 0.405f, y = 0.618f, nextFields = listOf(58), type = FieldType.TIER),
        Field(index = 58, x = 0.405f, y = 0.618f, nextFields = listOf(59), type = FieldType.AKTION),
        Field(index = 59, x = 0.405f, y = 0.618f, nextFields = listOf(60), type = FieldType.AKTION),
        Field(index = 60, x = 0.405f, y = 0.618f, nextFields = listOf(61), type = FieldType.BABY),
// x größer ist rechts und y größer ist runter
        // Kinder Nein
        Field(index = 61, x = 0.405f, y = 0.618f, nextFields = listOf(62), type = FieldType.KINDER_NEIN),
        Field(index = 62, x = 0.405f, y = 0.618f, nextFields = listOf(63), type = FieldType.AKTION),
        Field(index = 63, x = 0.405f, y = 0.618f, nextFields = listOf(64), type = FieldType.BERUF),
        Field(index = 64, x = 0.60f, y = 0.27f, nextFields = listOf(65), type = FieldType.AKTION),
        Field(index = 65, x = 0.65f, y = 0.23f, nextFields = listOf(66), type = FieldType.ZAHLTAG),
        Field(index = 66, x = 0.70f, y = 0.19f, nextFields = listOf(67), type = FieldType.AKTION),
        Field(index = 67, x = 0.55f, y = 0.31f, nextFields = listOf(68), type = FieldType.ANLAGE),
        Field(index = 68, x = 0.10f, y = 0.11f, nextFields = listOf(69), type = FieldType.TIER),
        Field(index = 69, x = 0.85f, y = 0.07f, nextFields = listOf(70), type = FieldType.AKTION),
// x größer ist rechts und y größer ist runter
        //Gemeinsamer Weg zur Midlifechrisis
        Field(index = 70, x = 0.405f, y = 0.618f, nextFields = listOf(71), type = FieldType.ZAHLTAG),
        Field(index = 71, x = 0.405f, y = 0.618f, nextFields = listOf(72), type = FieldType.AKTION),
        Field(index = 72, x = 0.405f, y = 0.618f, nextFields = listOf(73), type = FieldType.HAUS),
        Field(index = 73, x = 0.60f, y = 0.27f, nextFields = listOf(74), type = FieldType.AKTION),
        Field(index = 74, x = 0.65f, y = 0.23f, nextFields = listOf(75), type = FieldType.AKTION),
        Field(index = 75, x = 0.70f, y = 0.19f, nextFields = listOf(76), type = FieldType.ZAHLTAG),
        Field(index = 76, x = 0.75f, y = 0.15f, nextFields = listOf(77), type = FieldType.AKTION),
        Field(index = 77, x = 0.10f, y = 0.11f, nextFields = listOf(78), type = FieldType.FREUND),
        Field(index = 78, x = 0.85f, y = 0.07f, nextFields = listOf(79), type = FieldType.AKTION),
        Field(index = 79, x = 0.405f, y = 0.618f, nextFields = listOf(80), type = FieldType.BERUF),
        Field(index = 80, x = 0.405f, y = 0.618f, nextFields = listOf(81), type = FieldType.AKTION),
        Field(index = 81, x = 0.405f, y = 0.618f, nextFields = listOf(82), type = FieldType.ANLAGE),
        Field(index = 82, x = 0.405f, y = 0.618f, nextFields = listOf(83), type = FieldType.MIDLIFECHRISIS),
// x größer ist rechts und y größer ist runter
        // Midlifechrisis rot
        Field(index = 83, x = 0.405f, y = 0.618f, nextFields = listOf(84), type = FieldType.MIDLIFECHRISIS_ROT),
        Field(index = 84, x = 0.405f, y = 0.618f, nextFields = listOf(85), type = FieldType.ANLAGE),
        Field(index = 85, x = 0.405f, y = 0.618f, nextFields = listOf(86), type = FieldType.AKTION),
        Field(index = 86, x = 0.405f, y = 0.618f, nextFields = listOf(87), type = FieldType.BERUF),
        Field(index = 87, x = 0.405f, y = 0.618f, nextFields = listOf(88), type = FieldType.AKTION),
        Field(index = 88, x = 0.405f, y = 0.618f, nextFields = listOf(89), type = FieldType.HAUS),
        Field(index = 89, x = 0.60f, y = 0.27f, nextFields = listOf(90), type = FieldType.AKTION),
        Field(index = 90, x = 0.65f, y = 0.23f, nextFields = listOf(91), type = FieldType.ZAHLTAG),
        Field(index = 91, x = 0.70f, y = 0.19f, nextFields = listOf(92), type = FieldType.AKTION),
        Field(index = 92, x = 0.75f, y = 0.15f, nextFields = listOf(93), type = FieldType.TIER),
        Field(index = 93, x = 0.80f, y = 0.11f, nextFields = listOf(94), type = FieldType.ANLAGE),
        Field(index = 94, x = 0.85f, y = 0.07f, nextFields = listOf(95), type = FieldType.AKTION),
// x größer ist rechts und y größer ist runter
        // Midlifechrisis schwarz
        Field(index = 95, x = 0.405f, y = 0.618f, nextFields = listOf(96), type = FieldType.MIDLIFECHRISIS_SCHWARZ),
        Field(index = 96, x = 0.405f, y = 0.618f, nextFields = listOf(97), type = FieldType.AKTION),
        Field(index = 97, x = 0.405f, y = 0.618f, nextFields = listOf(98), type = FieldType.ZAHLTAG),
        Field(index = 98, x = 0.405f, y = 0.618f, nextFields = listOf(99), type = FieldType.AKTION),
        Field(index = 99, x = 0.405f, y = 0.618f, nextFields = listOf(100), type = FieldType.FREUND),
        Field(index = 100, x = 0.405f, y = 0.618f, nextFields = listOf(101), type = FieldType.AKTION),
// x größer ist rechts und y größer ist runter
        // Gemeinsamer Weg zum Ruhestand
        Field(index = 101, x = 0.405f, y = 0.618f, nextFields = listOf(102), type = FieldType.ZAHLTAG),
        Field(index = 102, x = 0.405f, y = 0.618f, nextFields = listOf(103), type = FieldType.AKTION),
        Field(index = 103, x = 0.405f, y = 0.618f, nextFields = listOf(104), type = FieldType.ANLAGE),
        Field(index = 104, x = 0.405f, y = 0.618f, nextFields = listOf(105), type = FieldType.AKTION),
        Field(index = 105, x = 0.405f, y = 0.618f, nextFields = listOf(106), type = FieldType.HAUS),
        Field(index = 106, x = 0.60f, y = 0.27f, nextFields = listOf(107), type = FieldType.AKTION),
        Field(index = 107, x = 0.65f, y = 0.23f, nextFields = listOf(108), type = FieldType.BERUF),
        Field(index = 108, x = 0.70f, y = 0.19f, nextFields = listOf(109), type = FieldType.ANLAGE),
        Field(index = 109, x = 0.75f, y = 0.15f, nextFields = listOf(110), type = FieldType.ZAHLTAG),
        Field(index = 110, x = 0.80f, y = 0.11f, nextFields = listOf(111), type = FieldType.AKTION),
        Field(index = 111, x = 0.85f, y = 0.07f, nextFields = listOf(112), type = FieldType.FREUND),
        Field(index = 112, x = 0.405f, y = 0.618f, nextFields = listOf(113), type = FieldType.AKTION),
        Field(index = 113, x = 0.405f, y = 0.618f, nextFields = listOf(114), type = FieldType.AKTION),
        Field(index = 114, x = 0.405f, y = 0.618f, nextFields = listOf(115), type = FieldType.FRUEHPENSION),
// x größer ist rechts und y größer ist runter
        // Frühpension Ja
        Field(index = 115, x = 0.405f, y = 0.618f, nextFields = listOf(116), type = FieldType.FRUEHPENSION_JA),
        Field(index = 116, x = 0.405f, y = 0.618f, nextFields = listOf(117), type = FieldType.AKTION),
        Field(index = 117, x = 0.405f, y = 0.618f, nextFields = listOf(118), type = FieldType.ZAHLTAG),
        Field(index = 118, x = 0.405f, y = 0.618f, nextFields = listOf(119), type = FieldType.RUHESTAND),
// x größer ist rechts und y größer ist runter
        // Frühpension Nein
        Field(index = 119, x = 0.405f, y = 0.618f, nextFields = listOf(120), type = FieldType.FRUEHPENSION_NEIN),
        Field(index = 120, x = 0.405f, y = 0.618f, nextFields = listOf(121), type = FieldType.AKTION),
        Field(index = 121, x = 0.405f, y = 0.618f, nextFields = listOf(122), type = FieldType.BERUF),
        Field(index = 122, x = 0.405f, y = 0.618f, nextFields = listOf(123), type = FieldType.ANLAGE),
        Field(index = 123, x = 0.405f, y = 0.618f, nextFields = listOf(124), type = FieldType.TIER),
        Field(index = 124, x = 0.405f, y = 0.618f, nextFields = listOf(125), type = FieldType.AKTION),
        Field(index = 125, x = 0.405f, y = 0.618f, nextFields = listOf(126), type = FieldType.ZAHLTAG),
        Field(index = 126, x = 0.405f, y = 0.618f, nextFields = listOf(127), type = FieldType.AKTION),
        Field(index = 127, x = 0.405f, y = 0.618f, nextFields = listOf(128), type = FieldType.HAUS),
        Field(index = 128, x = 0.60f, y = 0.27f, nextFields = listOf(129), type = FieldType.AKTION),
        Field(index = 129, x = 0.65f, y = 0.23f, nextFields = listOf(130), type = FieldType.TIER),
        Field(index = 130, x = 0.70f, y = 0.19f, nextFields = listOf(131), type = FieldType.AKTION),
        Field(index = 131, x = 0.75f, y = 0.15f, nextFields = listOf(132), type = FieldType.ZAHLTAG),
        Field(index = 132, x = 0.80f, y = 0.11f, nextFields = listOf(133), type = FieldType.AKTION),
        Field(index = 133, x = 0.85f, y = 0.07f, nextFields = listOf(134), type = FieldType.RUHESTAND)



    )
}
