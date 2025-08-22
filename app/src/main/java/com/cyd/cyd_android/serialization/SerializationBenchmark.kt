package com.cyd.cyd_android.serialization


import android.content.Intent
import android.os.*
import android.util.Log
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import com.google.protobuf.GeneratedMessageLite
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.OutputStreamWriter
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

class SerializationBenchmark {
    private val TAG = "SerializationBenchmark"
    val gson = Gson()
    private val results = mutableListOf<BenchmarkResult>()
    
    // 每种测试的重复次数，取平均值
    private val TEST_REPEATS = 5

    private var actualSize = 0
    data class BenchmarkResult(
        val dataSize: String,
        val dataSizeBytes: Int,
        val actualySize: Int,
        val method: String,
        val serializeTimeMs: Long,
        val deserializeTimeMs: Long,
        val serializeMemoryKb: Long,
        val deserializeMemoryKb: Long,
        val serializedSizeKb: Long
    )
    
    fun runAllBenchmarks() {
        Log.d(TAG, "开始执行所有序列化性能测试...")
        
        // 测试所有数据大小档位
        for (size in DataGenerator.DataSize.values()) {
            Log.d(TAG, "开始测试数据大小: ${formatSize(size.sizeInBytes.toLong())}")
            
            // 生成测试数据
            val testData = DataGenerator.generateComplexData(size)

            // 验证数据大小
//            val actualSize = estimateObjectSize(testData)
            actualSize = getParcelableSize(testData)
            Log.d(TAG, "实际数据大小: ${formatSize(actualSize.toLong())}}")
            
            // 执行各种序列化方法的测试
//            testGsonSerialization(testData, size)
//            testParcelableSerialization(testData, size)
            testProtobufSerialization(testData, size)
        }
        
        // 打印汇总结果
        printResultsSummary()
    }
    
    private fun testGsonSerialization(data: ComplexData, size: DataGenerator.DataSize) {
        var totalSerializeTime = 0L
        var totalDeserializeTime = 0L
        var totalSerializeMemory = 0L
        var totalDeserializeMemory = 0L
        var serializedSize = 0L
        
        for (i in 0 until TEST_REPEATS) {
            // 测试序列化
            val (json, serializeTime, serializeMemory) = measureGsonSerialization(data)
//            val (serializeTime, serializeMemory) = measureGsonSerialization(data)
            // 测试反序列化
            val (deserializedData, deserializeTime, deserializeMemory) = measureGsonDeserialization(json)
            
            // 验证数据一致性
            if (!verifyDataEquality(data, deserializedData)) {
                Log.e(TAG, "Gson 序列化/反序列化后数据不一致!")
            }
            
            // 累加统计
            totalSerializeTime += serializeTime
            totalDeserializeTime += deserializeTime
            totalSerializeMemory += serializeMemory
            totalDeserializeMemory += deserializeMemory
            
            if (i == 0) {
                serializedSize = json.toByteArray().size.toLong()

                Log.d(TAG, "json序列化后的数据大小: ${formatSize(serializedSize)})}")
            }
        }
        
        // 计算平均值
        val avgSerializeTime = totalSerializeTime / TEST_REPEATS
        val avgDeserializeTime = totalDeserializeTime / TEST_REPEATS
        val avgSerializeMemory = totalSerializeMemory / TEST_REPEATS
        val avgDeserializeMemory = totalDeserializeMemory / TEST_REPEATS
        
        // 保存结果
        results.add(
            BenchmarkResult(
                dataSize = formatSize(size.sizeInBytes.toLong()),
                dataSizeBytes = size.sizeInBytes,
                actualySize = actualSize,
                method = "Gson",
                serializeTimeMs = avgSerializeTime,
                deserializeTimeMs = avgDeserializeTime,
                serializeMemoryKb = avgSerializeMemory / 1024,
                deserializeMemoryKb = avgDeserializeMemory / 1024,
                serializedSizeKb = serializedSize / 1024
            )
        )
        
        Log.d(TAG, "Gson 测试完成 - 序列化: ${avgSerializeTime}ms, 反序列化: ${avgDeserializeTime}ms")
    }
    
