package com.cyd.pluginframework

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast

/**
 * 修复后的HostPluginContext：实现PluginContext接口的所有抽象方法
 */
class HostPluginContext(
//    private val hostContext: Context,
    private val hostActivity: Activity,
    private val pluginManager: PluginManager
) : PluginContext {

    // 1. 保存插件资源（由PluginManager注入）
    private lateinit var pluginResources: Resources
    private lateinit var pluginAssets: AssetManager


    /**
     * 供PluginManager调用：注入插件资源（非接口方法，自定义）
     */
    fun injectPluginResources(resources: Resources, assets: AssetManager) {
        this.pluginResources = resources
        this.pluginAssets = assets
    }


    // 2. 实现PluginContext接口的原有抽象方法
    override fun getHostContext(): Context {
//        return hostContext.applicationContext
        return hostActivity
    }

    override fun showToast(message: String, duration: Int) {
        val toastDuration = if (duration == 1) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(hostActivity, message, toastDuration).show()
    }

    override fun startHostActivity(action: String, extras: Bundle?) {
        val intent = Intent(action).apply {
            setPackage(hostActivity.packageName) // 主应用包名
            extras?.let { putExtras(it) }
            // 若宿主不是Activity，添加NEW_TASK flag
            if (hostActivity !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        hostActivity.startActivity(intent)
    }

    override fun startPlugin(pluginId: String, extras: Bundle?) {
        // 调用PluginManager的方法启动插件（确保PluginManager有该方法）
        pluginManager.getPluginInstance(pluginId)?.start(extras)
    }

    override fun requestPermission(permission: String, callback: PluginContext.PermissionCallback) {
        if (hostActivity is Activity) {
            // 修复2：调用PluginManager的requestPermission方法（确保PluginManager有该方法）
            pluginManager.requestPermission(
                permission = permission,
                callback = object : PluginManager.PermissionCallback {
                    override fun onGranted() {
                        callback.onGranted()
                    }

                    override fun onDenied() {
                        callback.onDenied() // 正确：onDenied无参数
                    }
                }
            )
        } else {
            callback.onDenied() // 无Activity上下文，直接回调拒绝
        }
    }


    // 3. 实现PluginContext接口新增的抽象方法（资源相关）
    override fun getPluginResources(): Resources {
        return pluginResources // 返回注入的插件资源
    }

    override fun getPluginAssets(): AssetManager {
        return pluginAssets // 返回注入的插件AssetManager
    }

    override fun getPluginTheme(themeResId: Int): Resources.Theme {
        // 修复3：通过插件资源创建主题，避免调用super（抽象方法不能直接调用）
        val theme = pluginResources.newTheme()
        theme.applyStyle(themeResId, true) // 应用插件自身主题
        return theme
    }

    override fun getHostActivity(): Activity? {
        return if (!hostActivity.isFinishing && !hostActivity.isDestroyed) {
            hostActivity
        } else {
            null
        }
    }
}