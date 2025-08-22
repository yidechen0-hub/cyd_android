package com.cyd.cyd_android.serialization


import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import com.google.gson.Gson

class CrossProcessTester {
    private val TAG = "CrossProcessTester"
    private var messenger: Messenger? = null
    private var isBound = false
    private val results = mutableListOf<CrossProcessResult>()
    
    data class CrossProcessResult(
        val dataSize: String,
        val dataSizeBytes: Int,
        val method: String,
        val totalTimeMs: Long
    )
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            messenger = Messenger(service)
            isBound = true
            Log.d(TAG, "服务已连接，开始跨进程测试")
        }
        
        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            messenger = null
            Log.d(TAG, "服务已断开连接")
        }
    }
    
    fun bindService(context: android.content.Context) {
        val intent = Intent(context, TestService::class.java)
        context.bindService(intent, serviceConnection, android.content.Context.BIND_AUTO_CREATE)
    }
    
    fun unbindService(context: android.content.Context) {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }
    
    fun runCrossProcessTests() {
        if (!isBound || messenger == null) {
            Log.e(TAG, "服务未连接，无法进行测试")
            return
        }
        
        Log.d(TAG, "开始执行跨进程通信测试...")
        
        // 测试所有数据大小档位
        for (size in DataGenerator.DataSize.values()) {
            Log.d(TAG, "开始跨进程测试数据大小: ${DataGenerator.formatSize(size.sizeInBytes)}")
            
            // 生成测试数据
            val testData = DataGenerator.generateComplexData(size)
            
            // 执行各种序列化方法的跨进程测试
            testGsonCrossProcess(testData, size)
            testParcelableCrossProcess(testData, size)
            testProtobufCrossProcess(testData, size)
        }
        
        // 打印汇总结果
        printCrossProcessResults()
    }
    
    private fun testGsonCrossProcess(data: ComplexData, size: DataGenerator.DataSize) {
        val gson = Gson()
        val json = gson.toJson(data)
        var startTime = 0L
        
        val handler = Handler(Looper.getMainLooper()) { msg ->
            when (msg.what) {
                TestService.MSG_SERIALIZE_GSON -> {
                    val deserializeTime = msg.data.getLong(TestService.EXTRA_RESULT)
                    val totalTime = System.currentTimeMillis() - startTime
                    Log.d(
                        TAG,
                        "Gson 跨进程测试完成 - 总时间: ${totalTime}ms, 远程反序列化时间: ${deserializeTime}ms"
                    )
                    
                    results.add(
                        CrossProcessResult(
                            dataSize = DataGenerator.formatSize(size.sizeInBytes),
                            dataSizeBytes = size.sizeInBytes,
                            method = "Gson",
                            totalTimeMs = totalTime
                        )
                    )
                    true
                }
                else -> false
            }
        }
        
        val replyMessenger = Messenger(handler)

        
        try {
            val msg = Message.obtain(null, TestService.MSG_SERIALIZE_GSON)
            val bundle = Bundle()
            bundle.putString(TestService.EXTRA_DATA, json)
            msg.data = bundle
            msg.replyTo = replyMessenger
            
            startTime = System.currentTimeMillis()
            messenger?.send(msg)
        } catch (e: RemoteException) {
            Log.e(TAG, "Gson 跨进程测试失败", e)
        }
    }
    
    private fun testParcelableCrossProcess(data: ComplexData, size: DataGenerator.DataSize) {
        var startTime = 0L
        val handler = Handler(Looper.getMainLooper()) { msg ->
            when (msg.what) {
                TestService.MSG_SERIALIZE_PARCELABLE -> {
                    val totalTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "Parcelable 跨进程测试完成 - 总时间: ${totalTime}ms")
                    
                    results.add(
                        CrossProcessResult(
                            dataSize = DataGenerator.formatSize(size.sizeInBytes),
                            dataSizeBytes = size.sizeInBytes,
                            method = "Parcelable",
                            totalTimeMs = totalTime
                        )
                    )
                    true
                }
                else -> false
            }
        }
        
        val replyMessenger = Messenger(handler)

        
        try {
            val msg = Message.obtain(null, TestService.MSG_SERIALIZE_PARCELABLE)
            val bundle = Bundle()
            bundle.putParcelable(TestService.EXTRA_DATA, data)
            msg.data = bundle
            msg.replyTo = replyMessenger
            
            startTime = System.currentTimeMillis()
            messenger?.send(msg)
        } catch (e: RemoteException) {
            Log.e(TAG, "Parcelable 跨进程测试失败", e)
        }
    }
    
    private fun testProtobufCrossProcess(data: ComplexData, size: DataGenerator.DataSize) {
        val protoData = DataGenerator.convertToProto(data)
        val bytes = protoData.toByteArray()
        var startTime = 0L

        
        val handler = Handler(Looper.getMainLooper()) { msg ->
            when (msg.what) {
                TestService.MSG_SERIALIZE_PROTOBUF -> {
                    val deserializeTime = msg.data.getLong(TestService.EXTRA_RESULT)
                    val totalTime = System.currentTimeMillis() - startTime
                    Log.d(
                        TAG,
                        "Protobuf 跨进程测试完成 - 总时间: ${totalTime}ms, 远程反序列化时间: ${deserializeTime}ms"
                    )
                    
                    results.add(
                        CrossProcessResult(
                            dataSize = DataGenerator.formatSize(size.sizeInBytes),
                            dataSizeBytes = size.sizeInBytes,
                            method = "Protobuf",
                            totalTimeMs = totalTime
                        )
                    )
                    true
                }
                else -> false
            }
        }
        
        val replyMessenger = Messenger(handler)

        
        try {
            val msg = Message.obtain(null, TestService.MSG_SERIALIZE_PROTOBUF)
            val bundle = Bundle()
            bundle.putByteArray(TestService.EXTRA_DATA, bytes)
            msg.data = bundle
            msg.replyTo = replyMessenger
            
            startTime = System.currentTimeMillis()
            messenger?.send(msg)
        } catch (e: RemoteException) {
            Log.e(TAG, "Protobuf 跨进程测试失败", e)
        }
    }
    
    private fun printCrossProcessResults() {
        Log.d(TAG, "\n\n===== 跨进程通信测试结果汇总 =====")
        Log.d(TAG, "数据大小 | 方法 | 总通信时间(ms)")
        
        results.sortedBy { it.dataSizeBytes }.forEach { result ->
            Log.d(
                TAG,
                "${result.dataSize.padEnd(8)} | ${result.method.padEnd(8)} | ${result.totalTimeMs}"
            )
        }
    }
}
