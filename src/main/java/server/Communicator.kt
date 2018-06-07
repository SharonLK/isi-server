package server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.apache.commons.io.IOUtils
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.util.zip.ZipInputStream

fun main(args: Array<String>) {
    Communicator().start()
}

class Communicator {
    // Contains dummy data that is sent to the client, in the future this will be change to send actual real data
    private val data = IOUtils.toString(this.javaClass.classLoader.getResource("dummy_data.json"))

    // Contains the path to the directory where functions will be stored
    private val mainDirPath: String

    init {
        val parser = JSONParser()
        val config = parser.parse(IOUtils.toString(this.javaClass.classLoader.getResource("config.json"))) as JSONObject
        mainDirPath = config["path"] as String
    }

    fun start() {
        val server = HttpServer.create(InetSocketAddress(9091), 0)
        server.createContext("/", this::handleSendData)
        server.createContext("/post", this::handleReceiveZip)

        server.executor = null
        server.start()
    }

    private fun handleSendData(exchange: HttpExchange) {
        exchange.sendResponseHeaders(200, data.length.toLong())
        val body = exchange.responseBody
        body.write(data.toByteArray())
        body.close()
    }

    private fun handleReceiveZip(exchange: HttpExchange) {
        println("Receiving message")

        val name = exchange.requestHeaders.getFirst("func_name")
        val replicas = exchange.requestHeaders.getFirst("replicas")

        File("$mainDirPath/$name").mkdirs()

        val bos = BufferedOutputStream(FileOutputStream("$mainDirPath/a.zip"))
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

        val zis = ZipInputStream(FileInputStream("$mainDirPath/a.zip"))
        var zipEntry = zis.nextEntry

        while (zipEntry != null) {
            val fileName = zipEntry.name
            val newFile = File("$mainDirPath/$name/$fileName")
            val fos = FileOutputStream(newFile)
            var len = zis.read(buff)

            while (len > 0) {
                fos.write(buff, 0, len)

                len = zis.read(buff)
            }

            fos.close()
            zipEntry = zis.nextEntry
        }

        zis.closeEntry()
        zis.close()
    }
}
