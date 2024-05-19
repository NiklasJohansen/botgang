package util

import Server
import entities.Level
import entities.Pickup
import no.njoh.pulseengine.core.graphics.surface.Surface
import no.njoh.pulseengine.core.scene.SceneEntityList
import no.njoh.pulseengine.core.scene.SceneManager
import no.njoh.pulseengine.core.shared.primitives.Color

fun Surface.setDrawColor(color: Color, alpha: Float = 1f)
{
    setDrawColor(color.red, color.green, color.blue, color.alpha * alpha)
}

fun SceneManager.getActiveLevel(): Level?
{
    val levelId = getSystemOfType<Server>()?.activeLevel ?: return null
    return getEntityOfType(levelId)
}

fun SceneManager.getItemPickedUpBy(botId: Long): Pickup?
{
    val level = getActiveLevel() ?: return null
    for (id in level.pickupIds)
    {
        val pickup = getEntityOfType<Pickup>(id)
        if (pickup?.ownerId == botId)
            return pickup
    }
    return null
}

inline fun SceneManager.forEachActivePickup(action: (Pickup) -> Unit)
{
    val level = getActiveLevel() ?: return
    for (id in level.pickupIds)
        getEntityOfType<Pickup>(id)?.let(action)
}

val EMPTY_LIST = SceneEntityList<Nothing>()