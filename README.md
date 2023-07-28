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

| Command        | Example payload | Action                                                                                          |
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

## Connecting to the game

The game will be hosted on a server with a public IP address. The port number will be announced at the event.
On connection your client will receive a message with the ID of your bot: 
```json
{ "id": 123 }
```
This ID is used to identify your bot in the public game state distributed to all bots each tick.

Example code for connecting to the game using JavaScript:
```javascript
// Open a connection to the game server
const connection = new WebSocket('ws://127.0.0.1:55501');
let myId = -1;

// Listen for messages
connection.onmessage = function(event) {
    
    // Store the ID of your bot on initial connection
    if (myId === -1) {
        myId = JSON.parse(event.data).id
        return;
    }

    // Do something with the gamestate and calculate a response command
    const gamestate = JSON.parse(event.data);
    const enemy = gamestate.bots.find(b => b.id !== myId && b.alive);
    let command = "IDLE";
    if (enemy) {
        command = "MOVE_TO_" + enemy.x + "_" + enemy.y;
    }

    // Send the response 
    connection.send(JSON.stringify({ command: command }));
}
```
See [JsClient.html](https://github.com/NiklasJohansen/botgang/blob/master/JsClient.html) for an interactive web client
example.


## Gamestate

Each tick the game will send a message to all connected bots with the current state:

```json
{
  "tickNumber": 111,
  "level": {
    "name": "Level #1",
    "width": 21,
    "height": 21,
    "cells": [
      [2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
      [2, 1, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 1, 2],
      [2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0, 2],
      [2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2],
      [2, 2, 2, 0, 0, 0, 2, 0, 0, 2, 2, 2, 0, 0, 2, 0, 0, 0, 2, 2, 2],
      [2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2],
      [2, 0, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 0, 2],
      [2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2],
      [2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2],
      [2, 0, 0, 0, 2, 0, 2, 0, 0, 1, 2, 1, 0, 0, 2, 0, 2, 0, 0, 0, 2],
      [2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2],
      [2, 0, 0, 0, 2, 0, 2, 0, 0, 1, 2, 1, 0, 0, 2, 0, 2, 0, 0, 0, 2],
      [2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2],
      [2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2],
      [2, 0, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 0, 2],
      [2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2],
      [2, 2, 2, 0, 0, 0, 2, 0, 0, 2, 2, 2, 0, 0, 2, 0, 0, 0, 2, 2, 2],
      [2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2],
      [2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2],
      [2, 1, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 1, 2],
      [2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2]
    ]
  },
  "bots": [
    {
      "name": "Arne",
      "id": 56,
      "x": 1,
      "y": 1,
      "angle": 0,
      "isAlive": true
    },
    {
      "name": "Nils-Ove",
      "id": 57,
      "x": 17,
      "y": 18,
      "angle": 180,
      "isAlive": true
    }
  ],
  "guns": [
    {
      "id": 30,
      "x": 17,
      "y": 18,
      "ownerId": 56,
      "bulletCount": 3
    },
    {
      "id": 42,
      "x": 1,
      "y": 19,
      "ownerId": -1,
      "bulletCount": 2
    }
  ],
  "bullets": [
    {
      "id": 40,
      "x": 10,
      "y": 15,
      "xVelocity" : -1,
      "yVelocity" : 0,
      "angle": 0,
      "ownerId": 56
    }
  ],
  "ammo": [
    {
      "id": 50,
      "x": 15,
      "y": 15,
      "amount": 3
    }
  ]
}
```
The level consists of cells. A cell can be one of the following: **0** (empty), **1** (spawn point), **2** (wall).