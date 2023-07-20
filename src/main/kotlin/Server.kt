import core.server.GameServer
import data.Client
import data.GameState
import data.NewBotResponse
import data.Scores
import entities.*
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Font
import no.njoh.pulseengine.core.asset.types.Texture
import no.njoh.pulseengine.core.graphics.api.Multisampling.MSAA16
import no.njoh.pulseengine.core.input.Key
import no.njoh.pulseengine.core.scene.SceneEntity.Companion.DEAD
import no.njoh.pulseengine.core.scene.SceneState.RUNNING
import no.njoh.pulseengine.core.scene.SceneSystem
import no.njoh.pulseengine.core.shared.primitives.Color
import no.njoh.pulseengine.core.shared.primitives.SwapList
import no.njoh.pulseengine.core.shared.utils.Extensions.toRadians
import no.njoh.pulseengine.core.shared.utils.Logger
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class Server : SceneSystem()
{
    var port = 55500
    var tickRate = 10
    var maxBotCount = 8
    var activeLevel = -1L
    var levels = longArrayOf()
    var leaderBoardWaitTime = 5000f

    private var server = GameServer(Client::class.java)
    private var tickCount = 0L
    private var startTickNumber = 0L
    private var lastTickTime = 0L
    private var nextLevelTimer = 0L
    private var levelFinished = false
    private var gameFinished = false
    private var paused = false

    override fun onCreate(engine: PulseEngine)
    {
        engine.gfx.createSurface(name = "leaderboard", zOrder = -10, multisampling = MSAA16)
    }

    override fun onStart(engine: PulseEngine)
    {
        server.setOnNewPlayerConnection { client -> onConnected(client, engine) }
        server.setOnPlayerDisconnect { client -> onDisconnected(client, engine) }
        server.start(port)
    }

    override fun onUpdate(engine: PulseEngine)
    {
        if (!levelFinished && isLevelFinished(engine) && engine.scene.state == RUNNING)
        {
            finishLevel(engine)
        }

        val now = System.currentTimeMillis()
        val elapsedTime = now - lastTickTime
        if (!levelFinished && !paused && elapsedTime > 1000 / tickRate)
        {
            tick(engine)
            lastTickTime = now
        }

        if (levelFinished && !gameFinished && now - nextLevelTimer > leaderBoardWaitTime)
        {
            nextLevel(engine)
        }

        if (engine.input.wasClicked(Key.SPACE)) finishLevel(engine)
        if (engine.input.wasClicked(Key.P)) paused = !paused
        if (engine.input.wasClicked(Key.UP)) tickRate++
        if (engine.input.wasClicked(Key.DOWN)) tickRate = maxOf(1, tickRate - 1)
    }

    private fun onConnected(client: Client, engine: PulseEngine)
    {
        if (server.players.size > maxBotCount)
        {
            client.disconnect() // Server full
            return
        }

        val level = engine.scene.getActiveLevel() ?: return
        val bot = Bot()
        bot.client = client
        bot.color = Bot.nextFreeColor(engine)
        bot.setSpawn(engine, level)
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
            println("Disconnected: ${client.ipAddress}")
        }
    }

    private fun tick(engine: PulseEngine)
    {
        engine.scene.getActiveLevel()?.onServerTick(engine)

        engine.scene.getAllEntitiesOfType<Bot>()
            ?.shuffled()
            ?.forEach { it.onServerTick(engine) }

        engine.scene.forEachEntityOfType<Ammo> { it.onServerTick(engine) }

        engine.scene.forEachEntityOfType<Bullet> { it.onServerTick(engine) }

        // Send current game state to all clients
        distributeGameState(engine)
        tickCount++
    }

    private fun distributeGameState(engine: PulseEngine)
    {
        val level = engine.scene.getActiveLevel() ?: return
        val gameState = GameState(
            tickNumber = tickCount,
            level = level.getState(),
            bots = engine.scene.getAllEntitiesOfType<Bot>()
                ?.map { it.getState() } ?: emptyList(),
            guns = engine.scene.getAllEntitiesOfType<Gun>()
                ?.mapNotNull { if (it.parentId == level.id) it.getState() else null } ?: emptyList(),
            bullets = engine.scene.getAllEntitiesOfType<Bullet>()
                ?.map { it.getState() } ?: emptyList(),
            ammo = engine.scene.getAllEntitiesOfType<Ammo>()
                ?.map { it.getState() } ?: emptyList()
        )
        server.broadcast(gameState)
    }

    private fun finishLevel(engine: PulseEngine)
    {
        levelFinished = true
        nextLevelTimer = System.currentTimeMillis()
        if (levels.indexOf(activeLevel) == levels.lastIndex)
        {
            gameFinished = true
            Logger.info("Game finished!")
        }

        // Reward points among living players
        engine.scene.getAllEntitiesOfType<Bot>()
            ?.filter { it.isAlive }
            ?.let { bots -> bots.forEach { it.score += Scores.WIN / bots.size } }
    }

    private fun nextLevel(engine: PulseEngine)
    {
        if (activeLevel !in levels || gameFinished) return

        val nextLevel = levels.getOrNull(levels.indexOf(activeLevel) + 1)
        if (nextLevel == null)
        {
            Logger.info("No more levels")
            gameFinished = true
            return
        }

        startTickNumber = tickCount
        activeLevel = nextLevel

        // Respawn all bots
        val level = engine.scene.getEntityOfType<Level>(activeLevel) ?: return
        engine.scene.forEachEntityOfType<Bot> { bot ->
            bot.setSpawn(engine, level)
            bot.isAlive = true
        }

        // Reset all pickups
        engine.scene.forEachEntityOfType<Pickup> { it.ownerId = -1L }
        println("Next level: ${level.name} (${level.id}}")
        levelFinished = false
    }

    private fun isLevelFinished(engine: PulseEngine): Boolean
    {
        // Has the level time limit been reached
        val levelMaxTicks = engine.scene.getEntityOfType<Level>(activeLevel)?.maxTicks ?: -1
        if (levelMaxTicks > 0 && tickCount - startTickNumber > levelMaxTicks)
            return true

        // Is there only one bot left alive
        val bots = engine.scene.getAllEntitiesOfType<Bot>() ?: SwapList()
        val aliveBots = bots.count { it.isAlive }
        if (bots.size > 1 && aliveBots < 2)
            return true

        return false
    }

    override fun onRender(engine: PulseEngine)
    {
        if (levelFinished)
            drawLeaderboard(engine)
        else
            drawClock(engine)
    }

    private fun drawClock(engine: PulseEngine)
    {
        val xCenter = 30f
        val yCenter = 30f
        val radius = 20f
        val surface = engine.gfx.getSurfaceOrDefault("leaderboard")
        surface.setDrawColor(WHITE, 0.5f)

        // Draw circle
        var xLast = xCenter + radius * cos(0f)
        var yLast = yCenter + radius * sin(0f)
        val segments = 30
        val angleIncrement = (360f / segments).toRadians()
        for (i in 1 until segments + 1)
        {
            val x = xCenter + radius * cos(angleIncrement * i)
            val y = yCenter + radius * sin(angleIncrement * i)
            surface.drawLine(xLast, yLast, x, y)
            xLast = x
            yLast = y
        }

        // Draw clock arm
        val levelMaxTicks = engine.scene.getEntityOfType<Level>(activeLevel)?.maxTicks ?: 1f
        val t = (tickCount - startTickNumber) / levelMaxTicks.toFloat()
        val x = xCenter + radius * cos(t * 2f * PI.toFloat() - PI.toFloat() * 0.5f)
        val y = yCenter + radius * sin(t * 2f * PI.toFloat() - PI.toFloat() * 0.5f)
        surface.setDrawColor(WHITE, 0.8f)
        surface.drawLine(xCenter, yCenter, x, y)
    }

    private fun drawLeaderboard(engine: PulseEngine)
    {
        val now = System.currentTimeMillis()
        val time = now - nextLevelTimer
        val initWaitTime = leaderBoardWaitTime * 0.2f
        val easeInTime = leaderBoardWaitTime * 0.1f
        val waitTime = leaderBoardWaitTime * 0.6f
        val easeOutTime = leaderBoardWaitTime * 0.1f

        val alpha = when
        {
            time < initWaitTime -> 0f
            time <= initWaitTime + easeInTime -> (time - initWaitTime) / easeInTime
            time <= initWaitTime + easeInTime + waitTime -> 1f
            !gameFinished && time <= initWaitTime + easeInTime + waitTime + easeOutTime -> 1f - (time - (initWaitTime + easeInTime + waitTime)) / easeOutTime
            else -> 1f
        }

        val font = engine.asset.getOrNull<Font>("font-bold")
        val surface = engine.gfx.getSurfaceOrDefault("leaderboard")
        surface.setDrawColor(0f, 0f, 0f, alpha * 0.98f)
        surface.drawTexture(Texture.BLANK, 0f, 0f, surface.width.toFloat(), surface.height.toFloat())

        val bots = engine.scene.getAllEntitiesOfType<Bot>()?.sortedByDescending { it.score } ?: emptyList()
        val headingText = when
        {
            activeLevel == levels.firstOrNull() -> "Get Ready!"
            gameFinished -> bots.firstOrNull()?.name + " wins!"
            else -> "Leaderboard"
        }
        val headingColor = if (gameFinished) bots.firstOrNull()?.color ?: WHITE else WHITE

        // Draw leaderboard text on center of screen
        surface.setDrawColor(headingColor, alpha)
        surface.drawText(
            text = headingText,
            x = surface.width * 0.5f,
            y = surface.height * 0.175f,
            font = font,
            fontSize = 200f,
            xOrigin = 0.5f,
            yOrigin = 0.5f
        )

        val botSize = min(surface.height * 0.4f / bots.size, 80f)
        val ySpacing = botSize * 1.5f
        val xSpacing = botSize * 1.2f
        val xCenter = surface.width * 0.5f
        val yCenter = surface.height * 0.2f + 160f

        bots.forEachIndexed { i, bot ->
            // Bot body
            bot.drawBody(
                surface = surface,
                x = xCenter - surface.width * 0.2f,
                y = yCenter + i * ySpacing,
                width = botSize,
                height = botSize,
                rot = 0f,
                alpha
            )

            // Separator line
            surface.setDrawColor(0.6f, 0.6f, 0.6f, alpha)
            surface.drawLine(
                x0 = xCenter - surface.width * 0.2f + xSpacing,
                y0 = yCenter + i * ySpacing + botSize * 0.6f,
                x1 = xCenter + surface.width * 0.2f + botSize * 0.5f,
                y1 = yCenter + i * ySpacing + botSize * 0.6f,
            )

            // Name
            surface.setDrawColor(PODIUM_COLORS.getOrNull(i) ?: WHITE, alpha)
            surface.drawText(
                text = bot.name,
                x = xCenter - surface.width * 0.2f + xSpacing,
                y = yCenter + i * ySpacing,
                font = font,
                fontSize = botSize,
                xOrigin = 0f,
                yOrigin = 0.5f
            )

            // Score
            surface.drawText(
                text = "${bot.score} (${bot.kills})",
                x = xCenter + surface.width * 0.2f + botSize * 0.5f,
                y = yCenter + i * ySpacing,
                font = font,
                fontSize = botSize,
                xOrigin = 1f,
                yOrigin = 0.5f
            )
        }
    }

    override fun onStop(engine: PulseEngine) { shutdown() }

    override fun onDestroy(engine: PulseEngine) { shutdown() }

    private fun shutdown()
    {
        try { server.shutdown() }
        catch (e: Exception) { Logger.error("Failed to shut down server: ${e.message}") }
    }

    companion object
    {
        private val GOLD = Color(255, 215, 0)
        private val SILVER = Color(200, 200, 200)
        private val BRONZE = Color(176, 141, 87)
        private val WHITE = Color(0.95f, 0.95f, 0.95f)
        private val PODIUM_COLORS = listOf(GOLD, SILVER, BRONZE)
    }
}