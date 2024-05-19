package entities

import data.BulletState
import data.Scores
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Texture.Companion.BLANK
import no.njoh.pulseengine.core.graphics.surface.Surface
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Initiable
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.scene.interfaces.Updatable
import util.EMPTY_LIST
import util.getActiveLevel

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

        for (bot in engine.scene.getAllEntitiesOfType<Bot>() ?: EMPTY_LIST)
        {
            if (bot.id == ownerId || !bot.isAlive) continue

            val isOnSameSpot = bot.xCell == xCell && bot.yCell == yCell
            val passedThroughBot = bot.xCell == xCellLast && bot.yCell == yCellLast && bot.xCellLast == xCell && bot.yCellLast == yCell

            if (isOnSameSpot || passedThroughBot)
            {
                bot.kill(engine)
                this.set(DEAD)
                engine.scene.getEntityOfType<Bot>(ownerId)?.let()
                {
                    it.score += Scores.KILL
                    it.kills++
                }
            }
        }
    }

    override fun onFixedUpdate(engine: PulseEngine)
    {
        val level = engine.scene.getActiveLevel() ?: return
        val (xTarget, yTarget) = level.getWorldPos(xCell, yCell)
        x += (xTarget - x) * 0.3f
        y += (yTarget - y) * 0.3f
    }

    override fun onUpdate(engine: PulseEngine) { }

    override fun onRender(engine: PulseEngine, surface: Surface)
    {
        surface.setDrawColor(1f, 1f, 0f)
        surface.drawTexture(
            texture = BLANK,
            x = x,
            y = y,
            width = width * 0.6f,
            height = 5f,
            angle = if (yVel == 0) 0f else 90f,
            xOrigin = 0.5f,
            yOrigin = 0.5f,
            cornerRadius = 2f
        )
    }

    fun getState() = BulletState(id, xCell, yCell, xVel, yVel, ownerId)
}