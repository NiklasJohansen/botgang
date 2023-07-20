# Botgang 

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

TODO

## Gamestate

TODO