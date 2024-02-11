package com.mvv.demo2.app

//import lombok.SneakyThrows
import org.testcontainers.containers.ContainerLaunchException
import org.testcontainers.containers.output.FrameConsumerResultCallback
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.containers.output.WaitingConsumer
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy
//import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Predicate



class LogMessageWaitStrategy2 (val regEx: String, val predicate: (String)->Boolean) : AbstractWaitStrategy() {

    private var times = 1

    //@SneakyThrows(IOException::class)
    override fun waitUntilReady() {
        val waitingConsumer = WaitingConsumer()

        val cmd = waitStrategyTarget
            .dockerClient
            .logContainerCmd(waitStrategyTarget.containerId)
            .withFollowStream(true)
            //.withFollowStream(false)
            .withSince(0)
            .withStdOut(true)
            .withStdErr(true)

        FrameConsumerResultCallback().use { callback ->
            callback.addConsumer(OutputFrame.OutputType.STDOUT, waitingConsumer)
            callback.addConsumer(OutputFrame.OutputType.STDERR, waitingConsumer)

            cmd.exec(callback)

            val waitPredicate =
                Predicate { outputFrame: OutputFrame ->
                    //outputFrame.utf8String.matches("(?s)$regEx".toRegex())
                    val s = outputFrame.utf8String
                    //println("  #### [$s]")
                    print("  #### $s")
                    val r1 = predicate(s)
                    val r = s.matches("(?s)$regEx".toRegex())
                    if (r) println("    !!! #### [$s]")
                    r || r1
                }
            try {
                waitingConsumer.waitUntil(
                    waitPredicate,
                    startupTimeout.seconds,
                    TimeUnit.SECONDS,
                    times
                )
            } catch (e: TimeoutException) {
                throw ContainerLaunchException("Timed out waiting for log output matching '$regEx'")
            }
        }
    }

    //fun withRegEx(regEx: String?): LogMessageWaitStrategy2 {
    //    this.regEx = regEx
    //    return this
    //}

    fun withTimes(times: Int): LogMessageWaitStrategy2 {
        this.times = times
        return this
    }
}

