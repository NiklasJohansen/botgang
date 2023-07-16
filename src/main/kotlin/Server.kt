import core.server.GameServer
import data.GameState
import data.NewBotResponse
import entities.*
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.scene.SceneEntity.Companion.DEAD
import no.njoh.pulseengine.core.scene.SceneSystem
import no.njoh.pulseengine.core.shared.utils.Logger

class Server : SceneSystem()
{
    var port = 55500
    var tickRate = 10
    var maxBotCount = 8
    var activeLevel = -1L
    var levels = longArrayOf()

    private var server = GameServer(Client::class.java)
    private var lastTickTime = 0L
    private var tickCount = 0L

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
        if (server.players.size > maxBotCount)
        {
            client.disconnect() // Server full
            return
        }

        val level = engine.scene.getActiveLevel() ?: return
        val (xCell,yCell) = level.getSpawnPoint(engine)
        val (x,y) = level.getWorldPos(xCell, yCell)
        val bot = Bot()
        bot.client = client
        engine.scene.getFirstEntityOfType<Level>()?.let()
        {
            val (x,y) = it.getFreeSpot(engine)
            bot.xCell = x
            bot.yCell = y
            bot.xCellLast = x
            bot.yCellLast = y
        }
        engine.scene.addEntity(bot)
        client.send(NewBotResponse(bot.id))
        println("Connected: ${client.ipAddress}")
    }

    private fun onDisconnected(client: Client, engine: PulseEngine)
    {
        engine.scene.getAllEntitiesOfType<Bot>()?.firstOrNull { it.client === client }?.let()
        {
            it.kill(engine)
            it.set(DEAD)
        }
        println("Disconnected: ${client.ipAddress}")
    }

    private fun tick(engine: PulseEngine)
    {
        // Update entities
        engine.scene.forEachEntityOfType<Bot> { it.onServerTick(engine) }
        engine.scene.forEachEntityOfType<Bullet> { it.onServerTick(engine) }

        // Send current game state to all clients
        distributeGameState(engine)
        tickCount++
    }

    private fun distributeGameState(engine: PulseEngine)
    {
        val gameState = GameState(
            tickNumber = tickCount,
            level = engine.scene.getActiveLevel()?.getState() ?: return,
            bots = engine.scene.getAllEntitiesOfType<Bot>()?.map { it.getState() } ?: emptyList(),
            pickups = engine.scene.getAllEntitiesOfType<Gun>()?.map { it.getState() } ?: emptyList(),
            bullets = engine.scene.getAllEntitiesOfType<Bullet>()?.map { it.getState() } ?: emptyList(),
        )
        server.broadcast(gameState)
    }

    private fun nextLevel(engine: PulseEngine)
    {
        if (activeLevel !in levels) return

        val nextLevel = levels.getOrNull(levels.indexOf(activeLevel) + 1)

        if (nextLevel == null)
        {
            Logger.info("No more levels")
            return
        }

        activeLevel = nextLevel
        val level = engine.scene.getEntityOfType<Level>(activeLevel) ?: return

        // Respawn all bots
        engine.scene.forEachEntityOfType<Bot> { bot ->
            bot.setSpawn(engine, level)
            bot.isAlive = true
        }
        engine.scene.forEachEntityOfType<Pickup> { it.ownerId = -1L }
    }

    private fun Bot.setSpawn(engine: PulseEngine, level: Level)
    {
        val (xCell, yCell) = level.getSpawnPoint(engine)
        val (x,y) = level.getWorldPos(xCell, yCell)
        this.x = x
        this.y = y
        this.xCell = xCell
        this.yCell = yCell
        this.xCellLast = xCell
        this.yCellLast = yCell
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