    private fun testParcelableSerialization(data: ComplexData, size: DataGenerator.DataSize) {
        var totalSerializeTime = 0L
        var totalDeserializeTime = 0L
        var totalSerializeMemory = 0L
        var totalDeserializeMemory = 0L
        var serializedSize = 0L
        
        for (i in 0 until TEST_REPEATS) {
            // 测试序列化
            val (parcelData, serializeTime, serializeMemory) = measureParcelableSerialization(data)
            
            // 测试反序列化
            val (deserializedData, deserializeTime, deserializeMemory) = measureParcelableDeserialization(parcelData)
            
            // 验证数据一致性
            if (data != deserializedData) {
                Log.e(TAG, "Parcelable 序列化/反序列化后数据不一致!")
            }
            
            // 累加统计
            totalSerializeTime += serializeTime
            totalDeserializeTime += deserializeTime
            totalSerializeMemory += serializeMemory
            totalDeserializeMemory += deserializeMemory
            
            if (i == 0) {
                serializedSize = parcelData.size.toLong()
                Log.d(TAG, "Parcelable序列化后的数据大小: ${formatSize(serializedSize)})}")
            }
        }
        
        // 计算平均值
        val avgSerializeTime = totalSerializeTime / TEST_REPEATS
        val avgDeserializeTime = totalDeserializeTime / TEST_REPEATS
        val avgSerializeMemory = totalSerializeMemory / TEST_REPEATS
        val avgDeserializeMemory = totalDeserializeMemory / TEST_REPEATS
        
        // 保存结果
        results.add(
            BenchmarkResult(
                dataSize = formatSize(size.sizeInBytes.toLong()),
                dataSizeBytes = size.sizeInBytes,
                actualySize = actualSize,
                method = "Parcelable",
                serializeTimeMs = avgSerializeTime,
                deserializeTimeMs = avgDeserializeTime,
                serializeMemoryKb = avgSerializeMemory / 1024,
                deserializeMemoryKb = avgDeserializeMemory / 1024,
                serializedSizeKb = serializedSize / 1024
            )
        )
        
        Log.d(TAG, "Parcelable 测试完成 - 序列化: ${avgSerializeTime}ms, 反序列化: ${avgDeserializeTime}ms")
    }
    
    private fun testProtobufSerialization(data: ComplexData, size: DataGenerator.DataSize) {
        // 转换为Proto对象
        val protoData = DataGenerator.convertToProto(data)
        
        var totalSerializeTime = 0L
        var totalDeserializeTime = 0L
        var totalSerializeMemory = 0L
        var totalDeserializeMemory = 0L
        var serializedSize = 0L
        
        for (i in 0 until TEST_REPEATS) {
            // 测试序列化
            val (bytes, serializeTime, serializeMemory) = measureProtobufSerialization(protoData)
            
            // 测试反序列化
            val (deserializedProto, deserializeTime, deserializeMemory) = measureProtobufDeserialization(bytes)
            val deserializedData = DataGenerator.convertFromProto(deserializedProto)
            
            // 验证数据一致性
            if (!verifyDataEquality(data, deserializedData)) {
                Log.e(TAG, "Protobuf 序列化/反序列化后数据不一致!")
            }
            
            // 累加统计
            totalSerializeTime += serializeTime
            totalDeserializeTime += deserializeTime
            totalSerializeMemory += serializeMemory
            totalDeserializeMemory += deserializeMemory
            
            if (i == 0) {
                serializedSize = bytes.size.toLong()
                Log.d(TAG, "Protobuf序列化后的数据大小: ${formatSize(serializedSize)})}")
            }
        }
        
        // 计算平均值
        val avgSerializeTime = totalSerializeTime / TEST_REPEATS
        val avgDeserializeTime = totalDeserializeTime / TEST_REPEATS
        val avgSerializeMemory = totalSerializeMemory / TEST_REPEATS
        val avgDeserializeMemory = totalDeserializeMemory / TEST_REPEATS
        
        // 保存结果
        results.add(
            BenchmarkResult(
                dataSize = formatSize(size.sizeInBytes.toLong()),
                dataSizeBytes = size.sizeInBytes,
                actualySize = actualSize,
                method = "Protobuf",
                serializeTimeMs = avgSerializeTime,
                deserializeTimeMs = avgDeserializeTime,
                serializeMemoryKb = avgSerializeMemory / 1024,
                deserializeMemoryKb = avgDeserializeMemory / 1024,
                serializedSizeKb = serializedSize / 1024
            )
        )
        
        Log.d(TAG, "Protobuf 测试完成 - 序列化: ${avgSerializeTime}ms, 反序列化: ${avgDeserializeTime}ms")
    }
    
    // Gson序列化测量
    private fun measureGsonSerialization(data: ComplexData): Triple<String, Long, Long> {
        // 测量内存使用
        val runtime = Runtime.getRuntime()
        runtime.gc()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()

        // 测量时间
        val startTime = System.nanoTime()
        val json = gson.toJson(data)
        val endTime = System.nanoTime()


        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore

        val timeTakenMs = (endTime - startTime) / 1_000_000

        return Triple(json, timeTakenMs, memoryUsed)

    }
    
    // Gson反序列化测量
    private fun measureGsonDeserialization(json: String): Triple<ComplexData, Long, Long> {
        // 测量内存使用
        val runtime = Runtime.getRuntime()
        runtime.gc()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        // 测量时间
        val startTime = System.nanoTime()
        val data = gson.fromJson(json, ComplexData::class.java)
        val endTime = System.nanoTime()
        
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore
        
        val timeTakenMs = (endTime - startTime) / 1_000_000
        
        return Triple(data, timeTakenMs, memoryUsed)
    }
    
