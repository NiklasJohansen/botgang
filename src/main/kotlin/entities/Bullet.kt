package entities

import data.BulletState
import data.Scores
import getActiveLevel
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Texture.Companion.BLANK
import no.njoh.pulseengine.core.graphics.Surface2D
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Initiable
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.scene.interfaces.Updatable

class Bullet : SceneEntity(), Initiable, Updatable, Renderable, Spatial
{
    override var width = 50f
    override var height = 50f
    override var rotation = 0f
    override var x = 0f
    override var y = 0f
    override var z = -1f

    var xVel = 0
    var yVel = 0
    var xCell = 0
    var yCell = 0
    var xCellLast = 0
    var yCellLast = 0
    var ownerId = INVALID_ID

    fun onServerTick(engine: PulseEngine)
    {
        val xNew = xCell + xVel
        val yNew = yCell + yVel
        val level = engine.scene.getActiveLevel() ?: return
        if (level.isWalkable(xNew, yNew))
        {
            xCellLast = xCell
            yCellLast = yCell
            xCell = xNew
            yCell = yNew
        }
        else this.set(DEAD)

        engine.scene.forEachEntityOfType<Bot> { bot ->
            val isOnSameSpot = bot.xCell == xCell && bot.yCell == yCell
            val passedThrough = bot.xCell == xCellLast && bot.yCell == yCellLast && bot.xCellLast == xCell && bot.yCellLast == yCell
            if (bot.id != ownerId && (isOnSameSpot || passedThrough))
            {
                bot.kill(engine)
                this.set(DEAD)
                engine.scene.getEntityOfType<Bot>(ownerId)?.let {
                    it.score += Scores.KILL
                    it.kills++
                }
            }
        }
    }

    override fun onFixedUpdate(engine: PulseEngine)
    {
        val level = engine.scene.getActiveLevel() ?: return
        val (x, y) = level.getWorldPos(xCell, yCell)
        this.x = x
        this.y = y
    }

    override fun onUpdate(engine: PulseEngine) { }

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        surface.setDrawColor(1f, 1f, 0f)
        surface.drawTexture(
            texture = BLANK,
            x = x,
            y = y,
            width = width * 0.6f,
            height = 5f,
            rot = if (yVel == 0) 0f else 90f,
            xOrigin = 0.5f,
            yOrigin = 0.5f,
            cornerRadius = 2f
        )
    }

    fun getState() = BulletState(id, xCell, yCell, xVel, yVel, ownerId)
}