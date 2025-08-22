package com.cyd.cyd_android.serialization


import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log

class TestService : Service() {
    private val TAG = "TestService"
    private val MessengerHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            MSG_SERIALIZE_GSON -> handleGsonMessage(msg)
            MSG_SERIALIZE_PARCELABLE -> handleParcelableMessage(msg)
            MSG_SERIALIZE_PROTOBUF -> handleProtobufMessage(msg)
            else -> false
        }
    }
    
    private val messenger = Messenger(MessengerHandler)
    
    companion object {
        const val MSG_SERIALIZE_GSON = 1
        const val MSG_SERIALIZE_PARCELABLE = 2
        const val MSG_SERIALIZE_PROTOBUF = 3
        const val EXTRA_DATA = "extra_data"
        const val EXTRA_RESULT = "extra_result"
    }
    
    private fun handleGsonMessage(msg: Message): Boolean {
        val json = msg.data.getString(EXTRA_DATA) ?: return false
        val replyTo = msg.replyTo
        
        // 反序列化
        val startTime = System.nanoTime()
        val data = SerializationBenchmark().gson.fromJson(json, ComplexData::class.java)
        val endTime = System.nanoTime()
        val timeTakenMs = (endTime - startTime) / 1_000_000
        
        // 准备响应
        val replyMsg = Message.obtain(null, MSG_SERIALIZE_GSON)
        val bundle = Bundle()
        bundle.putLong(EXTRA_RESULT, timeTakenMs)
        replyMsg.data = bundle
        
        try {
            replyTo.send(replyMsg)
        } catch (e: RemoteException) {
            Log.e(TAG, "发送Gson响应失败", e)
        }
        
        return true
    }
    
    private fun handleParcelableMessage(msg: Message): Boolean {
        val data = msg.data.getParcelable<ComplexData>(EXTRA_DATA) ?: return false
        val replyTo = msg.replyTo
        
        // 简单处理表示已接收并反序列化
        val replyMsg = Message.obtain(null, MSG_SERIALIZE_PARCELABLE)
        val bundle = Bundle()
        bundle.putLong(EXTRA_RESULT, 0) // 这里可以测量反序列化时间
        replyMsg.data = bundle
        
        try {
            replyTo.send(replyMsg)
        } catch (e: RemoteException) {
            Log.e(TAG, "发送Parcelable响应失败", e)
        }
        
        return true
    }
    
    private fun handleProtobufMessage(msg: Message): Boolean {
        val bytes = msg.data.getByteArray(EXTRA_DATA) ?: return false
        val replyTo = msg.replyTo
        
        // 反序列化
        val startTime = System.nanoTime()
        val protoData = ComplexDataProtoClass.ComplexDataProto.parseFrom(bytes)
        val data = DataGenerator.convertFromProto(protoData)
        val endTime = System.nanoTime()
        val timeTakenMs = (endTime - startTime) / 1_000_000
        
        // 准备响应
        val replyMsg = Message.obtain(null, MSG_SERIALIZE_PROTOBUF)
        val bundle = Bundle()
        bundle.putLong(EXTRA_RESULT, timeTakenMs)
        replyMsg.data = bundle
        
        try {
            replyTo.send(replyMsg)
        } catch (e: RemoteException) {
            Log.e(TAG, "发送Protobuf响应失败", e)
        }
        
        return true
    }
    
    override fun onBind(intent: Intent): IBinder {
        return messenger.binder
    }
}
