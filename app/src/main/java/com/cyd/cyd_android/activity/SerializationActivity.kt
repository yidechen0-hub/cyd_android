package com.cyd.cyd_android.activity


import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyd.cyd_android.R
import com.cyd.cyd_android.serialization.CrossProcessTester
import com.cyd.cyd_android.serialization.SerializationBenchmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SerializationActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serialization)
        val SerializationBenchmarksBtn = findViewById<Button>(R.id.SerializationBenchmarks_btn)
        val CrossProcessTestsBtn = findViewById<Button>(R.id.CrossProcessTests_btn)
        SerializationBenchmarksBtn.setOnClickListener {
            // 从 UI 线程启动协程
            lifecycleScope.launch(Dispatchers.IO) {
                // 显式指定泛型类型为 Unit（无返回值）
                coroutineScope<Unit> {
                    runSerializationBenchmarks(this@SerializationActivity)
                }
                // 测试完成后切换回主线程更新 UI
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SerializationActivity, "测试完成", Toast.LENGTH_SHORT).show()
                }
            }

        }
        CrossProcessTestsBtn.setOnClickListener {

            runCrossProcessTests(this)
        }

    }
    fun runSerializationBenchmarks(context: Context) {
        // 运行本地序列化性能测试
        val benchmark = SerializationBenchmark()
        benchmark.runAllBenchmarks()
    }

    fun runCrossProcessTests(context: Context) {

        // 运行跨进程通信测试
        val crossProcessTester = CrossProcessTester()
        crossProcessTester.bindService(context)
        // 等待服务绑定完成后调用
        crossProcessTester.runCrossProcessTests()
    }




}