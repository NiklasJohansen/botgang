package entities

import data.BulletState
import no.njoh.pulseengine.core.PulseEngine
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
        val level = engine.scene.getFirstEntityOfType<Level>() ?: return
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
            }
        }
    }

    override fun onFixedUpdate(engine: PulseEngine)
    {
        val level = engine.scene.getFirstEntityOfType<Level>() ?: return
        val (x, y) = level.getWorldPos(xCell, yCell)
        this.x = x
        this.y = y
    }

    override fun onUpdate(engine: PulseEngine)
    {

    }

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        val length = width * 0.5f
        val x0 = x - length * xVel
        val y0 = y - length * yVel
        val x1 = x + length * xVel
        val y1 = y + length * yVel

        // Draw bullet as line
        surface.setDrawColor(1f, 1f, 0f)
        surface.drawLine(x0, y0, x1, y1)
    }

    fun getState() = BulletState(id, xCell, yCell, xVel, yVel, ownerId)
}