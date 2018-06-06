package server

import com.sun.net.httpserver.HttpServer
import org.apache.commons.io.IOUtils
import java.io.BufferedOutputStream
import java.io.FileOutputStream
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
        server.createContext("/post", { exchange ->
            println("Receiving message")

            val bos = BufferedOutputStream(FileOutputStream("/home/sharon/a.txt"))
            val buff = ByteArray(2048)
            val requestBody = exchange.requestBody
            var c = requestBody.read(buff)

            while (c > 0) {
                bos.write(buff, 0, c)
                c = requestBody.read(buff)
            }

            bos.close()
            requestBody.close()

            println("Message received")

            exchange.sendResponseHeaders(200, 0)
        })

        server.executor = null
        server.start()
    }
}
