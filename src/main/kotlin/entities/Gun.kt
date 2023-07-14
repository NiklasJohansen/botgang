package entities

import data.PickupState
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Texture
import no.njoh.pulseengine.core.graphics.Surface2D

class Gun : Pickup()
{
    var bulletCount = 3

    override fun use(engine: PulseEngine)
    {
        if (bulletCount > 0)
        {
            var xDir = 0
            var yDir = 0
            when (angle)
            {
                0 -> xDir = 1
                90 -> yDir = -1
                180 -> xDir = -1
                270 -> yDir = 1
            }

            val bullet = Bullet()
            bullet.xCell = xCell
            bullet.yCell = yCell
            bullet.xVel = xDir
            bullet.yVel = yDir
            bullet.ownerId = ownerId
            engine.scene.addEntity(bullet)

            bulletCount--
        }
    }

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        // Handle
        surface.setDrawColor(71/255f, 51/255f, 26/255f)
        surface.drawTexture(
            texture = Texture.BLANK,
            x = x,
            y = y,
            width = width * 0.8f,
            height = height * 0.40f,
            rot = rotation + 80,
            xOrigin = 0.9f,
            yOrigin = 1f,
            cornerRadius = 4f
        )

        // Slide
        surface.setDrawColor(0.3f, 0.3f, 0.3f)
        surface.drawTexture(
            texture = Texture.BLANK,
            x = x,
            y = y,
            width = width,
            height = height * 0.32f,
            rot = rotation,
            xOrigin = 0.5f,
            yOrigin = 0.5f,
            cornerRadius = 4f
        )

        for (i in 0 until bulletCount)
        {
            // Tip
            surface.setDrawColor(0.7f, 0.7f, 0.7f)
            surface.drawTexture(
                texture = Texture.BLANK,
                x = x,
                y = y,
                width = width * 0.07f,
                height = height * 0.1f,
                rot = rotation + 80,
                xOrigin = 5.4f + (bulletCount - i - 1) * 1.85f,
                yOrigin = 1.5f,
                cornerRadius = 3f
            )

            // Casing
            surface.setDrawColor(209/255f, 165/255f, 27/255f)
            surface.drawTexture(
                texture = Texture.BLANK,
                x = x,
                y = y,
                width = width * 0.08f,
                height = height * 0.22f,
                rot = rotation + 80,
                xOrigin = 4.8f + (bulletCount - i - 1) * 1.6f,
                yOrigin = 1.5f,
                cornerRadius = 0f
            )
        }
    }

    override fun getState() = GunPickUpState(type = "GUN", id, xCell, yCell, ownerId, bulletCount)

    class GunPickUpState(
        type: String,
        id: Long,
        x: Int,
        y: Int,
        ownerId: Long?,
        val bulletCount: Int
    ) : PickupState(type, id, x, y, ownerId)
}