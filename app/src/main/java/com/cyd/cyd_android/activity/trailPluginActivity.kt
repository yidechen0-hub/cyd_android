package com.cyd.cyd_android.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cyd.cyd_android.R
import com.cyd.cyd_android.contentProvider.Students
import dalvik.system.DexClassLoader
import java.io.File
import kotlin.toString



class trailPluginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trail_plugin)

        testClassLoader()
        testAllClassLoader()

        testParentDelegationMechanism()


    }

    fun testClassLoader() {
        val pathClassLoader = MainActivity::class.java.classLoader
        Log.d(TAG, pathClassLoader?.toString() ?: "PathClassLoader is null")

        val bootClassLoader = String::class.java.classLoader
        Log.d(TAG, bootClassLoader?.toString() ?: "BootClassLoader is null")
    }
    fun testAllClassLoader() {
        var pathClassLoader = MainActivity::class.java.classLoader
//        Log.d(TAG, pathClassLoader?.toString() ?: "PathClassLoader is null")
        while(pathClassLoader != null){
            Log.d(TAG, pathClassLoader?.toString() ?: "PathClassLoader is null")
            pathClassLoader = pathClassLoader.parent
        }
    }
    fun testDexClassLoader(context: Context, dexFilePath: String) {
        // 创建存放优化后dex文件的目录（应用私有目录）
        val optFile: File = context.getDir("opt_dex", Context.MODE_PRIVATE)
        // 创建存放native库的目录
        val libFile: File = context.getDir("lib_path", Context.MODE_PRIVATE)
        val dexClassLoader = DexClassLoader(
            dexFilePath,
            optFile.absolutePath,
            libFile.absolutePath,
            MainActivity::class.java.classLoader
        )
        Log.d(TAG, dexClassLoader.toString())
    }

    fun testParentDelegationMechanism() {
        // 获取MainActivity的类加载器（通常是PathClassLoader）
        val classLoader = MainActivity::class.java.classLoader

        try {
            // 尝试加载java.lang.String类
            val stringClass = classLoader?.loadClass("java.lang.String")
            Log.d(TAG, "load StringClass success! ${classLoader?.toString()}")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Log.d(TAG, "load StringClass fail! ${classLoader?.toString()}")
        }
    }

    companion object {
        private val TAG: String = "trailPluginActivity"
    }
}