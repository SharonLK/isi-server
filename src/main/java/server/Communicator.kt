package server

import org.apache.commons.io.IOUtils
import java.io.PrintWriter
import java.net.ServerSocket

fun main(args: Array<String>) {
    Communicator().start()
}

class Communicator {
    init {

    }

    fun start() {
        val data = IOUtils.toString(this.javaClass.classLoader.getResource("dummy_data.json"))

        val listener = ServerSocket(9091)
        println("Listening on port 9091")

        while (true) {
            val socket = listener.accept()
            println("New client found: ${socket.inetAddress}")

            val out = PrintWriter(socket.getOutputStream(), true)
            out.println(data)

            socket.close()
            println("Socket closed")
        }
    }
}