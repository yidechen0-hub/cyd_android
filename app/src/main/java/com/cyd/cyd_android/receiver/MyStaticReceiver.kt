package com.cyd.cyd_android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyStaticReceiver : BroadcastReceiver() {
    private val TAG = "MyStaticReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val action = it.action
            val data = it.getStringExtra("data") ?: "无数据"
            Log.d(TAG, "接收到静态广播 -> Action: $action, 数据: $data")
        }
    }
}