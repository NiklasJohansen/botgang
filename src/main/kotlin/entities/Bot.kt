package entities

import data.BotState
import data.Client
import data.Command
import data.Command.*
import data.Scores
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Font
import no.njoh.pulseengine.core.asset.types.Texture.Companion.BLANK
import no.njoh.pulseengine.core.graphics.Surface2D
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.scene.interfaces.Updatable
import no.njoh.pulseengine.core.shared.primitives.Color
import no.njoh.pulseengine.core.shared.utils.Extensions.degreesBetween
import no.njoh.pulseengine.core.shared.utils.Logger
import util.*
import kotlin.random.Random

class Bot : SceneEntity(), Updatable, Spatial, Renderable
{
    override var width = 70f
    override var height = 70f
    override var rotation = 0f
    override var x = 0f
    override var y = 0f
    override var z = 0f

    var name = "Unnamed"
    var color = Color.WHITE
    var score = 0
    var kills = 0
    var isAlive = true
    var angle = 0
    var xCell = 0; set (value) { xCellLast = field; field = value; }
    var yCell = 0; set (value) { yCellLast = field; field = value; }
    var xCellLast = 0
    var yCellLast = 0
    var client: Client? = null

    private val touchedPickups = mutableSetOf<Long>()

    fun onServerTick(engine: PulseEngine)
    {
        if (!isAlive) return

        client?.response?.command?.let()
        {
            try { handleCommand(engine, it) }
            catch (e: Exception) {
                Logger.error("$name client sent a bad command: $it")
                e.printStackTrace()
            }
        }

        // Clear command
        client?.response?.command = "IDLE"
    }

    private fun handleCommand(engine: PulseEngine, command: String)
    {
        when (Command.parse(command))
        {
            NAME         -> updateName(command)
            COLOR        -> updateColor(command)
            MOVE_TO      -> moveTo(engine, command)
            MOVE_UP      -> move(engine, 0, -1)
            MOVE_DOWN    -> move(engine, 0, 1)
            MOVE_LEFT    -> move(engine, -1, 0)
            MOVE_RIGHT   -> move(engine, 1, 0)
            ROTATE_UP    -> rotate(90)
            ROTATE_DOWN  -> rotate(270)
            ROTATE_LEFT  -> rotate(180)
            ROTATE_RIGHT -> rotate(0)
            PICK_UP      -> pickUpItem(engine)
            DROP         -> dropItem(engine)
            USE          -> useItem(engine)
            IDLE         -> { }
            else         -> Logger.error("Unknown command: $command")
        }
    }

    private fun updateName(command: String)
    {
        name = command.substringAfter("_").take(10)
    }

    private fun updateColor(command: String)
    {
        val c = java.awt.Color.decode(command.substringAfter("_"))
        color.setFrom(c.red / 255f, c.green / 255f, c.blue / 255f, 1f)
    }

    private fun moveTo(engine: PulseEngine, rawCommand: String)
    {
        val (_,_,x,y) = rawCommand.split("_")
        val xTarget = x.toInt()
        val yTarget = y.toInt()

        if (xTarget == xCell && yTarget == yCell)
            return // Already there

        val level = engine.scene.getActiveLevel() ?: return
        val moves = Pathfinder().getPath(level, xCell, yCell, xTarget, yTarget) ?: return
        val (xCell, yCell) = moves.pop()

        if (level.isWalkable(xCell, yCell) && !level.isOccupied(xCell, yCell))
        {
            val targetAngle = when
            {
                xCell > this.xCell -> 0
                xCell < this.xCell -> 180
                yCell > this.yCell -> 270
                yCell < this.yCell -> 90
                else -> angle
            }

            if (angle == targetAngle)
            {
                this.xCell = xCell
                this.yCell = yCell
                level.setOccupied(xCell, yCell)
            }
            else rotate(targetAngle)
        }
    }

    private fun move(engine: PulseEngine, xDir: Int, yDir: Int)
    {
        val level = engine.scene.getActiveLevel() ?: return
        if (level.isWalkable(xCell + xDir, yCell + yDir) && !level.isOccupied(xCell + xDir, yCell + yDir))
        {
            xCell += xDir
            yCell += yDir
            level.setOccupied(xCell, yCell)
        }
    }

    private fun rotate(angle: Int)
    {
        this.angle = angle
    }

    private fun pickUpItem(engine: PulseEngine)
    {
        val holdingItem = engine.scene.getItemPickedUpBy(id)
        engine.scene.forEachActivePickup()
        {
            if (it.ownerId == INVALID_ID && it !== holdingItem && it.xCell == xCell && it.yCell == yCell)
            {
                if (it.id !in touchedPickups)
                {
                    score += Scores.PICKUP
                    touchedPickups.add(it.id)
                }

                // Assign item to bot
                it.ownerId = this.id

                // Drop item if already holding one
                if (holdingItem != null)
                {
                    holdingItem.ownerId = INVALID_ID
                    holdingItem.xCell = xCell
                    holdingItem.yCell = yCell
                }
                return
            }
        }
    }

