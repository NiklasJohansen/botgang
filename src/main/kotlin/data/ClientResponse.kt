package data

class ClientResponse
{
    var command = "IDLE"
}

enum class Command
{
    NAME,
    MOVE_TO,
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT,
    ROTATE_UP,
    ROTATE_DOWN,
    ROTATE_LEFT,
    ROTATE_RIGHT,
    PICK_UP,
    DROP,
    USE,
    IDLE;

    companion object
    {
        fun parse(command: String) = values().find { command.startsWith(it.name) }
    }
}