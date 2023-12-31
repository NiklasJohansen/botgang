package data

import core.server.Player

class ClientResponse
{
    var command = "IDLE"
}

enum class Command
{
    NAME,
    COLOR,
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

class Client : Player<ClientResponse>(ClientResponse::class.java)