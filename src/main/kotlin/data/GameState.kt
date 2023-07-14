package data

data class GameState(
    var tickNumber: Long,
    var level: LevelState,
    var bots: List<BotState>,
    var pickups: List<PickupState>,
    var bullets: List<BulletState>
)

data class LevelState(
    val name: String,
    val width: Int,
    val height: Int,
    val cells: Array<IntArray>
)

data class BotState(
    val name: String,
    val id: Long,
    val x: Int,
    val y: Int,
    val angle: Int,
    val isAlive: Boolean
)

open class PickupState(
    val type: String,
    val id: Long,
    val x: Int,
    val y: Int,
    val ownerId: Long?,
)

data class BulletState(
    val id: Long,
    val x: Int,
    val y: Int,
    val xVelocity: Int,
    val yVelocity: Int,
    val ownerId: Long
)