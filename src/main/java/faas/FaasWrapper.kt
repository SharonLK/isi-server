/**
 * require access to sudo.
 * for easy access, you can permit use of sudo functions without password for the user.
 *
 * commands to run:
 * sudo adduser ThisUser sudo or in earlier versions of linux - sudo adduser ThisUser admins
 * then run the command sudo visudo and in the editors change the line thats start with $sudo with:
 * $sudo    ALL=NOPASSWD: ALL
 */

package faas

import java.io.BufferedReader
import java.io.InputStreamReader

fun main(args: Array<String>){
    var faasList = list()
    faasList.forEach {
        print("${it.functionName}\t${it.invocations}\t${it.replicas}\n")
    }
}

fun list(): List<FaasCliList>{
    var listString = runCommand("faas-cli list")
    var result : MutableList<FaasCliList> = mutableListOf()
    var lines = listString.lines()
    lines = lines.subList(1, lines.size-1)
    lines.forEach{
        var parm = it.split("\\s".toRegex())
        var actualString : MutableList<String> = mutableListOf()
        for(i in 0 until parm.size-1){
            if (parm[i] != " " && parm[i] != "") {
                actualString.add(parm[i])
            }
        }
        result.add(FaasCliList(actualString[0], Integer.parseInt(actualString[1]), Integer.parseInt(actualString[2])))
    }

    return result
}

fun remove(functionName : String){
    println(runCommand("faas-cli remove $functionName`"))

}

fun build(image : String, lang : String = "python3", name : String) {
    println(runCommand("sudo faas-cli build --image $image --lang $lang --name $name"))
}

fun deploy(image : String, lang : String = "python3", name : String) {
    println(runCommand("sudo faas-cli deploy --image $image --lang $lang --name $name"))
}

fun buildAndDeploy(image : String, lang : String = "python3", name : String) {
    build(image, lang, name)
    deploy(image, lang, name)
}


fun runCommand(command : String) : String {
    val rt = Runtime.getRuntime()
    val proc = rt.exec(command)

    val stdInput = BufferedReader(InputStreamReader(proc.inputStream))

    val stdError = BufferedReader(InputStreamReader(proc.errorStream))

    println("\nThe command is: $command\n")
// read the output from the command
    //println("\nHere is the standard output of the command:\n")
    var result = ""
    var s: String?
    s = stdInput.readLine()
    while (s != null) {
        result += s+"\n"
        //println(s)
        s = stdInput.readLine()
    }
    result = result.substring(0, result.length-2)

// read any errors from the attempted command
    //println("\nHere is the standard error of the command (if any):\n")
    s = stdError.readLine()
    while (s != null) {
        println(s)
        s = stdError.readLine()
    }

    return result
}
