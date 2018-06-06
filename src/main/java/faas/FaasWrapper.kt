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
    list()
}

fun list(){
    runCommand("faas-cli list")
}

fun remove(functionName : String){
    runCommand("faas-cli remove $functionName`")
}

fun build(image : String, lang : String = "python3", name : String) {
    runCommand("sudo faas-cli build --image $image --lang $lang --name $name")
}

fun deploy(image : String, lang : String = "python3", name : String) {
    runCommand("sudo faas-cli deploy --image $image --lang $lang --name $name")
}

fun buildAndDeploy(image : String, lang : String = "python3", name : String) {
    build(image, lang, name)
    deploy(image, lang, name)
}


fun runCommand(command : String) : String? {
    val rt = Runtime.getRuntime()
    val proc = rt.exec(command)

    val stdInput = BufferedReader(InputStreamReader(proc.inputStream))

    val stdError = BufferedReader(InputStreamReader(proc.errorStream))

    println("\nThe command is: $command\n")
// read the output from the command
    println("\nHere is the standard output of the command:\n")
    var s: String? = null
    s = stdInput.readLine()
    while (s != null) {
        println(s)
        s = stdInput.readLine()
    }

// read any errors from the attempted command
    println("\nHere is the standard error of the command (if any):\n")
    s = stdError.readLine()
    while (s != null) {
        println(s)
        s = stdError.readLine()
    }

    return null
}
