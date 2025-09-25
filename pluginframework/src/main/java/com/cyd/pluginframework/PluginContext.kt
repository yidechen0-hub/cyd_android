package com.cyd.pluginframework

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle

/**
 * 统一的PluginContext接口定义：补充资源访问能力
 * 所有方法定义明确，兼顾交互能力与资源能力
 */
interface PluginContext {

    /**
     * 1. 宿主与插件基础交互能力（原有功能保持不变）
     */
    fun getHostContext(): Context

    fun showToast(message: String, duration: Int = 0)

    fun startHostActivity(action: String, extras: Bundle? = null)

    fun startPlugin(pluginId: String, extras: Bundle? = null)

    fun requestPermission(permission: String, callback: PermissionCallback)


    /**
     * 2. 新增：插件资源访问能力（解决资源找不到问题）
     * 插件自身的资源（布局、图片、字符串等）通过以下方法获取
     */
    /**
     * 获取插件自身的Resources对象
     * @return 插件的Resources，非主应用Resources
     */
    fun getPluginResources(): Resources

    /**
     * 获取插件自身的AssetManager（用于访问assets目录下的文件）
     * @return 插件的AssetManager
     */
    fun getPluginAssets(): AssetManager

    /**
     * （可选）获取插件自身的主题
     * 若插件有自定义主题，可通过此方法获取，避免使用主应用主题导致样式错乱
     * @param themeResId 插件主题的资源ID（如R.style.PluginTheme）
     * @return 插件的主题对象
     */
    fun getPluginTheme(themeResId: Int): Resources.Theme

    fun getHostActivity(): Activity?
    /**
     * 权限请求回调接口（原有功能保持不变）
     */
    interface PermissionCallback {
        fun onGranted()
        fun onDenied()
    }
}