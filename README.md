# BOTGANG 

**Botgang** is the next game in the [shotgang](https://github.com/NiklasJohansen/shotgang) series of 2D top-down competitive 
shooters. This one is made for Låve-LAN 2023 and changes up the formula a bit. Each player is now controlled by a bot,
more specifically an external AI communicating with the game through a TCP socket connection. The bots are sent 
data about the gamestate each tick/round and responds with commands for the player to execute.

## Goal

The goal is to gather as many points as possible by eliminating other players, collecting items and surviving rounds. 
The player with the most points at the end of the game wins. Points are awarded as follows:

| Action                     | Score                                              |
|----------------------------|----------------------------------------------------|
| Surviving a round          | **100** points divided evenly among living players |
| Eliminating another player | **50** points                                      |
| Picking up a gun           | **10** points                                      |
| Restocking ammunition      | **5** points                                       |

A round is over when there is one or zero players alive, or when time runs out. Different levels will have 
different time limits.

## Commands

These are the available commands accepted by the game.

| Command        | <div style="width:220px">Example payload </div> | Action                                                                                          |
|----------------|-------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `NAME`         | `{"command": "NAME_NameOfBot"}`                 | Sets the bot name (max 10 chars).                                                               |
| `COLOR`        | `{"command": "COLOR_#0317fc"}`                  | Set the bot color.                                                                              |
| `MOVE_TO`      | `{"command": "MOVE_TO_15_7"}`                   | Moves the bot one cell along the path to the given position, or no action if no path was found. |
| `MOVE_UP`      | `{"command": "MOVE_UP"}`                        | Moves the bot one cell upwards (-Y dir) if possible.                                            |
| `MOVE_DOWN`    | `{"command": "MOVE_DOWN"}`                      | Moves the bot one cell downwards (+Y dir) if possible.                                          |
| `MOVE_LEFT`    | `{"command": "MOVE_LEFT"}`                      | Moves the bot one cell left (-X dir) if possible.                                               |
| `MOVE_RIGHT`   | `{"command": "MOVE_RIGHT"}`                     | Moves the bot one cell right (+X dir) if possible.                                              |
| `ROTATE_UP`    | `{"command": "ROTATE_UP"}`                      | Rotates the bot towards 90 degrees.                                                             |
| `ROTATE_DOWN`  | `{"command": "ROTATE_DOWN"}`                    | Rotates the bot towards 270 degrees.                                                            |
| `ROTATE_LEFT`  | `{"command": "ROTATE_LEFT"}`                    | Rotates the bot towards 180 degrees.                                                            |
| `ROTATE_RIGHT` | `{"command": "ROTATE_RIGHT"}`                   | Rotates the bot towards 0 degrees.                                                              |
| `PICK_UP`      | `{"command": "PICK_UP"}`                        | Picks up the item at the bot position. Will swap an already picked up item.                     |
| `DROP`         | `{"command": "DROP"}`                           | Drops the picked up item if present.                                                            |
| `USE`          | `{"command": "USE"}`                            | Uses the picked up item if present.                                                             |
| `IDLE`         | `{"command": "IDLE"}`                           | No action.                                                                                      |

## Gamestate

TODO