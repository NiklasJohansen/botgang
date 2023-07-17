package data

data class GameState(
    var tickNumber: Long,
    var level: LevelState,
    var bots: List<BotState>,
    var guns: List<GunState>,
    var bullets: List<BulletState>,
    var ammo: List<AmmoState>
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

open class GunState(
    val type: String,
    val id: Long,
    val x: Int,
    val y: Int,
    val ownerId: Long?,
    val bulletCount: Int
)

data class BulletState(
    val id: Long,
    val x: Int,
    val y: Int,
    val xVelocity: Int,
    val yVelocity: Int,
    val ownerId: Long
)

data class AmmoState(
    val id: Long,
    val x: Int,
    val y: Int,
    val amount: Int,
)