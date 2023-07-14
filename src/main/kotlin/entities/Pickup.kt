package entities

import data.PickupState
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Initiable
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.scene.interfaces.Updatable

abstract class Pickup : SceneEntity(), Initiable, Updatable, Renderable, Spatial
{
    override var width = 20f
    override var height = 20f
    override var rotation = 0f
    override var x = 0f
    override var y = 0f
    override var z = -1f

    var xCell = 0
    var yCell = 0
    var angle = 0
    var ownerId = INVALID_ID

    override fun onStart(engine: PulseEngine)
    {
        setPosition(engine)
    }

    override fun onFixedUpdate(engine: PulseEngine) { }

    override fun onUpdate(engine: PulseEngine)
    {
        val owner = engine.scene.getEntityOfType<Bot>(ownerId)
        if (owner != null)
        {
            x = owner.x
            y = owner.y
            rotation = owner.rotation
            xCell = owner.xCell
            yCell = owner.yCell
            angle = owner.angle
        }
        else ownerId = INVALID_ID
    }

    private fun setPosition(engine: PulseEngine)
    {
        val level = engine.scene.getFirstEntityOfType<Level>() ?: return
        val xClosestCell = (x - (level.x - level.width * 0.5f)).coerceIn(0f, level.width) / level.cellSize
        val yClosestCell = (y - (level.y - level.height * 0.5f)).coerceIn(0f, level.height) / level.cellSize
        xCell = xClosestCell.toInt()
        yCell = yClosestCell.toInt()

        val (xTarget, yTarget) = level.getWorldPos(xCell, yCell)
        x = xTarget
        y = yTarget
    }

    abstract fun use(engine: PulseEngine)

    abstract fun getState(): PickupState
}