    // Parcelable序列化测量
    private fun measureParcelableSerialization(data: ComplexData): Triple<ByteArray, Long, Long> {
        // 测量内存使用
        val runtime = Runtime.getRuntime()
        runtime.gc()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        // 测量时间
        val startTime = System.nanoTime()
        val parcel = Parcel.obtain()
        parcel.writeParcelable(data, 0)
        val bytes = parcel.marshall()
        parcel.recycle()
        val endTime = System.nanoTime()
        
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore
        
        val timeTakenMs = (endTime - startTime) / 1_000_000
        
        return Triple(bytes, timeTakenMs, memoryUsed)
    }
    
    // Parcelable反序列化测量
    private fun measureParcelableDeserialization(data: ByteArray): Triple<ComplexData, Long, Long> {
        // 测量内存使用
        val runtime = Runtime.getRuntime()
        runtime.gc()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        // 测量时间
        val startTime = System.nanoTime()
        val parcel = Parcel.obtain()
        parcel.unmarshall(data, 0, data.size)
        parcel.setDataPosition(0)
        val deserializedData = parcel.readParcelable<ComplexData>(ComplexData::class.java.classLoader)
        parcel.recycle()
        val endTime = System.nanoTime()
        
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore
        
        val timeTakenMs = (endTime - startTime) / 1_000_000
        
        return Triple(deserializedData!!, timeTakenMs, memoryUsed)
    }
    
    // Protobuf序列化测量
    private fun measureProtobufSerialization(data: ComplexDataProtoClass.ComplexDataProto): Triple<ByteArray, Long, Long> {
        // 测量内存使用
        val runtime = Runtime.getRuntime()
        runtime.gc()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        // 测量时间
        val startTime = System.nanoTime()
        val bytes = data.toByteArray()
        val endTime = System.nanoTime()
        
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore
        
        val timeTakenMs = (endTime - startTime) / 1_000_000
        
        return Triple(bytes, timeTakenMs, memoryUsed)
    }
    
    // Protobuf反序列化测量
    private fun measureProtobufDeserialization(data: ByteArray): Triple<ComplexDataProtoClass.ComplexDataProto, Long, Long> {
        // 测量内存使用
        val runtime = Runtime.getRuntime()
        runtime.gc()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        // 测量时间
        val startTime = System.nanoTime()
        val protoData = ComplexDataProtoClass.ComplexDataProto.parseFrom(data)
        val endTime = System.nanoTime()
        
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore
        
        val timeTakenMs = (endTime - startTime) / 1_000_000
        
        return Triple(protoData, timeTakenMs, memoryUsed)
    }
    
    // 验证两个数据对象是否相等
    private fun verifyDataEquality(original: ComplexData, deserialized: ComplexData): Boolean {
        if (original.id != deserialized.id) return false
        if (original.name != deserialized.name) return false
        if (original.timestamp != deserialized.timestamp) return false
        if (original.isValid != deserialized.isValid) return false
        if (!original.byteData.contentEquals(deserialized.byteData)) return false
        if (original.stringList != deserialized.stringList) return false
        if (original.nestedData != deserialized.nestedData) return false
        
        return true
    }
    
    // 估算对象大小（字节）
    private fun estimateObjectSize(obj: Any): Int  {
        return try {
            val byteOut = ByteArrayOutputStream()
            val objOut = ObjectOutputStream(byteOut)
            objOut.writeObject(obj)
            objOut.close()
            byteOut.size()
        } catch (e: Exception) {
            Log.e(TAG, "估算对象大小失败", e)
            0
        }
    }
    fun getParcelableSize(parcelable: Parcelable): Int {
        // 获取一个可复用的 Parcel 实例（比 new Parcel() 更高效）
        val parcel = Parcel.obtain()
        return try {
            // 将对象写入 Parcel
            parcel.writeParcelable(parcelable, 0)
            // 获取已写入数据的大小（字节数）
            parcel.dataSize()
        } catch (e: Exception) {
            // 处理可能的异常（如写入过程出错）
            e.printStackTrace()
            0
        } finally {
            // 回收 Parcel 实例，避免资源泄漏
            parcel.recycle()
        }
    }
    
    // 格式化大小显示
    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            "%.2f %s",
            bytes / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }
    
    // 打印结果汇总
    private fun printResultsSummary() {
        Log.d(TAG, "\n\n===== 序列化性能测试结果汇总 =====")
        Log.d(TAG, "数据大小 |  实际大小 | 方法 | 序列化时间(ms) | 反序列化时间(ms) | 序列化内存(KB) | 反序列化内存(KB) | 序列化后大小(KB)")
        
        results.sortedBy { it.dataSizeBytes }.forEach { result ->
            Log.d(
                TAG,
                "${result.dataSize.padEnd(8)} |  ${result.actualySize.toString().padEnd(8)}   |  ${result.method.padEnd(8)} | ${result.serializeTimeMs.toString().padEnd(16)} | ${result.deserializeTimeMs.toString().padEnd(18)} | ${result.serializeMemoryKb.toString().padEnd(16)} | ${result.deserializeMemoryKb.toString().padEnd(18)} | ${result.serializedSizeKb}"
            )
        }
    }
}
