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
import java.util.zip.ZipEntry
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
        // Parse the main directory path from the config.json file
        val parser = JSONParser()
        val config = parser.parse(IOUtils.toString(this.javaClass.classLoader.getResource("config.json"))) as JSONObject
        mainDirPath = config["path"] as String
    }

    fun start() {
        // Open a local server on port 9091 and add two basic handlers
        val server = HttpServer.create(InetSocketAddress(9091), 0)
        server.createContext("/", this::handleSendData)
        server.createContext("/post", this::handleReceiveZip)

        server.executor = null
        server.start()
    }

    /**
     * Handlers initial GET requests from the client and sends them currently deployed functions
     */
    private fun handleSendData(exchange: HttpExchange) {
        exchange.sendResponseHeaders(200, data.length.toLong())
        val body = exchange.responseBody
        body.write(data.toByteArray())
        body.close()
    }

    /**
     * Handlers POST requests from the client for deployment of new functions
     */
    private fun handleReceiveZip(exchange: HttpExchange) {
        println("Receiving message")

        // Parse headers
        val name = exchange.requestHeaders.getFirst("func_name")
        val replicas = exchange.requestHeaders.getFirst("replicas")

        // Make directories needed for the new function
        File("$mainDirPath/$name").mkdirs()

        // Read the send ZIP file into a local file on this server that will be stored in '$mainDirPath/a.zip'
        val bos = BufferedOutputStream(FileOutputStream("$mainDirPath/a.zip"))
        bos.write(exchange.requestBody.readBytes())
        bos.close()

        println("Message received")

        // Send response to the client that the file has been received successfully
        exchange.sendResponseHeaders(200, 0)

        // Unzip the file into a new folder that was created especially for this new function
        val zis = ZipInputStream(FileInputStream("$mainDirPath/a.zip"))
        var entry = zis.nextEntry

        while (entry != null) {
            val fos = FileOutputStream("$mainDirPath/$name/${entry.name}")
            fos.write(zis.readBytes())
            fos.close()

            entry = zis.nextEntry
        }
    }
}
