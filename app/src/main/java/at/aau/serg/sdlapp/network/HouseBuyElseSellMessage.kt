package at.aau.serg.sdlapp.network

data class HouseBuyElseSellMessage(
    var playerID: String?,
    var gameId: Int,
    var buyElseSell: Boolean
)