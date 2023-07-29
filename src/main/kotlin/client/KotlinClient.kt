package client

import com.google.gson.GsonBuilder
import data.ClientResponse
import data.GameState
import data.NewBotResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

/**
 * Example client in Kotlin.
 */
fun main()
{
    val gson = GsonBuilder().create()
    val socket = Socket("127.0.0.1", 55500)
    val writer = PrintWriter(socket.getOutputStream(), true)
    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    val myId = gson.fromJson(reader.readLine(), NewBotResponse::class.java).id

    while (true)
    {
        // Parse the game state
        val gameState = gson.fromJson(reader.readLine(), GameState::class.java)

        // Do something with the state and calculate a response command
        val enemy = gameState.bots.find { it.id != myId && it.isAlive }
        val response = if (enemy != null) "MOVE_TO_${enemy.x}_${enemy.y}" else "IDLE"

        // Send the response
        writer.println(gson.toJson(ClientResponse().apply { command = response }))
    }
}