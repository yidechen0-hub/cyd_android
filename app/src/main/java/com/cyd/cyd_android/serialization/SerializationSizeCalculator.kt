package com.cyd.cyd_android.serialization

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import java.nio.charset.StandardCharsets

object SerializationSizeCalculator {
    private val gson = Gson()

    /**
     * 计算 JSON 序列化后的字节大小
     */
    fun calculateJsonSize(data: Any): Int {
        return try {
            // 序列化为JSON字符串，再转换为字节数组计算大小
            val json = gson.toJson(data)
            json.toByteArray(StandardCharsets.UTF_8).size
        } catch (e: Exception) {
            e.printStackTrace()
            -1 // 失败返回-1
        }
    }

    /**
     * 计算 Parcelable 序列化后的字节大小
     */
    fun calculateParcelableSize(parcelable: Parcelable): Int {
        return try {
            // 创建临时Parcel对象写入数据
            val parcel = Parcel.obtain()
            parcelable.writeToParcel(parcel, 0)
            // 获取写入的字节大小
            val size = parcel.dataSize()
            parcel.recycle() // 回收Parcel，避免内存泄漏
            size
        } catch (e: Exception) {
            e.printStackTrace()
            -1 // 失败返回-1
        }
    }

    /**
     * 计算 Protobuf 序列化后的字节大小
     * 注意：T需要是Protobuf生成的消息类（继承自GeneratedMessageLite）
     */
    fun <T : com.google.protobuf.GeneratedMessageLite<*, *>> calculateProtobufSize(protoData: T): Int {
        return try {
            // Protobuf直接提供toByteArray()方法
            protoData.toByteArray().size
        } catch (e: Exception) {
            e.printStackTrace()
            -1 // 失败返回-1
        }
    }
}
