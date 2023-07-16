package entities

import com.fasterxml.jackson.annotation.JsonIgnore
import data.LevelState
import entities.Level.CellType.*
import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.asset.types.Texture
import no.njoh.pulseengine.core.graphics.Surface2D
import no.njoh.pulseengine.core.input.Key.*
import no.njoh.pulseengine.core.input.Mouse
import no.njoh.pulseengine.core.scene.SceneEntity
import no.njoh.pulseengine.core.scene.SceneState
import no.njoh.pulseengine.core.scene.interfaces.Initiable
import no.njoh.pulseengine.core.scene.interfaces.Named
import no.njoh.pulseengine.core.scene.interfaces.Renderable
import no.njoh.pulseengine.core.scene.interfaces.Spatial
import no.njoh.pulseengine.core.shared.annotations.ScnProp
import no.njoh.pulseengine.core.shared.primitives.Color
import kotlin.math.max

class Level : SceneEntity(), Initiable, Spatial, Named, Renderable
{
    @ScnProp(min = 1f)      var cellSize = 20f
    @ScnProp(min = 1f)      var xCells = 20
    @ScnProp(min = 1f)      var yCells = 20
    @ScnProp(hidden = true) var cells = Array(xCells * yCells) { FLOOR }

    var wallColor = Color(0.1f, 0.1f, 0.1f)
    var floorColor = Color(0.4f, 0.4f, 0.4f)
    var spawnColor = Color(0.4f, 0.4f, 0.4f)

    override var name = "Unnamed Level"
    override var width = xCells * cellSize
    override var height = yCells * cellSize
    override var rotation = 0f
    override var x = 0f
    override var y = 0f
    override var z = 0f

    @JsonIgnore
    var pickupIds = listOf<Long>()
    private var editType = FLOOR

    override fun onStart(engine: PulseEngine)
    {
        pickupIds = this.childIds?.filter { engine.scene.getEntity(it) is Pickup } ?: emptyList()
    }

    override fun onRender(engine: PulseEngine, surface: Surface2D)
    {
        if (xCells * yCells != cells.size)
            resizeLevel()

        val xStart = x - xCells * cellSize * 0.5f
        val yStart = y - yCells * cellSize * 0.5f

        for (cellType in CellType.values())
        {
            for (yi in 0 until yCells)
            {
                for (xi in 0 until xCells)
                {
                    if (cells[yi * xCells + xi] != cellType)
                        continue

                    val color = when (cellType)
                    {
                        FLOOR -> floorColor
                        SPAWN -> spawnColor
                        WALL -> wallColor
                    }
                    surface.setDrawColor(color)
                    surface.drawTexture(
                        texture = Texture.BLANK,
                        x = xStart + (xi + 0.5f) * cellSize,
                        y = yStart + (yi + 0.5f) * cellSize,
                        width = cellSize * 1.08f,
                        height = cellSize * 1.08f,
                        xOrigin = 0.5f,
                        yOrigin = 0.5f,
                        cornerRadius = 4f
                    )
                }
            }
        }



        if (engine.scene.state == SceneState.STOPPED && isSet(EDITABLE))
        {
            val x = ((engine.input.xWorldMouse - x + width * 0.5f) / cellSize).toInt()
            val y = ((engine.input.yWorldMouse - y + height * 0.5f) / cellSize).toInt()
            if (x >= 0 && x < xCells && y >= 0 && y < yCells)
            {
                if (engine.input.isPressed(K_1)) editType = FLOOR
                if (engine.input.isPressed(K_2)) editType = SPAWN
                if (engine.input.isPressed(K_3)) editType = WALL
                if (engine.input.isPressed(Mouse.RIGHT))
                    cells[y * xCells + x] = editType
            }
        }
    }

    private fun resizeLevel()
    {
        cells = Array(xCells * yCells) { FLOOR }
        width = xCells * cellSize
        height = yCells * cellSize
    }

    fun isWalkable(x: Int, y: Int): Boolean =
        x >= 0 && x < xCells && y >= 0 && y < yCells && cells[y * xCells + x].num < 2

    fun getSpawnPoint(engine: PulseEngine): Pair<Int, Int>
    {
        val bots = engine.scene.getAllEntitiesOfType<Bot>()
        val xCenter = (bots?.sumOf { it.xCell } ?: 0) / max(bots?.size ?: 1, 1)
        val yCenter = (bots?.sumOf { it.yCell } ?: 0) / max(bots?.size ?: 1, 1)
        var distMax = Int.MIN_VALUE
        var xMax = 1
        var yMax = 1

        for (yi in 0 until yCells)
        {
            for (xi in 0 until xCells)
            {
                val cell = cells[yi * xCells + xi]
                val dist = (xCenter - xi) * (xCenter - xi) + (yCenter - yi) * (yCenter - yi)
                if (cell == SPAWN && dist > distMax && bots?.any { it.xCell == xi && it.yCell == yi } != true)
                {
                    distMax = dist
                    xMax = xi
                    yMax = yi
                }
            }
        }

        // No spawn points found, return first spawn point
        if (distMax == Int.MIN_VALUE)
            cells.forEachIndexed { i, cell -> if (cell == SPAWN) return Pair(i % xCells, i / xCells) }

        return Pair(xMax, yMax)
    }

    fun getWorldPos(xCell: Int, yCell: Int) = Pair(
        x - width * 0.5f + xCell * cellSize + cellSize * 0.5f,
        y - height * 0.5f + yCell * cellSize + cellSize * 0.5f
    )

    enum class CellType(val num: Int)
    {
        FLOOR(0),
        SPAWN(1),
        WALL(2)
    }

    fun getState() = LevelState(
        name = name,
        width = xCells,
        height = yCells,
        cells = Array(yCells) { y -> IntArray(xCells) { x -> cells[y * xCells + x].num } }
    )
}