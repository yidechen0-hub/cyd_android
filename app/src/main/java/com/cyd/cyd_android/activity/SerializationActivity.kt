package com.cyd.cyd_android.activity


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyd.cyd_android.R
import com.cyd.cyd_android.serialization.ComplexDataProtoClass
import com.cyd.cyd_android.serialization.CrossProcessTester
import com.cyd.cyd_android.serialization.DataGenerator
import com.cyd.cyd_android.serialization.SerializationBenchmark
import com.cyd.cyd_android.serialization.UserProto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureNanoTime

class SerializationActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serialization)
        val SerializationBenchmarksBtn = findViewById<Button>(R.id.SerializationBenchmarks_btn)
        val CrossProcessTestsBtn = findViewById<Button>(R.id.CrossProcessTests_btn)
        val TestUserDataBtn = findViewById<Button>(R.id.TestUserData_btn)
        val TestComplexDataBtn = findViewById<Button>(R.id.TestComplexData_btn)
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
        TestUserDataBtn.setOnClickListener {
            val iterations = 10000

            // 构建一个 User 对象
            val user = UserProto.User.newBuilder()
                .setId(123)
                .setName("Alice")
                .setEmail("alice@example.com")
                .build()

            // 序列化测试
            val serializeTime = measureNanoTime {
                repeat(iterations) {
                    user.toByteArray()
                }
            }
            val avgSerializeTime = serializeTime / iterations.toDouble()

            // 先准备一个序列化的字节数组
            val serialized = user.toByteArray()

            // 反序列化测试
            val deserializeTime = measureNanoTime {
                repeat(iterations) {
                    UserProto.User.parseFrom(serialized)
                }
            }
            val avgDeserializeTime = deserializeTime / iterations.toDouble()

            Log.d("ProtoPerf", "序列化平均耗时 = $avgSerializeTime ns")
            Log.d("ProtoPerf", "反序列化平均耗时 = $avgDeserializeTime ns")
        }

        TestComplexDataBtn.setOnClickListener {
            val iterations = 10
            val data = DataGenerator.generateComplexData(DataGenerator.DataSize.SIZE_2MB)
            val protoData = DataGenerator.convertToProto(data)
            repeat(5) {
                protoData.toByteArray()
            }
            // 序列化测试
            val serializeTime = measureNanoTime {
                repeat(iterations) {
                    protoData.toByteArray()
                }
            }
            val avgSerializeTime = serializeTime / iterations.toDouble()

            // 先准备一个序列化的字节数组
            val serialized = protoData.toByteArray()
            repeat(5) {
                ComplexDataProtoClass.ComplexDataProto.parseFrom(serialized)
            }
            // 反序列化测试
            val deserializeTime = measureNanoTime {
                repeat(iterations) {
                    ComplexDataProtoClass.ComplexDataProto.parseFrom(serialized)
                }
            }
            val avgDeserializeTime = deserializeTime / iterations.toDouble()
            Log.d("ProtoPerf", "序列化平均耗时 = ${avgSerializeTime/1000000.0} ms" )
            Log.d("ProtoPerf", "反序列化平均耗时 = ${avgDeserializeTime/1000000.0} ms" )
        }

    }
    fun runSerializationBenchmarks(context: Context) {
        // 运行本地序列化性能测试
        val benchmark = SerializationBenchmark()
        benchmark.runAllBenchmarks()
    }

    fun runCrossProcessTests(context: Context) {

//        // 运行跨进程通信测试
        val crossProcessTester = CrossProcessTester()
//        crossProcessTester.bindService(context)
//        // 等待服务绑定完成后调用
//        crossProcessTester.runCrossProcessTests()
        // 例如在Activity的lifecycleScope中
        lifecycleScope.launch {
            try {
                // 1. 挂起等待服务绑定成功（失败会抛异常）
                crossProcessTester.bindServiceSuspend(this@SerializationActivity)

                // 2. 绑定成功后，执行测试（此时isBound一定为true）
                crossProcessTester.runCrossProcessTests()
            } catch (e: Exception) {
                Log.e("Client", "服务绑定或测试失败", e)
            } finally {
                // 3. 测试完成后，解绑服务（可选，根据需求决定是否保持绑定）
//                crossProcessTester.unbindService(this@MainActivity)
            }
        }
    }




}