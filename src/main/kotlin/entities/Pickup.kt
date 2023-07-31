package entities

import data.GunState
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Initiable
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.scene.interfaces.Updatable
import no.njoh.pulseengine.core.shared.utils.Extensions.degreesBetween

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
        val level = engine.scene.getEntityOfType<Level>(parentId) ?: return
        val (xCell, yCell) = level.getCellPos(x, y)
        val (x, y) = level.getWorldPos(xCell, yCell)
        this.xCell = xCell
        this.yCell = yCell
        this.x = x
        this.y = y
    }

    override fun onFixedUpdate(engine: PulseEngine)
    {
        val level = engine.scene.getEntityOfType<Level>(parentId) ?: return
        val (xTarget, yTarget) = level.getWorldPos(xCell, yCell)
        x += (xTarget - x) * 0.3f
        y += (yTarget - y) * 0.3f
        rotation -= rotation.degreesBetween(angle.toFloat()) * 0.3f
    }

    override fun onUpdate(engine: PulseEngine)
    {
        val owner = engine.scene.getEntityOfType<Bot>(ownerId)
        if (owner != null)
        {
            xCell = owner.xCell
            yCell = owner.yCell
            angle = owner.angle
        }
        else ownerId = INVALID_ID
    }

    abstract fun use(engine: PulseEngine)

    abstract fun getState(): GunState
}