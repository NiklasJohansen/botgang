package entities

import data.AmmoState
import data.Scores
import getItemPickedUpBy
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Texture
import no.njoh.pulseengine.core.graphics.Surface2D
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Initiable
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.scene.interfaces.Updatable
import no.njoh.pulseengine.core.shared.primitives.SwapList
import kotlin.math.max

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
        coolDownTimer = max(0, coolDownTimer - 1)
        if (coolDownTimer == 0 && amount < maxAmount)
        {
            amount++
            coolDownTimer = coolDownTicks
        }

        for (bot in engine.scene.getAllEntitiesOfType<Bot>() ?: SwapList())
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

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        val bulletWidth = width * 0.13f
        for (i in 0 until amount)
        {
            val x = x - (amount - 1) * bulletWidth * 0.5f + i * bulletWidth

            // Tip
            surface.setDrawColor(0.7f, 0.7f, 0.7f)
            surface.drawTexture(
                texture = Texture.BLANK,
                x = x,
                y = y,
                width = bulletWidth * 0.5f,
                height = height * 0.15f,
                rot = rotation + 180,
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
                rot = rotation + 180,
                xOrigin = 0.5f,
                yOrigin = 0.7f,
                cornerRadius = 0f
            )
        }
    }

    fun getState() = AmmoState(id, xCell, yCell, amount)
}