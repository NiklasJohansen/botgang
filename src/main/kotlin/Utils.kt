import entities.Pickup
import no.njoh.pulseengine.core.graphics.Surface2D
import no.njoh.pulseengine.core.scene.SceneManager
import no.njoh.pulseengine.core.shared.primitives.Color

fun Surface2D.setDrawColor(color: Color, alpha: Float = 1f)
{
    setDrawColor(color.red, color.green, color.blue, color.alpha * alpha)
}

fun SceneManager.getItemPickedUpBy(botId: Long): Pickup?
{
    forEachEntityOfType<Pickup>()
    {
        if (it.ownerId == botId)
            return it
    }
    return null
}