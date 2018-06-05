package server

import com.sun.net.httpserver.HttpServer
import org.apache.commons.io.IOUtils
import java.net.InetSocketAddress

fun main(args: Array<String>) {
    Communicator().start()
}

class Communicator {
    init {

    }

    fun start() {
        val data = IOUtils.toString(this.javaClass.classLoader.getResource("dummy_data.json"))

        val server = HttpServer.create(InetSocketAddress(9091), 0)
        server.createContext("/", { exchange ->
            exchange.sendResponseHeaders(200, data.length.toLong())
            val body = exchange.responseBody
            body.write(data.toByteArray())
            body.close()
        })

        server.executor = null
        server.start()
    }
}
