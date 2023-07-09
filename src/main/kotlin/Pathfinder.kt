import entities.Level
import java.util.*

class NewPathfinder
{
    private val OPEN_LIST_ID = 1
    private val CLOSED_LIST_ID = 2
    private val INVALID = -1
    private val MOVEMENT_COST = 10

    private var currentNode: Vector? = null
    private var targetNode: Vector? = null
    private var mapSize: Vector? = null
    private var dataMap: Array<Array<IntArray>> = emptyArray()
    private var pathFound = false

    fun getPath(level: Level, xStart: Int, yStart: Int, xTarget: Int, yTarget: Int): Stack<Vector>? {
        val start = Vector(xStart, yStart)
        val target = Vector(xTarget, yTarget)
        if (start == target)
            return null

        mapSize = Vector(level.xCells, level.yCells)
        targetNode = Vector(target.x, target.y)
        currentNode = Vector(start.x, start.y)
        dataMap = Array(mapSize!!.y) { Array(mapSize!!.x) { IntArray(6) } } // 0:parent_x, 1:parent_y,  2:F-Cost,  3:G-Cost,  4:H (Heuristic), 5:list(1=open, 2=closed)
        dataMap[start.y][start.x][0] = INVALID
        pathFound = false

        while (!pathFound)
        {
            for (dir in DIRECTIONS)
            {
                processNode(level, dir)
                if (pathFound)
                    break
            }

            if (!pathFound)
            {
                dataMap[currentNode!!.y][currentNode!!.x][5] = CLOSED_LIST_ID // Adds node to closed list
                currentNode = findLowestFCost()
                if (currentNode!!.x == INVALID) // If findLowestFCost return INVALID the open list is empty
                    return null
            }
        }

        return backtracePath()
    }

    private fun backtracePath(): Stack<Vector>
    {
        val path = Stack<Vector>()
        var parent = Vector(targetNode!!.x, targetNode!!.y)
        while (dataMap[parent.y][parent.x][0] != INVALID)
        {
            path.push(parent)
            parent = Vector(dataMap[parent.y][parent.x][0], dataMap[parent.y][parent.x][1])
        }
        return path
    }

    private fun processNode(level: Level, dir: Vector)
    {
        val currX = currentNode!!.x
        val currY = currentNode!!.y
        val testX = currX + dir.x
        val testY = currY + dir.y
        if (testX < 0 || testX >= dataMap[0].size || testY < 0 || testY >= dataMap.size)
            return

        if (!level.isWalkable(testX, testY))
            return

        if (targetNode!!.x == testX && targetNode!!.y == testY)
        {
            dataMap[targetNode!!.y][targetNode!!.x][0] = currentNode!!.x
            dataMap[targetNode!!.y][targetNode!!.x][1] = currentNode!!.y
            pathFound = true
            return
        }

        if (dataMap[testY][testX][5] == OPEN_LIST_ID) // In open list
        {
            val newGCost = dataMap[currY][currX][3] + MOVEMENT_COST // New G-Cost
            if (newGCost < dataMap[testY][testX][3]) // Less than last G-Cost?
            {
                dataMap[testY][testX][0] = currX // Set parents x position
                dataMap[testY][testX][1] = currY // Set parents y position
                dataMap[testY][testX][3] = newGCost // updates G-Cost
                dataMap[testY][testX][2] = newGCost + dataMap[testY][testX][4] // calculates F-Cost
            }
        }
        else if (dataMap[testY][testX][5] != CLOSED_LIST_ID) // not in closed list
        {
            dataMap[testY][testX][0] = currX // Set parents x position
            dataMap[testY][testX][1] = currY // Set parents y position
            dataMap[testY][testX][3] = dataMap[currY][currX][3] + MOVEMENT_COST // calculates new G-Cost
            dataMap[testY][testX][4] =
                Math.abs(testX - targetNode!!.x) + Math.abs(testY - targetNode!!.y) // calculates H
            dataMap[testY][testX][2] = dataMap[testY][testX][3] + dataMap[testY][testX][4] // calculates F-Cost
            dataMap[testY][testX][5] = OPEN_LIST_ID // add node to open list
        }
    }

    private fun findLowestFCost(): Vector
    {
        var X = INVALID
        var Y = INVALID
        var smalestF = Int.MAX_VALUE
        for (y in 0 until mapSize!!.y)
        {
            for (x in 0 until mapSize!!.x)
            {
                if (dataMap[y][x][2] <= smalestF && dataMap[y][x][5] == OPEN_LIST_ID)
                {
                    X = x
                    Y = y
                    smalestF = dataMap[y][x][2]
                }
            }
        }
        return Vector(X, Y)
    }

    companion object
    {
        val DIRECTIONS = arrayOf(Vector(-1, 0), Vector(1, 0), Vector(0, -1), Vector(0, 1))
    }
}

data class Vector(
    var x: Int = 0,
    var y: Int = 0
)