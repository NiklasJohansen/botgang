package util

import entities.Level
import java.util.*
import kotlin.math.abs

class Pathfinder
{
    private var width = 0
    private var height = 0

    fun getPath(level: Level, xStart: Int, yStart: Int, xTarget: Int, yTarget: Int): Stack<Vector>? {
        if (xStart == xTarget && yStart == yTarget)
            return null

        width = level.xCells
        height = level.yCells
        val map = IntArray(width * height * 6).also { it[X_PARENT, xStart, yStart] = INVALID }
        var xNode = xStart
        var yNode = yStart

        while (true)
        {
            for (dir in DIRECTIONS)
            {
                val xTest = xNode + dir.x
                val yTest = yNode + dir.y
                if (!level.isWalkable(xTest, yTest) ||
                    (level.isOccupied(xTest, yTest) && xTest != xTarget && yTest != yTarget)
                ) {
                    continue
                }

                val targetFound = processNode(map, xNode, yNode, xTarget, yTarget, xTest, yTest)
                if (targetFound)
                    return getBackTracedPathFrom(map, xTarget, yTarget)
            }

            map[LIST, xNode, yNode] = CLOSED_LIST_ID // Add node to closed list

            val (x, y) = findOpenNodeWithLowestFCost(map) ?: return null // No more nodes to process, no path found
            xNode = x
            yNode = y
        }
    }

    private fun processNode(map: IntArray, xNode: Int, yNode: Int, xTarget: Int, yTarget: Int, xTest: Int, yTest: Int): Boolean
    {
        if (xTest == xTarget && yTest == yTarget)
        {
            map[X_PARENT, xTest, yTest] = xNode
            map[Y_PARENT, xTest, yTest] = yNode
            return true // Target found
        }

        val list = map[LIST, xTest, yTest]
        if (list == OPEN_LIST_ID)
        {
            val newGCost = map[G_COST, xNode, yNode] + MOVEMENT_COST
            if (newGCost < map[G_COST, xTest, yTest])
            {
                map[X_PARENT, xTest, yTest] = xNode
                map[Y_PARENT, xTest, yTest] = yNode
                map[G_COST,   xTest, yTest] = newGCost
                map[F_COST,   xTest, yTest] = newGCost + map[HEURISTIC, xTest, yTest]
            }
        }
        else if (list != CLOSED_LIST_ID)
        {
            val heuristic = abs(xTest - xTarget) + abs(yTest - yTarget)
            val newGCost = map[G_COST, xNode, yNode] + MOVEMENT_COST
            val newFCost = newGCost + heuristic
            map[X_PARENT,  xTest, yTest] = xNode
            map[Y_PARENT,  xTest, yTest] = yNode
            map[G_COST,    xTest, yTest] = newGCost
            map[HEURISTIC, xTest, yTest] = heuristic
            map[F_COST,    xTest, yTest] = newFCost
            map[LIST,      xTest, yTest] = OPEN_LIST_ID
        }

        return false
    }

    private fun getBackTracedPathFrom(map: IntArray, x: Int, y: Int): Stack<Vector>
    {
        val path = Stack<Vector>()
        var parent = Vector(x, y)
        while (map[X_PARENT, parent.x, parent.y] != INVALID)
        {
            path.push(parent)
            parent = Vector(x = map[X_PARENT, parent.x, parent.y], y = map[Y_PARENT, parent.x, parent.y])
        }
        return path
    }

    private fun findOpenNodeWithLowestFCost(map: IntArray): Vector?
    {
        var index = 0
        var minIndex = INVALID
        var minCost = Int.MAX_VALUE
        while (index < map.size)
        {
            val cost = map[index + F_COST]
            if (cost < minCost && map[index + LIST] == OPEN_LIST_ID)
            {
                minIndex = index
                minCost = cost
            }
            index += 6
        }
        return if (minIndex == INVALID) null else Vector(minIndex / 6 % width, minIndex / 6 / width)
    }

    private operator fun IntArray.get(prop: Int, x: Int, y: Int) = this[(y * width + x) * 6 + prop]
    private operator fun IntArray.set(prop: Int, x: Int, y: Int, value: Int) { this[(y * width + x) * 6 + prop] = value }

    companion object
    {
        val DIRECTIONS = arrayOf(Vector(-1, 0), Vector(1, 0), Vector(0, -1), Vector(0, 1))

        // Props
        private const val X_PARENT  = 0
        private const val Y_PARENT  = 1
        private const val F_COST    = 2
        private const val G_COST    = 3
        private const val HEURISTIC = 4
        private const val LIST      = 5

        // Values
        private const val OPEN_LIST_ID   =  1
        private const val CLOSED_LIST_ID =  2
        private const val INVALID        = -1
        private const val MOVEMENT_COST  = 10
    }
}

data class Vector(
    var x: Int = 0,
    var y: Int = 0
)