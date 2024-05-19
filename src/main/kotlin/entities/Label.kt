package entities

import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Font
import no.njoh.pulseengine.core.graphics.surface.Surface
import no.njoh.pulseengine.core.shared.annotations.AssetRef
import no.njoh.pulseengine.core.shared.primitives.Color
import no.njoh.pulseengine.modules.scene.entities.StandardSceneEntity

class Label : StandardSceneEntity()
{
    @AssetRef(Font::class)
    var font = "font-bold"
    var text = "TEXT"
    var size = 20f
    var color = Color(1f, 1f, 1f)

    override fun onRender(engine: PulseEngine, surface: Surface)
    {
        val font = engine.asset.getOrNull(font) ?: Font.DEFAULT
        surface.setDrawColor(color)
        surface.drawText(text, x, y, font, size, xOrigin = 0.5f, yOrigin = 0.5f)
    }
}