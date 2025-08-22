package com.cyd.cyd_android.serialization


import java.security.SecureRandom
import java.util.*
import kotlin.math.pow

object DataGenerator {
    private val random = SecureRandom()
    private val stringChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray()
    
    // 数据大小档位 (字节)
    enum class DataSize(val sizeInBytes: Int) {
        SIZE_1KB(1024),
        SIZE_100KB(100 * 1024),
        SIZE_1MB(1024 * 1024),
        SIZE_2MB(2 * 1024 * 1024),
        SIZE_5MB(5 * 1024 * 1024),
        SIZE_10MB(10 * 1024 * 1024),
        SIZE_50MB(50 * 1024 * 1024)
    }

    // 格式化字节数为可读形式
    fun formatSize(bytes: Int): String {
        if (bytes < 0) return "Invalid size"

        // 定义单位数组：B、KB、MB、GB
        val units = arrayOf("B", "KB", "MB", "GB")
        // 计算单位索引（每级相差1024倍）
        val unitIndex = when {
            bytes < 1024 -> 0
            bytes < 1024 * 1024 -> 1
            bytes < 1024 * 1024 * 1024 -> 2
            else -> 3
        }

        // 计算转换后的值（保留两位小数）
        val value = bytes.toDouble() / (1024.0.pow(unitIndex))
        return String.format("%.2f %s", value, units[unitIndex])
    }
    
    fun generateComplexData(size: DataSize): ComplexData {
        // 基础数据固定大小
        val baseSize = estimateBaseSize()
        val remainingSize = size.sizeInBytes - baseSize
        
        // 根据剩余大小计算需要的字节数组大小
        val byteArraySize = (remainingSize * 0.7).toInt().coerceAtLeast(100) // 70%分配给字节数组
        
        // 生成字节数组
        val byteData = ByteArray(byteArraySize)
        random.nextBytes(byteData)
        
        // 剩余大小分配给字符串列表
        val stringListSize = remainingSize - byteArraySize
        val avgStringLength = 50
        val stringCount = (stringListSize / avgStringLength).coerceAtLeast(5) // 至少5个字符串
        
        // 生成字符串列表
        val stringList = List(stringCount) {
            generateRandomString(avgStringLength)
        }
        
        // 生成嵌套数据
        val nestedDataCount = (stringCount / 10).coerceAtLeast(2) // 至少2个嵌套对象
        val nestedData = List(nestedDataCount) {
            generateNestedData(it)
        }
        
        return ComplexData(
            id = random.nextLong(),
            name = generateRandomString(20),
            timestamp = System.currentTimeMillis(),
            isValid = random.nextBoolean(),
            byteData = byteData,
            stringList = stringList,
            nestedData = nestedData
        )
    }
    
    private fun generateNestedData(index: Int): ComplexData.NestedData {
        val valuesCount = 5 + random.nextInt(10)
        val values = List(valuesCount) { random.nextDouble() }
        
        return ComplexData.NestedData(
            nestedId = index,
            nestedName = generateRandomString(15),
            values = values,
            subNested = generateSubNestedData()
        )
    }
    
    private fun generateSubNestedData(): ComplexData.NestedData.SubNestedData {
        val numbersCount = 3 + random.nextInt(7)
        val numbers = List(numbersCount) { random.nextInt() }
        
        return ComplexData.NestedData.SubNestedData(
            flag = random.nextBoolean(),
            code = generateRandomString(8),
            numbers = numbers
        )
    }
    
    private fun generateRandomString(length: Int): String {
        val sb = StringBuilder(length)
        for (i in 0 until length) {
            sb.append(stringChars[random.nextInt(stringChars.size)])
        }
        return sb.toString()
    }
    
    // 估算基础数据结构的大小
    private fun estimateBaseSize(): Int {
        // 估算对象头和固定大小字段的大致内存占用
        return 200 // 字节 (粗略估计)
    }
    
    // 转换ComplexData到Proto对象
    fun convertToProto(data: ComplexData): ComplexDataProtoClass.ComplexDataProto {
        val builder = ComplexDataProtoClass.ComplexDataProto.newBuilder()
            .setId(data.id)
            .setName(data.name)
            .setTimestamp(data.timestamp)
            .setIsValid(data.isValid)
            .setByteData(com.google.protobuf.ByteString.copyFrom(data.byteData))
        
        data.stringList.forEach { builder.addStringList(it) }
        
        data.nestedData.forEach { nested ->
            val nestedBuilder = ComplexDataProtoClass.NestedDataProto.newBuilder()
                .setNestedId(nested.nestedId)
                .setNestedName(nested.nestedName)
            
            nested.values.forEach { nestedBuilder.addValues(it) }
            
            val subNested = nested.subNested
            val subNestedBuilder = ComplexDataProtoClass.SubNestedDataProto.newBuilder()
                .setFlag(subNested.flag)
                .setCode(subNested.code)
            
            subNested.numbers.forEach { subNestedBuilder.addNumbers(it) }
            
            nestedBuilder.subNested = subNestedBuilder.build()
            builder.addNestedData(nestedBuilder.build())
        }
        
        return builder.build()
    }
    
    // 转换Proto对象到ComplexData
    fun convertFromProto(proto: ComplexDataProtoClass.ComplexDataProto): ComplexData {
        val nestedData = proto.nestedDataList.map { nestedProto ->
            val subNested = nestedProto.subNested
            ComplexData.NestedData(
                nestedId = nestedProto.nestedId,
                nestedName = nestedProto.nestedName,
                values = nestedProto.valuesList,
                subNested = ComplexData.NestedData.SubNestedData(
                    flag = subNested.flag,
                    code = subNested.code,
                    numbers = subNested.numbersList
                )
            )
        }
        
        return ComplexData(
            id = proto.id,
            name = proto.name,
            timestamp = proto.timestamp,
            isValid = proto.isValid,
            byteData = proto.byteData.toByteArray(),
            stringList = proto.stringListList,
            nestedData = nestedData
        )
    }
}
