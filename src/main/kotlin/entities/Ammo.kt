package entities

import data.AmmoState
import data.Scores
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Texture
import no.njoh.pulseengine.core.graphics.surface.Surface
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Initiable
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.scene.interfaces.Updatable
import util.EMPTY_LIST
import util.getActiveLevel
import util.getItemPickedUpBy
import kotlin.math.max
import kotlin.math.min

class Ammo : SceneEntity(), Initiable, Updatable, Renderable, Spatial
{
    override var width = 50f
    override var height = 50f
    override var rotation = 0f
    override var x = 0f
    override var y = 0f
    override var z = -1f

    var xCell = 0
    var yCell = 0
    var amount = 3
    var maxAmount = 3
    var coolDownTicks = 10

    private var coolDownTimer = 0

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

    fun onServerTick(engine: PulseEngine)
    {
        if (parentId != engine.scene.getActiveLevel()?.id)
            return // Only update ammo in active level

        coolDownTimer = max(0, coolDownTimer - 1)
        if (coolDownTimer == 0 && amount < maxAmount)
        {
            amount++
            coolDownTimer = coolDownTicks
        }

        for (bot in engine.scene.getAllEntitiesOfType<Bot>() ?: EMPTY_LIST)
        {
            val isOnSameSpot = bot.xCell == xCell && bot.yCell == yCell
            if (!isOnSameSpot || amount <= 0)
                continue

            val gun = engine.scene.getItemPickedUpBy(bot.id) as? Gun ?: continue
            var wasRestocked = false
            while (gun.bulletCount < 3 && amount > 0)
            {
                amount--
                gun.bulletCount++
                coolDownTimer = coolDownTicks
                wasRestocked = true
            }

            if (wasRestocked)
                bot.score += Scores.RESTOCK
        }
    }

    override fun onFixedUpdate(engine: PulseEngine) { }

    override fun onUpdate(engine: PulseEngine) { }

    override fun onRender(engine: PulseEngine, surface: Surface)
    {
        val size = min(width, height) * 0.9f
        surface.setDrawColor(0f, 0f, 0f, 0.05f)
        surface.drawTexture(Texture.BLANK, x, y, size, size, xOrigin = 0.5f, yOrigin = 0.5f, cornerRadius = size * 0.5f)

        val bulletWidth = width * 0.13f
        for (i in 0 until amount)
        {
            val x = x - (amount - 1) * bulletWidth * 0.5f + i * bulletWidth
            val y = y - height * 0.03f

            // Tip
            surface.setDrawColor(0.7f, 0.7f, 0.7f)
            surface.drawTexture(
                texture = Texture.BLANK,
                x = x,
                y = y,
                width = bulletWidth * 0.5f,
                height = height * 0.15f,
                angle = rotation + 180,
                xOrigin = 0.5f,
                yOrigin = -0.2f,
                cornerRadius = 3f
            )

            // Casing
            surface.setDrawColor(209/255f, 165/255f, 27/255f)
            surface.drawTexture(
                texture = Texture.BLANK,
                x = x,
                y = y,
                width = bulletWidth * 0.6f,
                height = height * 0.22f * 1.5f,
                angle = rotation + 180,
                xOrigin = 0.5f,
                yOrigin = 0.7f,
                cornerRadius = 0f
            )
        }
    }

    fun getState() = AmmoState(id, xCell, yCell, amount)
}