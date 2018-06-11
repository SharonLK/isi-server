package server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import faas.buildAndDeploy
import faas.list
import faas.remove
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.*
import java.net.InetSocketAddress

fun main(args: Array<String>) {
    Communicator().start()
}

class Communicator {
    // Contains the path to the directory where functions will be stored
    private val mainDirPath: String

    init {
        // Parse the main directory path from the config.json file
        val parser = JSONParser()
        val config = parser.parse(IOUtils.toString(this.javaClass.classLoader.getResource("config.json"))) as JSONObject
        mainDirPath = (config["path"] as String).replaceFirst("^~".toRegex(), System.getProperty("user.home"))
    }

    fun start() {
        // Open a local server on port 9091 and add two basic handlers
        val server = HttpServer.create(InetSocketAddress(9091), 0)
        server.createContext("/", this::handleSendData)
        server.createContext("/post", this::handleReceiveZip)
        server.createContext("/download", this::handleDownload)
        server.createContext("/resubmit", this::handleReSubmit)

        server.executor = null
        server.start()
    }

    /**
     * Retrieves data of currently deployed functions and packages it in a JSON format with the following structure:
     *
     *  {
     *      functions: [
     *          {
     *              name: <name#1>
     *              invocations: <invocations#1>
     *              replicas: <replicas#1>
     *              url: <url#1>
     *          },
     *          {
     *              name: <name#1>
     *              invocations: <invocations#1>
     *              replicas: <replicas#1>
     *              url: <url#1>
     *          }
     *      ]
     *  }
     */
    private fun functionsData(): String {
        val functions = list()
        val json = JSONObject()
        val jsonFunctions = JSONArray()

        // Iterate over all functions and add them to the JSON array
        functions.forEach({ function ->
            jsonFunctions.add(JSONObject(hashMapOf(
                    "name" to function.functionName,
                    "invocations" to function.invocations,
                    "replicas" to function.replicas,
                    "url" to "http://127.0.0.1/function/${function.functionName}"
            )))
        })

        json["functions"] = jsonFunctions

        return json.toJSONString()
    }

    /**
     * Handlers initial GET requests from the client and sends them currently deployed functions
     */
    private fun handleSendData(exchange: HttpExchange) {
        val data = functionsData()

        exchange.sendResponseHeaders(200, data.length.toLong())
        val body = exchange.responseBody
        body.write(data.toByteArray())
        body.close()
    }

    private fun handleReSubmit(exchange: HttpExchange) {
        println("Receiving message")

        // Parse headers
        val name = exchange.requestHeaders.getFirst("name")

        // Read the send ZIP file into a local file on this server that will be stored in '$mainDirPath/a.zip'
        val bos = BufferedOutputStream(FileOutputStream("$mainDirPath/a.zip"))
        bos.write(exchange.requestBody.readBytes())
        bos.close()

        println("Message received")

        // Send response to the client that the file has been received successfully
        exchange.sendResponseHeaders(200, 0)

        // Remove the docker container, the whole directory and then create an empty directory instead
        remove(name)
        FileUtils.deleteDirectory(File("$mainDirPath/$name/"))
        File("$mainDirPath/$name").mkdirs()

        // Unzip the file into a new folder that was created especially for this new function
        val zf = ZipFile("$mainDirPath/a.zip")
        zf.extractAll("$mainDirPath/$name/")

        buildAndDeploy(name, "python3", name, "$mainDirPath/$name/")
    }

    /**
     * Handles POST requests from the client for deployment of new functions
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
        val zf = ZipFile("$mainDirPath/a.zip")
        zf.extractAll("$mainDirPath/$name/")
    }

    /**
     * Handles GET requests from the client asking to download a certain function. The function is packaged into a ZIP
     * file and sent back to the client.
     */
    private fun handleDownload(exchange: HttpExchange) {
        println("Receiving /download message")

        // Parse headers
        val name = exchange.requestHeaders.getFirst("name")

        val zf = ZipFile("$mainDirPath/a.zip")
        val params = ZipParameters()
        params.compressionLevel = Zip4jConstants.COMP_DEFLATE
        params.isIncludeRootFolder = false
        zf.addFolder("$mainDirPath/$name", params)

        // Send response to the client that the file has been received successfully
        exchange.sendResponseHeaders(200, 0)

        val responseStream = exchange.responseBody
        val fis = FileInputStream("$mainDirPath/a.zip")
        val bis = BufferedInputStream(fis)

        responseStream.write(bis.readBytes())

        bis.close()
        fis.close()
        responseStream.close()

        File("$mainDirPath/a.zip").delete()
    }
}
