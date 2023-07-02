import core.server.Player
import data.IncomingPacket
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Texture
import no.njoh.pulseengine.core.graphics.Surface2D
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.scene.interfaces.Updatable
import no.njoh.pulseengine.core.shared.primitives.Color
import kotlin.random.Random

class Client : Player<IncomingPacket>(IncomingPacket::class.java)

class Bot : SceneEntity(), Spatial, Updatable, Renderable
{
    override var width = 20f
    override var height = 20f
    override var rotation = 0f
    override var x = 100f + 500 * Random.nextFloat()
    override var y = 100f + 300 * Random.nextFloat()
    override var z = 0f

    var name = "unknown"
    var color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
    var client: Client? = null

    override fun onFixedUpdate(engine: PulseEngine)
    {
        client?.response?.clientName?.let { name = it }
    }

    override fun onUpdate(engine: PulseEngine)
    {
        // Update bot
    }

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        surface.setDrawColor(color)
        surface.drawTexture(Texture.BLANK, x, y, width, height, rotation)
    }
}