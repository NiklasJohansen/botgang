import core.server.Player
import data.Command.*
import data.IncomingPacket
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Texture
import no.njoh.pulseengine.core.graphics.Surface2D
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.shared.primitives.Color
import kotlin.random.Random

class Client : Player<IncomingPacket>(IncomingPacket::class.java)

class Bot : SceneEntity(), Spatial, Renderable
{
    override var width = 45f
    override var height = 45f
    override var rotation = 0f
    override var x = 100f + 500 * Random.nextFloat()
    override var y = 100f + 300 * Random.nextFloat()
    override var z = 0f

    var name = "unknown"
    var color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
    var client: Client? = null
    var xCell = 0
    var yCell = 0

    fun onServerTick(engine: PulseEngine)
    {
        client?.response?.clientName?.let { name = it }

        val level = engine.scene.getFirstEntityOfType<Level>()
        val command = client?.response?.command ?: IDLE

        if (level != null)
        {
            when (command)
            {
                MOVE_UP -> move(level, 0, -1)
                MOVE_DOWN -> move(level, 0, 1)
                MOVE_LEFT -> move(level, -1, 0)
                MOVE_RIGHT -> move(level, 1, 0)
                IDLE -> {  }
            }

            x = level.x - (level.width * 0.5f) + (xCell * level.cellSize) + (level.cellSize * 0.5f)
            y = level.y - (level.height * 0.5f) + (yCell * level.cellSize) + (level.cellSize * 0.5f)
        }
    }

    private fun move(level: Level, xDir: Int, yDir: Int)
    {
        if (level.canMoveTo(xCell + xDir, yCell + yDir))
        {
            xCell += xDir
            yCell += yDir
        }
    }

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        surface.setDrawColor(color)
        surface.drawTexture(Texture.BLANK, x, y, width, height, rotation, xOrigin = 0.5f, yOrigin = 0.5f)
    }
}