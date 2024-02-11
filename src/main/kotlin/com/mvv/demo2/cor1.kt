@file:Suppress("unused", "SameParameterValue")

package com.mvv.demo2



import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.System.currentTimeMillis
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

//@main
fun main1() {
    GlobalScope.launch {
        delay(3000)
        println("Hello from Coroutine!")
    }
    println("Hello from Main Thread!")
    Thread.sleep(3500L)
}


fun main2() {
    GlobalScope.launch {
        val result: Deferred<Int> = async {
            computeResult()
        }
        println("Computed result: ${result.await()}")
    }
    Thread.sleep(2000L)
}

suspend fun computeResult(): Int {
    delay(1000L)
    return 42
}


fun main3() = runBlocking {
    launch {
        delay(3000)
        println("Hello from Coroutine!")
    }
    println("Hello from Main Thread!")
}


fun main4(): Unit = runBlocking {
    launch(Dispatchers.IO) {
        println("IO: ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Default) {
        println("Default: ${Thread.currentThread().name}")
    }
    // For UI
    //launch(Dispatchers.Main) {
    //    println("Main: ${Thread.currentThread().name}")
    //}
}


fun main5() = runBlocking {
    val job = GlobalScope.launch {
        println("Throwing exception from coroutine")
        throw IllegalArgumentException()
    }

    job.join()
    println("Joined failed job")

    val deferred = GlobalScope.async {
        println("Throwing exception from async")
        throw ArithmeticException()
        42
    }

    try {
        deferred.await()
        println("Unreached")
    } catch (e: ArithmeticException) {
        println("Caught ArithmeticException")
    }
}

private val threadName: String get() = Thread.currentThread().name

fun main6() = runBlocking {
    launch {
        delay(1000L)
        println("Task from runBlocking $threadName")
    }

    coroutineScope {
        launch {
            delay(2000L)
            println("Task from nested launch $threadName")
        }

        delay(500L)
        println("Task from coroutine scope $threadName")
    }

    println("Coroutine scope is over $threadName")
}


suspend fun doSomething1() {
    delay(1000L)
    println("Doing something")
}

fun test5748574895794() = runBlocking {
    delay(1000L)
    doSomething1()
}

suspend fun f101_01(): Int {
    delay(100)
    return 1
}
suspend fun f101_02(): Int {
    delay(100)
    return 2
}
suspend fun f101_03(): String {
    delay(100)
    return "3"
}
suspend fun f101_04(p1: Int, p2: Int, p3: String): String {
    delay(100)
    return " $p1 $p2 $p3 "
}
suspend fun f101_05(p0: Int): String {
    delay(100)
    return " $p0 " + f101_04(f101_01(), f101_02(), f101_03())
}
fun main7() = runBlocking {
    println(f101_05(852))
}



fun main8() = runBlocking {
    val parentJob = launch {
        val childJob = launch {
            while (true) {
                println("Child is running $threadName")
                delay(500L)
            }
        }
        delay(2000L)
        println("Cancelling child job $threadName")
        childJob.cancel()
    }
    parentJob.join()
}


fun main9() = runBlocking {
    val channel = Channel<Int>()
    launch {
        for (x in 1..5) channel.send(x * x)
        channel.close()
    }
    repeat(5) { println(channel.receive()) }
    println("Done!")
}


fun main10() = runBlocking {
    val channel = Channel<Int>()
    launch {
        for (x in 1..5) channel.send(x * x)
        delay(100)
        channel.close()
    }
    for (i in channel) { println(i) }
    println("Done!")
}


val fibonacciSeq: Sequence<Int> = sequence {
    var a = 0
    var b = 1

    yield(1)

    while (true) {
        yield(a + b)

        val tmp = a + b
        a = b
        b = tmp
    }
}

fun main11() {
    fibonacciSeq
        .take(10)
        .forEach { println(it) }
}


fun main12() = runBlocking {
    val counter = AtomicInteger(0)
    val numberOfCoroutines = 100_000
    //val numberOfCoroutines = 1000

    //when
    val jobs = List(numberOfCoroutines) {
        launch {
            delay(1L)
            counter.incrementAndGet()
            //println("$threadName")
        }
    }
    jobs.forEach { it.join() }

    println(numberOfCoroutines)
    //assertEquals(counter.get(), numberOfCoroutines)
    assert(counter.get() == numberOfCoroutines)
}


fun main13() = runBlocking {
    val delay = 1000L
    val time = measureTimeMillis {
        //given
        val one = async(Dispatchers.Default) { someExpensiveComputation(delay) }
        val two = async(Dispatchers.Default) { someExpensiveComputation(delay) }

        //println("result: $one $two (${one + two})")

        // TODO: avoid using manual
        //when
        runBlocking {
            one.await()
            two.await()
        }
    }

    println("time: $time, delay: $delay")
    //then
    //assertTrue(time < delay * 2)
    assert(time < delay * 2)
}

private fun someExpensiveComputation(delay: Long): Long {
    println("someExpensiveComputation $threadName")
    Thread.sleep(delay)
    return currentTimeMillis()
}


fun main14() {
    thread { println("thread name: $threadName") }
    //Thread.sleep(1000)
}


private fun numbers(): Flow<Int> = flow {
    for (i in 1..5) {
        delay(1000L)
        emit(i)
    }
}
fun main15() = runBlocking {
    launch {
        for (k in 1..5) {
            println("I'm not blocked $k")
            delay(1000)
        }
    }
    numbers().collect { value -> println(value) }
}


suspend fun performRequest(request: Int): String {
    delay(1000L)
    return "response $request"
}
fun main16() = runBlocking {
    val flow = (1..5).asFlow().onEach { delay(300L) }
    flow.debounce(500L)
        .map { request -> performRequest(request) }
        .collect { response -> println(response) }
}


fun main17FlowZip() = runBlocking {
    val nums = (1..5).asFlow()
    val strs = nums.map { performRequest(it) }
    nums.zip(strs) { a, b -> "$a -> $b" }
        .collect { println(it) }
}


fun main18() = runBlocking {

    println("main 0   $threadName")

    val v1: Deferred<Int> = async/*(Dispatchers.Default)*/ {
        println("v1 0   $threadName")
        delay(1000)
        println("v1 1   $threadName")
        f101_01()
    }
    val v2: Deferred<Int> = async/*(Dispatchers.Default)*/ {
        println("v2 1   $threadName")
        delay(3000)
        println("v2 2   $threadName")
        f101_02()
    }

    delay(2000)

    val result = v1.await() + v2.await()
    println("main end   $threadName")
    println("result: $result")
}
fun main19_2() = runBlocking {
    val v1: Int = f101_01()
    val v2: Int = f101_02()

    val dateFlow: Flow<Int> = flowOf(v1)
    val timeFlow = flowOf(v2)
    val zippedFlow: Pair<Int, Int> = dateFlow.zip(timeFlow) { date, time -> Pair(date, time) }.first()
}


suspend fun doSomething20() {
    for (i in 0..3000 step 250) {
        println("doSomething20 $i")
        delay(250)
    }
}
fun main20withTimeout() = runBlocking {
    try {
        withTimeout(1500) {
            doSomething20()
        }
    } catch (e: TimeoutCancellationException) {
        println("The task exceeded the timeout limit.")
    }
}


suspend fun doSomething21() {
    delay(1000L)
    throw Exception("Something went wrong.")
}
fun main21supervisor() = runBlocking {
    val supervisor = SupervisorJob()
    with(CoroutineScope(coroutineContext + supervisor)) {
    //with(CoroutineScope(coroutineContext)) {
        val child1 = launch {
            doSomething21()
        }
        val child2 = launch {
            delay(2000L)
            println("Coroutine 2 completed.")
        }
    }
    delay(3000L)
}


private var counter = 0
private val mutex = Mutex()
private suspend fun increment() {
    withContext(Dispatchers.Default) {
        repeat(1000) {
            println("increment in $threadName")
            mutex.withLock { counter++ }
        }
    }
}
fun main22() = runBlocking {
    val job1 = launch { increment() }
    val job2 = launch { increment() }
    job1.join()
    job2.join()
    println(counter)
}


private val sharedFlow = MutableSharedFlow<Int>()
suspend fun producer() {
    var counter = 0
    while (true) {
        delay(1000L)
        counter++
        sharedFlow.emit(counter)
    }
}
suspend fun consumer(id: Int) {
    sharedFlow.collect { value ->
        println("Consumer $id received $value")
    }
}
fun main23_1producer_nCosumers() = runBlocking {
    val job1 = launch { producer() }
    val job2 = launch { consumer(1) }
    val job3 = launch { consumer(2) }
    delay(5000L)
    job1.cancel()
    job2.cancel()
    job3.cancel()
}


//@OptIn
fun main24(): Unit = runBlocking {
    val result = flow {
        emit("a")
        delay(100)
        emit("b")
    }
        .onCompletion { println("onCompletion 1") }
        .mapLatest { value ->
            println("Started computing $value")
            delay(200)
            println("Computed $value")
            "Computed $value"
        }
        .onCompletion { println("onCompletion 2") }
        .collect()
        //.collect { println(it) }
        //.first()

    println("result: $result")
}


suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { 1 }
    val two = async { 2 }
    one.await() + two.await()
}


fun main25channelBackPressureTest(): Unit = runBlocking {
    val channel = Channel<Int>(10, BufferOverflow.SUSPEND)
    //val channel = Channel<Int>()

    val n = 50

    launch {
        for (x in 1..n) {
            println("sending $x")
            channel.send(x)
            delay(1)
        }
        channel.close()
    }
    repeat(n) {
        println("received ${channel.receive()}")
        delay(200)
    }
    println("Done!")
}

/*
TODO: impl window/buffer operator
//fun main26flowBackPressureTest(): Unit = runBlocking {
fun main(): Unit = runBlocking {

    val n = 100

    val flow = flow<Int> {
        for (x in 1..n) {
            println("sending $x")
            emit(x)
            delay(1)
        }
    }

    flow.cache()

    flow
        .buffer(10)
        .onEach { delay(50) }
        .collect { println("received $it") }

    println("Done!")
}
*/

