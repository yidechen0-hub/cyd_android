package com.cyd.deviceinfoplugin

import android.os.Build
import com.cyd.pluginframework.IPlugin
import com.cyd.pluginframework.PluginInfo
import com.cyd.pluginframework.PluginContext
import android.content.res.Resources
import android.view.View
import android.widget.TextView
import android.app.AlertDialog
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.FrameLayout


// 插件现在只依赖抽象接口，不依赖具体宿主类
class DeviceInfoPlugin : IPlugin {
    private lateinit var pluginContext: PluginContext
    private lateinit var pluginResources: Resources
    private lateinit var pluginInfo: PluginInfo

    override fun init(context: PluginContext, pluginResources: Resources) {
        this.pluginContext = context
        this.pluginResources = pluginResources

        // 初始化插件信息
        this.pluginInfo = PluginInfo(
            pluginId = "device_info",
            name = "设备信息插件",
            version = "1.0.0",
            description = "显示设备硬件和系统信息",
            mainClass = "com.cyd.deviceinfoplugin.DeviceInfoPlugin"
        )
    }

    override fun start(bundle: Bundle?) {
        // 使用抽象的上下文获取宿主环境，而非直接依赖HostMainActivity
        val hostContext = pluginContext.getHostContext()


        // 加载插件资源（使用插件自身的资源ID）
        val layoutId = pluginResources.getIdentifier("activity_device_info", "layout", "com.cyd.deviceinfoplugin")
        if (layoutId == 0) {
//            pluginContext.showToast("布局资源未找到", 1)
            Log.e("DeviceInfoPlugin", "布局ID无效：activity_device_info")
            return
        }

        val pluginResourceContext = object : ContextThemeWrapper(hostContext, 0) {
            // 重写成员方法（这里是类的成员方法，可使用override）
            override fun getResources(): Resources {
                return pluginResources
            }

            override fun getAssets(): AssetManager {
                return pluginResources.assets
            }
        }

        // 3.3 用插件资源上下文创建LayoutInflater，加载布局
        val inflater = LayoutInflater.from(pluginResourceContext)
        val view = inflater.inflate(layoutId, null)
            ?: run {
                Log.e("DeviceInfoPlugin", "布局加载失败：布局文件损坏或资源不匹配")
                pluginContext.showToast("布局加载失败", 1)
                return
            }

        // 4. 查找并设置TextView（空安全检查）
        val textViewId = pluginResources.getIdentifier(
            "deviceInfoTextView",  // 控件ID（与布局中定义一致）
            "id",
            "com.cyd.deviceinfoplugin"
        )
        if (textViewId == 0) {
            Log.e("DeviceInfoPlugin", "控件ID无效：deviceInfoTextView")
            pluginContext.showToast("文本控件未找到", 1)
            return
        }
        val textView = view.findViewById<TextView>(textViewId)
        textView?.text = getDeviceInfo() ?: "无法获取设备信息"
        Log.d("DeviceInfoPlugin", "获取到的设备信息：${getDeviceInfo() ?: "null"}") // 查看日志输出

        // 使用抽象接口显示Toast，而非直接调用宿主方法
        pluginContext.showToast("${pluginInfo.name} 已启动")

    }

    override fun stop() {
        pluginContext.showToast("${pluginInfo.name} 已停止")
    }

    override fun getPluginInfo(): PluginInfo {
        return pluginInfo
    }

    private fun getDeviceInfo(): String {
        return buildString {
            append("设备型号: ${Build.MODEL}\n")
            append("制造商: ${Build.MANUFACTURER}\n")
            append("Android版本: ${Build.VERSION.RELEASE}\n")
            append("SDK版本: ${Build.VERSION.SDK_INT}\n")
        }
    }
}
