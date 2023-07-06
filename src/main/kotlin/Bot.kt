import core.server.Player
import data.Command.*
import data.IncomingPacket
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
import kotlin.random.Random

class Client : Player<IncomingPacket>(IncomingPacket::class.java)

class Bot : SceneEntity(), Updatable, Spatial, Renderable
{
    override var width = 40f
    override var height = 40f
    override var rotation = 0f
    override var x = 0f
    override var y = 0f
    override var z = 0f

    var color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
    var name = "Unnamed"
    var xCell = 0
    var yCell = 0
    var angle = 0

    var client: Client? = null

    fun onServerTick(engine: PulseEngine)
    {
        client?.response?.clientName?.let { name = it.take(10) }

        when (client?.response?.command)
        {
            MOVE_UP      -> move(engine, 0, -1)
            MOVE_DOWN    -> move(engine, 0, 1)
            MOVE_LEFT    -> move(engine, -1, 0)
            MOVE_RIGHT   -> move(engine, 1, 0)
            ROTATE_UP    -> angle = 90
            ROTATE_DOWN  -> angle = 270
            ROTATE_LEFT  -> angle = 180
            ROTATE_RIGHT -> angle = 0
            IDLE, null   -> { }
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

    override fun onFixedUpdate(engine: PulseEngine)
    {
        val level = engine.scene.getFirstEntityOfType<Level>() ?: return
        val xTarget = level.x - (level.width * 0.5f) + level.cellSize * (xCell + 0.5f)
        val yTarget = level.y - (level.height * 0.5f) + level.cellSize * (yCell + 0.5f)

        x += (xTarget - x) * 0.3f
        y += (yTarget - y) * 0.3f
        rotation -= rotation.degreesBetween(angle.toFloat()) * 0.3f
    }

    override fun onUpdate(engine: PulseEngine) { }

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        // Body
        surface.setDrawColor(color)
        surface.drawTexture(Texture.BLANK, x, y, width, height, rotation, xOrigin = 0.5f, yOrigin = 0.5f, cornerRadius = 5f)

        // Eyes
        surface.setDrawColor(Color.WHITE)
        surface.drawTexture(Texture.BLANK, x, y, 10f, 10f, rotation, xOrigin = -0.7f, yOrigin = -0.4f, cornerRadius = 3f)
        surface.drawTexture(Texture.BLANK, x, y, 10f, 10f, rotation, xOrigin = -0.7f, yOrigin = 1.4f, cornerRadius = 3f)
        surface.setDrawColor(Color.BLACK)
        surface.drawTexture(Texture.BLANK, x, y, 7f, 6f, rotation, xOrigin = -1.3f, yOrigin = -1f, cornerRadius = 3f)
        surface.drawTexture(Texture.BLANK, x, y, 7f, 6f, rotation, xOrigin = -1.3f, yOrigin = 2f, cornerRadius = 3f)

        // Name plate
        val namePlateWidth = Font.DEFAULT.getWidth(name, 22f) + 12f
        surface.setDrawColor(Color(0f, 0f, 0f, 0.2f))
        surface.drawTexture(Texture.BLANK, x, y - 40f, namePlateWidth, 30f, xOrigin = 0.5f, yOrigin = 0.5f, cornerRadius = 5f)

        // Name
        val font = engine.asset.getOrNull<Font>("font-bold")
        surface.setDrawColor(Color.WHITE)
        surface.drawText(name, x, y - 40f, font = font, fontSize = 22f, xOrigin = 0.5f, yOrigin = 0.5f)
    }
}