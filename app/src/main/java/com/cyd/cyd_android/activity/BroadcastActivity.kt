package com.cyd.cyd_android.activity

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cyd.cyd_android.receiver.MyDynamicReceiver
import com.cyd.cyd_android.receiver.MyStaticReceiver
import com.cyd.cyd_android.R

class BroadcastActivity: AppCompatActivity() {
    // 定义广播Action
    private val STATIC_ACTION = "com.example.broadcast.STATIC_ACTION"
    private val DYNAMIC_ACTION = "com.example.broadcast.DYNAMIC_ACTION"


    // 静态接收器实例
    private val staticReceiver = MyStaticReceiver()
    // 动态接收器实例
    private lateinit var dynamicReceiver: MyDynamicReceiver
    private lateinit var statusText: TextView
//    private lateinit var packageNameText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast)
        statusText = findViewById(R.id.tv_status)
//        packageNameText = findViewById(R.id.tv_packageName)
        // 初始化并注册动态接收器
        dynamicReceiver = MyDynamicReceiver()
        val filter = IntentFilter(DYNAMIC_ACTION)
        // 注册广播接收器，根据Android版本添加导出标志
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 必须添加导出标志
            registerReceiver(
                dynamicReceiver,
                filter,
                RECEIVER_EXPORTED  // 允许接收其他应用的广播
                // 或使用 RECEIVER_NOT_EXPORTED（仅接收本应用的广播）
            )
        } else {
            // Android 13以下版本无需添加标志
            ContextCompat.registerReceiver(this,dynamicReceiver, filter,RECEIVER_EXPORTED)
        }

        // 发送静态广播按钮
        findViewById<Button>(R.id.btn_send_static).setOnClickListener {
            val intent = Intent(STATIC_ACTION).apply {
//                Intent.setPackage = packageName  // Android 8.0+ 必需
                putExtra("data", "Hello 静态广播!")
            }
            sendBroadcast(intent)
            statusText.text = "已发送静态广播"
        }

        // 发送动态广播按钮
        findViewById<Button>(R.id.btn_send_dynamic).setOnClickListener {
            val intent = Intent(DYNAMIC_ACTION).apply {
                putExtra("data", "Hello 动态广播!")
            }
            sendBroadcast(intent)
            statusText.text = "已发送动态广播"
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        // 取消注册动态接收器
        unregisterReceiver(dynamicReceiver)
    }
}