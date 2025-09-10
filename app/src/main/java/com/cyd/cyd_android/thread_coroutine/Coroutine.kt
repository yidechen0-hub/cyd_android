package com.cyd.cyd_android.thread_coroutine
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

fun main1() = runBlocking {
    // launch: 没有返回值
    val job = launch(Dispatchers.Default) {
        delay(5*1000)
        println("successfully completed")
    }

    // async: 有返回值
    val result = async {
        delay(500)
        42
    }

//    job.join() // 等待 launch 完成
//    job.cancel() // 取消 launch
    println("result: ${result.await()}")

}



fun main2() {
    val threads = List(10_000) { i ->
        Thread {
            Thread.sleep(1000) // 模拟耗时任务
            if (i % 1000 == 0) {
                println("thread task $i completed")
            }
        }
    }

    threads.forEach { it.start() }
    threads.forEach { it.join() }

    println("all threads completed")
}




fun main3() = runBlocking {
    val jobs = List(10_000) { i ->
        launch {
            delay(1000) // 非阻塞挂起
            if (i % 1000 == 0) {
                println("coroutine task $i completed")
            }
        }
    }

    jobs.forEach { it.join() }
    println("all coroutines completed")
}


fun main4() {
    val threads = List(10_000) { i ->
        Thread {
            var sum = 0L
            repeat(100_000) { sum += it } // 模拟 CPU 计算
            if (i % 1000 == 0) {
                println("thread task $i completed, sum=$sum")
            }
        }
    }

    val start = System.currentTimeMillis()
    threads.forEach { it.start() }
    threads.forEach { it.join() }
    val time = System.currentTimeMillis() - start

    println("all threads completed, time: ${time}ms")
}





fun main5() = runBlocking {
    val jobs = List(10_000) { i ->
        launch(Dispatchers.Default) { // CPU 密集型调度器
            var sum = 0L
            repeat(100_000) { sum += it }
            if (i % 1000 == 0) {
                println("coroutine task $i completed, sum=$sum")
            }
        }
    }

    val start = System.currentTimeMillis()
    jobs.forEach { it.join() }
    val time = System.currentTimeMillis() - start

    println("all coroutines completed, time: ${time}ms")
}


fun main() = runBlocking {
    val ticketChannel = Channel<String>(capacity = 5) // 最多同时存 5 张票

    // 生产者：出票
    val producer = launch {
        repeat(10) { i ->
            val ticket = "ticket-$i"
            delay(200) // 模拟出票耗时
            ticketChannel.send(ticket) // 放入票池
            println(" sold:$ticket")
        }
        ticketChannel.close() // 出票结束
    }

    // 消费者：买票
    val consumers = List(3) { id ->
        launch {
            for (ticket in ticketChannel) {
                println("passenger $id buyed ticket $ticket ✅")
                delay(500) // 模拟乘客付款/确认
            }
            println("passenger $id cannt buy ticket,over")
        }
    }

    producer.join()
    consumers.forEach { it.join() }
    println(" all tickets sold out")
}