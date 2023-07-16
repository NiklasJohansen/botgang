package entities

import getActiveLevel
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.scene.SceneState
import no.njoh.pulseengine.modules.scene.entities.Camera

class LevelCamera : Camera()
{
    override fun onUpdate(engine: PulseEngine)
    {
        if (engine.scene.state != SceneState.RUNNING)
            return

        val level = engine.scene.getActiveLevel() ?: return

        targetEntityId = level.id
        viewPortWidth = level.width + 10f
        viewPortHeight = level.height + 10f

        super.onUpdate(engine)
    }
}