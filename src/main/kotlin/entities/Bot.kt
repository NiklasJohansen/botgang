package entities

import Pathfinder
import Vector
import core.server.Player
import data.BotState
import data.ClientResponse
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Font
import no.njoh.pulseengine.core.asset.types.Texture
import no.njoh.pulseengine.core.graphics.Surface2D
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.scene.interfaces.Updatable
import no.njoh.pulseengine.core.shared.primitives.Color
import no.njoh.pulseengine.core.shared.utils.Extensions.degreesBetween
import no.njoh.pulseengine.core.shared.utils.Logger
import getItemPickedUpBy
import setDrawColor
import kotlin.random.Random

class Client : Player<ClientResponse>(ClientResponse::class.java)

class Bot : SceneEntity(), Updatable, Spatial, Renderable
{
    override var width = 70f
    override var height = 70f
    override var rotation = 0f
    override var x = 0f
    override var y = 0f
    override var z = 0f

    var name = "Unnamed"
    var color = Color(
        red = 0.5f + 0.5f * Random.nextFloat(),
        green = 0.5f + 0.5f * Random.nextFloat(),
        blue = 0.5f + 0.5f * Random.nextFloat()
    )

    var xCell = 0; set (value) { xCellLast = field; field = value; }
    var yCell = 0; set (value) { yCellLast = field; field = value; }
    var xCellLast = 0
    var yCellLast = 0
    var angle = 0
    var isAlive = true
    var client: Client? = null

    fun onServerTick(engine: PulseEngine)
    {
        if (!isAlive) return

        client?.response?.clientName?.let { name = it.take(10) }
        client?.response?.command?.let()
        {
            try { handleCommand(engine, it) }
            catch (e: Exception) { Logger.error("$name client sent a bad command: $it") }
        }

        // Clear command
        client?.response?.command = "IDLE"
    }

    private fun handleCommand(engine: PulseEngine, command: String)
    {
        when (command)
        {
            "MOVE_UP"      -> move(engine, 0, -1)
            "MOVE_DOWN"    -> move(engine, 0, 1)
            "MOVE_LEFT"    -> move(engine, -1, 0)
            "MOVE_RIGHT"   -> move(engine, 1, 0)
            "ROTATE_UP"    -> angle = 90
            "ROTATE_DOWN"  -> angle = 270
            "ROTATE_LEFT"  -> angle = 180
            "ROTATE_RIGHT" -> angle = 0
            "PICK_UP"      -> pickUpItem(engine)
            "DROP"         -> dropItem(engine)
            "USE"          -> useItem(engine)
            else           -> {
                if (command.startsWith("MOVE_TO"))
                     command.split("_").let { (_,_,x,y) -> moveTo(engine, x.toInt(), y.toInt()) }
            }
        }
    }

    private fun move(engine: PulseEngine, xDir: Int, yDir: Int)
    {
        if (engine.scene.getFirstEntityOfType<Level>()?.isWalkable(xCell + xDir, yCell + yDir) == true)
        {
            xCell += xDir
            yCell += yDir
        }
    }

    private fun pickUpItem(engine: PulseEngine)
    {
        // Drop item if already holding one
        val holdingItem = engine.scene.getItemPickedUpBy(id)
        if (holdingItem != null)
        {
            holdingItem.ownerId = INVALID_ID
            holdingItem.xCell = xCell
            holdingItem.yCell = yCell
        }

        engine.scene.forEachEntityOfType<Pickup>()
        {
            if (it.ownerId == INVALID_ID && it != holdingItem && it.xCell == xCell && it.yCell == yCell)
            {
                it.ownerId = this.id
                return
            }
        }
    }

    private fun dropItem(engine: PulseEngine)
    {
        val pickup = engine.scene.getItemPickedUpBy(id) ?: return // Not holding an item

        engine.scene.forEachEntityOfType<Pickup>()
        {
            if (it.id != pickup.id && it.xCell == xCell && it.yCell == yCell)
                return // Cannot drop item on top of another item // TODO what about disconnect?
        }

        pickup.ownerId = INVALID_ID
        pickup.xCell = xCell
        pickup.yCell = yCell
    }

    private fun useItem(engine: PulseEngine)
    {
        engine.scene.getItemPickedUpBy(id)?.use(engine)
    }

    private fun moveTo(engine: PulseEngine, xTarget: Int, yTarget: Int)
    {
        if (xTarget == xCell && yTarget == yCell)
            return // Already there

        val level = engine.scene.getFirstEntityOfType<Level>() ?: return

        Pathfinder().getPath(level, xCell, yCell, xTarget, yTarget)?.let { moves ->
            val (x, y) = moves.pop()
            this.xCell = x
            this.yCell = y
        }
    }

    override fun onFixedUpdate(engine: PulseEngine)
    {
        val level = engine.scene.getFirstEntityOfType<Level>() ?: return
        val (xTarget, yTarget) = level.getWorldPos(xCell, yCell)
        x += (xTarget - x) * 0.3f
        y += (yTarget - y) * 0.3f
        rotation -= rotation.degreesBetween(angle.toFloat()) * 0.3f
    }

    override fun onUpdate(engine: PulseEngine) { }

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        val alpha = if (isAlive) 1f else 0.3f

        // Body
        surface.setDrawColor(color, alpha)
        surface.drawTexture(Texture.BLANK, x, y, width, height, rotation, xOrigin = 0.5f, yOrigin = 0.5f, cornerRadius = 5f)

        // Eyes
        val wEye = height * 0.25f
        val hEye = height * 0.25f
        surface.setDrawColor(1f, 1f, 1f, 0.9f * alpha)
        surface.drawTexture(Texture.BLANK, x, y, wEye, hEye, rotation, xOrigin = -0.7f, yOrigin = -0.4f, cornerRadius = 6f)
        surface.drawTexture(Texture.BLANK, x, y, wEye, hEye, rotation, xOrigin = -0.7f, yOrigin = 1.4f, cornerRadius = 6f)
        surface.setDrawColor(0f, 0f, 0f, alpha)
        surface.drawTexture(Texture.BLANK, x, y, wEye * 0.7f, hEye * 0.6f, rotation, xOrigin = -1.3f, yOrigin = -1f, cornerRadius = 6f)
        surface.drawTexture(Texture.BLANK, x, y, wEye * 0.7f, hEye * 0.6f, rotation, xOrigin = -1.3f, yOrigin = 2f, cornerRadius = 6f)

        // Name plate
        val fontSize = height * 0.55f
        val namePlateHeight = y - height * 1.1f
        val namePlateWidth = Font.DEFAULT.getWidth(name, fontSize) + 12f
        surface.setDrawColor(Color(0f, 0f, 0f, 0.2f * alpha))
        surface.drawTexture(Texture.BLANK, x, namePlateHeight, namePlateWidth, fontSize * 1.3f, xOrigin = 0.5f, yOrigin = 0.5f, cornerRadius = fontSize * 0.2f)

        // Name
        val font = engine.asset.getOrNull<Font>("font-bold")
        surface.setDrawColor(1f, 1f, 1f, alpha)
        surface.drawText(name, x, namePlateHeight, font = font, fontSize = fontSize, xOrigin = 0.5f, yOrigin = 0.5f)
    }

    fun kill(engine: PulseEngine)
    {
        isAlive = false
        dropItem(engine)
    }

    fun getState() = BotState(name, id, xCell, yCell, angle, isAlive)
}