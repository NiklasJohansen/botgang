import core.server.GameServer
import data.OutgoingPacket
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.scene.SceneEntity.Companion.DEAD
import no.njoh.pulseengine.core.scene.SceneSystem
import no.njoh.pulseengine.core.shared.utils.Logger

class Server : SceneSystem()
{
    var port = 55500

    private var server = GameServer(Client::class.java)

    override fun onStart(engine: PulseEngine)
    {
        server.setOnNewPlayerConnection { client ->
            println("Connected: ${client.ipAddress}")
            val bot = Bot()
            bot.client = client
            engine.scene.addEntity(bot)
        }

        server.setOnPlayerDisconnect { client ->
            println("Disconnected: ${client.ipAddress}")
            engine.scene.getAllEntitiesOfType<Bot>()?.firstOrNull { it.client === client }?.set(DEAD)
        }

        server.start(port)
    }

    override fun onFixedUpdate(engine: PulseEngine)
    {
        val packet = OutgoingPacket().apply()
        {
            timestamp = System.currentTimeMillis().toString()
        }

        server.broadcast(packet)
    }

    override fun onRender(engine: PulseEngine)
    {
        // Draw player names
        engine.scene.getAllEntitiesOfType<Bot>()?.forEachIndexed { index, player ->
            engine.gfx.mainSurface.setDrawColor(player.color)
            engine.gfx.mainSurface.drawText(text = player.name, x = 10f, y = 20f + 30f * index, fontSize = 20f, yOrigin = 0.5f)
        }
    }

    override fun onStop(engine: PulseEngine) { shutdown() }

    override fun onDestroy(engine: PulseEngine) { shutdown() }

    private fun shutdown()
    {
        try { server.shutdown() }
        catch (e: Exception) { Logger.error("Failed to shut down server: ${e.message}") }
    }
}