package data
class IncomingPacket
{
    var clientName: String? = null
    var command = Command.IDLE
}

enum class Command {
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT,
    ROTATE_UP,
    ROTATE_DOWN,
    ROTATE_LEFT,
    ROTATE_RIGHT,
    IDLE
}