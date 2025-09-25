package com.cyd.cyd_android.activity

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cyd.pluginframework.PluginManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.File
import com.cyd.cyd_android.R

class HostMainActivity : AppCompatActivity(), PluginManager.PermissionRequestDelegate {
    private lateinit var pluginManager: PluginManager
    private lateinit var pluginContainer: LinearLayout
    private lateinit var pluginsDir: File

    // Activity中才能使用registerForActivityResult
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        currentPermissionCallback?.let {
            if (isGranted) it.onGranted() else it.onDenied()
        }
        currentPermissionCallback = null
    }
    private var currentPermissionCallback: PluginManager.PermissionCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_main)



        // 初始化PluginManager并设置委托（关键）
        pluginManager = PluginManager(this)
        pluginManager.setPermissionDelegate(this)

        pluginContainer = findViewById(R.id.plugin_container)
        pluginManager = PluginManager.getInstance(this)
        pluginsDir = File(Environment.getExternalStorageDirectory(), "plugins")

        // 初始化插件目录
        initPluginDirectory()

        // 加载插件
        findViewById<Button>(R.id.btn_refresh).setOnClickListener {
            loadAndShowPlugins()
        }

        // 初始加载插件
        loadAndShowPlugins()
    }

    // 实现PluginManager.PermissionRequestDelegate接口
    override fun requestPermission(
        permission: String,
        callback: PluginManager.PermissionCallback
    ) {
        currentPermissionCallback = callback
        permissionLauncher.launch(permission)
    }

    private fun initPluginDirectory() {
//        val pluginsDir = File(filesDir, "plugins")
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs()
        }
    }

    private fun loadAndShowPlugins() {
        Log.d("PluginHost", "loadAndShowPlugins")
        // 清空现有插件列表
        pluginContainer.removeAllViews()

        // 在后台线程加载插件，避免阻塞UI
        Thread {
            Log.d("PluginHost", "加载插件")
            // 加载插件
            pluginManager.loadAllPlugins()
           Log.d("PluginHost", "加载插件完成")
            // 获取所有插件
            val plugins = pluginManager.getAllPlugins()
            plugins.forEach { pluginInfo ->
                Log.d("PluginHost", "插件名称: ${pluginInfo.name}, 描述: ${pluginInfo.description}")
            }

            // 切换到UI线程更新界面
            runOnUiThread {
                if (plugins.isEmpty()) {
                    // 显示无插件提示
                    val noPluginView = TextView(this).apply {
                        text = "未发现任何插件，请将插件APK放入 ${pluginsDir}/plugins 目录"
                        textSize = 14f
                        setTextColor(ContextCompat.getColor(context, R.color.host_text_secondary))
                        // 修复padding错误：使用setPadding()方法，参数为像素值
                        val paddingPx = dpToPx(16)
                        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                        gravity = Gravity.CENTER
                    }
                    pluginContainer.addView(noPluginView)
                } else {
                    // 显示插件列表
                    plugins.forEach { pluginInfo ->
                        val pluginView = layoutInflater.inflate(R.layout.item_plugin, pluginContainer, false)

                        // 绑定插件信息到视图
                        pluginView.findViewById<TextView>(R.id.pluginName).text = pluginInfo.name
                        pluginView.findViewById<TextView>(R.id.pluginVersion).text = "版本: ${pluginInfo.version}"
                        pluginView.findViewById<TextView>(R.id.pluginDescription).text = pluginInfo.description

                        // 启动插件按钮点击事件
                        pluginView.findViewById<Button>(R.id.startPluginButton).setOnClickListener {
                            try {

                                pluginManager.startPlugin(pluginInfo.pluginId)
                                Toast.makeText(this, "启动 ${pluginInfo.name}", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this, "启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("PluginHost", "启动插件失败", e)
                            }
                        }

                        pluginContainer.addView(pluginView)
                    }
                }
            }
        }.start()
    }

    // 辅助方法：将dp转换为像素
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

}
