import Level.CellType.*
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Texture
import no.njoh.pulseengine.core.graphics.Surface2D
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.shared.primitives.Color
import kotlin.random.Random

class Level : SceneEntity(), Spatial, Renderable
{
    var cellSize = 20f; set (value) { field = value; updateDimensions() }
    var xCells   = 20;  set (value) { field = value; updateDimensions() }
    var yCells   = 20;  set (value) { field = value; updateDimensions() }

    override var width = xCells * cellSize
    override var height = yCells * cellSize
    override var rotation = 0f
    override var x = 0f
    override var y = 0f
    override var z = 0f

    private var cells = Array(xCells * yCells) { EMPTY }

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        val xStart = x - xCells * cellSize * 0.5f
        val yStart = y - yCells * cellSize * 0.5f

        for (yi in 0 until yCells)
        {
            for (xi in 0 until xCells)
            {
                val cell = cells[yi * xCells + xi]
                val color = when (cell) {
                    EMPTY -> Color.WHITE
                    WALL -> Color.BLACK
                }
                surface.setDrawColor(color)
                surface.drawTexture(Texture.BLANK, xStart + xi * cellSize, yStart + yi * cellSize, cellSize, cellSize)
            }
        }
    }

    private fun updateDimensions()
    {
        cells = Array(xCells * yCells) { if (Random.nextBoolean()) EMPTY else WALL }
        width = xCells * cellSize
        height = yCells * cellSize
    }

    fun canMoveTo(x: Int, y: Int): Boolean =
        x >= 0 && x < xCells && y >= 0 && y < yCells && cells[y * xCells + x] == EMPTY

    fun getFreeSpot(): Pair<Int, Int>
    {
        while (true)
        {
            val x = Random.nextInt(xCells)
            val y = Random.nextInt(yCells)
            if (cells[y * xCells + x] == EMPTY)
                return Pair(x, y)
        }
    }

    enum class CellType { EMPTY, WALL }
}