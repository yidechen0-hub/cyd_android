package com.cyd.pluginframework

import android.content.res.Resources
import android.os.Bundle

// 插件信息数据类 - 包含插件元数据
data class PluginInfo(
    val pluginId: String,
    val name: String,
    val version: String,
    val description: String,
    val mainClass: String
)

// 插件上下文 - 封装宿主提供的必要能力，而非直接暴露宿主Activity
//interface PluginContext {
//    fun getHostContext(): Context
//    fun showToast(message: String)
//    fun startHostActivity(action: String, extras: Map<String, Any>? = null)
//    // 可以添加更多宿主提供的能力，但保持抽象
//}

// 核心插件接口 - 完全独立，不依赖具体宿主类
interface IPlugin {
    // 初始化插件，接收抽象的PluginContext而非具体宿主Activity
    fun init(context: PluginContext, pluginResources: Resources)

    // 启动插件
    fun start(bundle: Bundle? = null)

    // 停止插件
    fun stop()

    // 获取插件信息
    fun getPluginInfo(): PluginInfo
}
