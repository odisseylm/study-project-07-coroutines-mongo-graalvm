package com.mvv.gradle.util

import java.io.ByteArrayOutputStream


private fun Iterable<String>.toCommandLine(): org.apache.commons.exec.CommandLine =
    org.apache.commons.exec.CommandLine(this.first())
        .also { cl -> this.asSequence().drop(1).forEach { cl.addArgument(it) } }

fun commandLine(vararg args: String) = args.asIterable().toCommandLine()


private fun executeAndReturnContent(vararg args: String): String {
    val executor = org.apache.commons.exec.DefaultExecutor()

    val contentStream = ByteArrayOutputStream()
    executor.streamHandler = org.apache.commons.exec.PumpStreamHandler(contentStream, System.err)

    val res: Int = executor.execute(commandLine(*args))
    if (res != 0) throw IllegalStateException(args.joinToString(" ") + " failed with code $res.")

    return contentStream.toString(Charsets.UTF_8)
}

fun getProcessCommandLine(pid: Long): String {
    val isLinux = org.apache.commons.lang3.SystemUtils.IS_OS_LINUX
    return if (isLinux) {
        val res = executeAndReturnContent("cat", "/proc/${pid}/cmdline")
        val cmdLine: String = res.toCharArray().map { ch -> if (ch == 0.toChar()) ' ' else ch }.joinToString("")
        cmdLine
    } else {
        ProcessHandle.of(pid).flatMap { it.info().commandLine() }.orElse("")
    }
}
