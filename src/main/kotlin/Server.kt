import core.server.GameServer
import data.OutgoingPacket
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.scene.SceneEntity.Companion.DEAD
import no.njoh.pulseengine.core.scene.SceneSystem
import no.njoh.pulseengine.core.shared.utils.Logger

class Server : SceneSystem()
{
    var port = 55500
    var tickRate = 10

    private var server = GameServer(Client::class.java)
    private var lastTickTime = 0L

    override fun onStart(engine: PulseEngine)
    {
        server.setOnNewPlayerConnection { client -> onConnected(client, engine) }
        server.setOnPlayerDisconnect { client -> onDisconnected(client, engine) }
        server.start(port)
    }

    override fun onUpdate(engine: PulseEngine)
    {
        val now = System.currentTimeMillis()
        val elapsedTime = now - lastTickTime
        if (elapsedTime > 1000 / tickRate)
        {
            tick(engine)
            lastTickTime = now
        }
    }

    private fun onConnected(client: Client, engine: PulseEngine)
    {
        println("Connected: ${client.ipAddress}")
        val bot = Bot()
        bot.client = client
        engine.scene.getFirstEntityOfType<Level>()?.let()
        {
            val (x,y) = it.getFreeSpot()
            bot.xCell = x
            bot.yCell = y
        }
        engine.scene.addEntity(bot)
    }

    private fun onDisconnected(client: Client, engine: PulseEngine)
    {
        println("Disconnected: ${client.ipAddress}")
        engine.scene.getAllEntitiesOfType<Bot>()?.firstOrNull { it.client === client }?.set(DEAD)
    }

    private fun tick(engine: PulseEngine)
    {
        engine.scene.forEachEntityOfType<Bot>{ it.onServerTick(engine) }

        val packet = OutgoingPacket().apply()
        {
            timestamp = System.currentTimeMillis().toString()
        }

        server.broadcast(packet)
    }

    override fun onRender(engine: PulseEngine) { }

    override fun onStop(engine: PulseEngine) { shutdown() }

    override fun onDestroy(engine: PulseEngine) { shutdown() }

    private fun shutdown()
    {
        try { server.shutdown() }
        catch (e: Exception) { Logger.error("Failed to shut down server: ${e.message}") }
    }
}