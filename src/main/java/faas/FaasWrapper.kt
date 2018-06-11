/**
 * Require access to sudo.
 * for easy access, you can permit use of sudo functions without password for the user.
 *
 * commands to run:
 * sudo adduser ThisUser sudo or in earlier versions of linux - sudo adduser ThisUser admins
 * then run the command sudo visudo and in the editors change the line thats start with $sudo with:
 *
 *  $sudo    ALL=NOPASSWD: ALL
 */

package faas

import java.io.BufferedReader
import java.io.InputStreamReader

fun main(args: Array<String>) {
    val faasList = list()
    faasList.forEach {
        //print("${it.functionName}\t${it.invocations}\t${it.replicas}\n")
    }
    val url = deploy("test", "python3" , "testing",
            "/home/iron-faas/projects/faas-cli/sample/tests/handler.py")
    println(url.toString())
}

data class FaasCliList (val functionName : String, val invocations : Int, val replicas : Int)

data class FaasCliDeploy (val url: String?)

fun list(): List<FaasCliList> {
    val listString = runCommand("faas-cli list")
    val result: MutableList<FaasCliList> = ArrayList()
    var lines = listString.lines()
    lines = lines.subList(1, lines.size)

    lines.forEach {
        val split = it.split("\\s+".toRegex())
        result.add(FaasCliList(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2])))
    }

    return result
}

fun remove(functionName: String) {
    println(runCommand("sudo faas-cli remove $functionName"))
}

fun build(image : String, lang : String = "python3", name : String, handler : String) {
    println(runCommand("sudo faas-cli build --image $image --lang $lang --name $name --handler $handler"))
}

fun deploy(image : String, lang : String = "python3", name : String, handler : String) : FaasCliDeploy{
    var deploy : String = runCommand("sudo faas-cli deploy --image $image --lang $lang --name $name" +
            " --handler $handler")
    val indexOfUrl = deploy.indexOf("URL:")
    if(indexOfUrl != -1 && indexOfUrl+4 < deploy.length-1)  {
        return FaasCliDeploy(deploy.substring(indexOfUrl+5))
    }
    return FaasCliDeploy(null)
}

fun buildAndDeploy(image: String, lang: String = "python3", name: String, handler : String) : FaasCliDeploy {
    build(image, lang, name, handler)
    return deploy(image, lang, name, handler)
}


fun runCommand(command: String): String {
    val rt = Runtime.getRuntime()
    val proc = rt.exec(command)

    val stdInput = BufferedReader(InputStreamReader(proc.inputStream))
    val stdError = BufferedReader(InputStreamReader(proc.errorStream))

    println("\nThe command is: $command\n")

    // Read the output from the command
    var result = ""
    var line: String?
    line = stdInput.readLine()
    while (line != null) {
        result += "$line\n"
        line = stdInput.readLine()
    }

    result = result.substring(0, result.length - 2)

    // Read any errors from the attempted command
    line = stdError.readLine()
    while (line != null) {
        println(line)
        line = stdError.readLine()
    }

    return result
}