    private fun dropItem(engine: PulseEngine)
    {
        val pickup = engine.scene.getItemPickedUpBy(id) ?: return // Not holding an item
        pickup.ownerId = INVALID_ID
        pickup.xCell = xCell
        pickup.yCell = yCell

        // If there is already an item at this position, remove it
        engine.scene.forEachActivePickup()
        {
            if (it.id != pickup.id && it.xCell == xCell && it.yCell == yCell)
            {
                it.ownerId = INVALID_ID
                it.set(DEAD)
            }
        }
    }

    private fun useItem(engine: PulseEngine)
    {
        engine.scene.getItemPickedUpBy(id)?.use(engine)
    }

    override fun onFixedUpdate(engine: PulseEngine)
    {
        val level = engine.scene.getActiveLevel() ?: return
        val (xTarget, yTarget) = level.getWorldPos(xCell, yCell)
        x += (xTarget - x) * 0.3f
        y += (yTarget - y) * 0.3f
        rotation -= rotation.degreesBetween(angle.toFloat()) * 0.3f
    }

    override fun onUpdate(engine: PulseEngine) { }

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        val alpha = if (isAlive) 1f else 0.3f

        drawBody(surface, x, y, width, height, rotation, alpha)

        // Name plate
        val fontSize = height * 0.55f
        val namePlateHeight = y - height * 1.1f
        val namePlateWidth = Font.DEFAULT.getWidth(name, fontSize) + 12f
        surface.setDrawColor(Color(0f, 0f, 0f, 0.2f * alpha))
        surface.drawTexture(BLANK, x, namePlateHeight, namePlateWidth, fontSize * 1.3f, xOrigin = 0.5f, yOrigin = 0.5f, cornerRadius = fontSize * 0.2f)

        // Name
        val font = engine.asset.getOrNull<Font>("font-bold")
        surface.setDrawColor(1f, 1f, 1f, alpha)
        surface.drawText(name, x, namePlateHeight, font = font, fontSize = fontSize, xOrigin = 0.5f, yOrigin = 0.5f)
    }

    fun drawBody(surface: Surface2D, x: Float, y: Float, width: Float, height: Float, rot: Float, alpha: Float)
    {
        // Body
        surface.setDrawColor(color, alpha)
        surface.drawTexture(BLANK, x, y, width, height, rot, xOrigin = 0.5f, yOrigin = 0.5f, cornerRadius = 5f)

        // Eyes
        val wEye = height * 0.25f
        val hEye = height * 0.25f
        surface.setDrawColor(1f, 1f, 1f, 0.9f * alpha)
        surface.drawTexture(BLANK, x, y, wEye, hEye, rot, xOrigin = -0.7f, yOrigin = -0.4f, cornerRadius = 6f)
        surface.drawTexture(BLANK, x, y, wEye, hEye, rot, xOrigin = -0.7f, yOrigin = 1.4f, cornerRadius = 6f)
        surface.setDrawColor(0f, 0f, 0f, alpha)
        surface.drawTexture(BLANK, x, y, wEye * 0.7f, hEye * 0.6f, rot, xOrigin = -1.3f, yOrigin = -1f, cornerRadius = 6f)
        surface.drawTexture(BLANK, x, y, wEye * 0.7f, hEye * 0.6f, rot, xOrigin = -1.3f, yOrigin = 2f, cornerRadius = 6f)
    }

    fun setSpawn(engine: PulseEngine, level: Level)
    {
        val (xCell, yCell) = level.getSpawnPoint(engine)
        val (x,y) = level.getWorldPos(xCell, yCell)
        this.x = x
        this.y = y
        this.xCell = xCell
        this.yCell = yCell
        this.xCellLast = xCell
        this.yCellLast = yCell
        this.angle = when
        {
            xCell < level.width / 2 -> 0
            xCell > level.width / 2 -> 180
            yCell < level.height / 2 -> 270
            yCell > level.height / 2 -> 90
            else -> 0
        }
    }

    fun kill(engine: PulseEngine)
    {
        isAlive = false
        client?.response?.command = IDLE.name
        dropItem(engine)
    }

    fun getState() = BotState(name, id, xCell, yCell, angle, isAlive)

    companion object
    {
        fun nextFreeColor(engine: PulseEngine): Color
        {
            val takenColors = engine.scene.getAllEntitiesOfType<Bot>()?.map { it.color } ?: emptyList()
            return COLORS.find { it !in takenColors } ?: COLORS[Random.nextInt(COLORS.size)]
        }

        private val COLORS = listOf(
            Color(232, 97, 97),
            Color(26, 102, 235),
            Color(232, 97, 221),
            Color(209, 143, 63),
            Color(119, 209, 63),
            Color(99, 67, 161),
            Color(244, 250, 62),
            Color(64, 224, 208)
        )
    }
}