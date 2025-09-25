package com.cyd.cyd_android.plugin

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import com.cyd.pluginframework.IPlugin
//import com.cyd.pluginframework.PluginContext
import com.cyd.pluginframework.HostConstants
import com.cyd.pluginframework.PluginInfo
import dalvik.system.DexClassLoader
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import androidx.appcompat.app.AppCompatActivity

class PluginManager(private val hostContext: Activity) {
    // 插件缓存：key为pluginId，value为插件信息
    private val pluginMap = mutableMapOf<String, LoadedPlugin>()

    // 单例模式
    companion object {
        @Volatile
        private var instance: PluginManager? = null

        fun getInstance(context: Activity): PluginManager {
            return instance ?: synchronized(this) {
                instance ?: PluginManager(context).also { instance = it }
            }
        }
    }

    // 加载指定目录下的所有插件
    fun loadAllPlugins() {
        // 1. 检查外部存储是否可用
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Log.e("PluginManager", "外部存储不可用，无法加载插件")
            return
        }

        // 2. 获取插件目录（优先推荐应用专属目录）
        val pluginsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 推荐使用应用专属目录（无需权限）
            File(hostContext.getExternalFilesDir(null), "cyd/plugins")
        } else {
            // 低版本使用公共目录
            val externalDir = Environment.getExternalStorageDirectory()
            File(externalDir, "cyd/plugins")
        }

        Log.d("PluginManager", "插件目录路径: ${pluginsDir.absolutePath}")

        // 3. 检查并创建目录（处理创建失败的情况）
        if (!pluginsDir.exists()) {
            val isCreated = pluginsDir.mkdirs()
            if (!isCreated) {
                Log.e("PluginManager", "目录创建失败！请检查权限")
//                // 检查是否缺少权限（仅低版本需要）
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//                    checkStoragePermission() // 调用权限检查方法
//                }
                return
            } else {
                Log.d("PluginManager", "目录创建成功")
                return // 目录刚创建，暂无插件可加载
            }
        }

        // 4. 加载所有APK插件（增加空安全和过滤条件）
        val apkFiles = pluginsDir.listFiles { file ->
            file?.isFile == true && file.name.endsWith(".apk", ignoreCase = true)
        }
        pluginsDir.listFiles().forEach {
            Log.d("PluginManager", "发现插件: ${it.absolutePath}")
        }

        if (apkFiles.isNullOrEmpty()) {
            Log.d("PluginManager", "目录中未发现APK插件")
            return
        }

        apkFiles.forEach { file ->
            Log.d("PluginManager", "发现插件: ${file.absolutePath}")
            loadPlugin(file) // 加载单个插件
        }
    }

    // 加载单个插件
    fun loadPlugin(pluginFile: File): PluginInfo? {
        if (!pluginFile.exists() || !pluginFile.name.endsWith(".apk")) {
            Log.e("PluginManager", "无效的插件文件: ${pluginFile.absolutePath}")
            return null
        }

        return try {
            Log.d("PluginManager", "loadPlugin: ${pluginFile.absolutePath}")
            // 1. 获取插件包信息（用于版本检查等）
            val packageInfo = getPackageInfo(pluginFile) ?: return null
            Log.d("PluginManager", "loadPlugin: ${packageInfo.versionName}")
            // 2. 创建插件的类加载器
            val optimizedDir = File(hostContext.cacheDir, HostConstants.PLUGIN_DEX_OPT_DIR)
            optimizedDir.mkdirs()


            val dexClassLoader = DexClassLoader(
                pluginFile.absolutePath,
                optimizedDir.absolutePath,
                null,
                hostContext.classLoader // 父类加载器使用宿主的类加载器
            )

            // 3. 加载插件资源
            val assetManager = AssetManager::class.java.newInstance()
            val addAssetPathMethod = AssetManager::class.java.getMethod("addAssetPath", String::class.java)
            addAssetPathMethod.invoke(assetManager, pluginFile.absolutePath)

            val pluginResources = Resources(
                assetManager,
                hostContext.resources.displayMetrics,
                hostContext.resources.configuration
            )

            // 4. 读取插件配置文件获取主类名和元数据
            val config = readPluginConfig(dexClassLoader, pluginResources) ?: return null

            // 5. 检查版本，如果已有更新版本则跳过加载
            if (isNewVersion(config.pluginId, config.version).not()) {
                Log.i("PluginManager", "插件 ${config.pluginId} 已有更新版本，跳过加载")
                return pluginMap[config.pluginId]?.info
            }

            // 6. 反射创建插件实例
            val pluginClass = dexClassLoader.loadClass(config.mainClass)
            val pluginInstance = pluginClass.newInstance() as IPlugin

            // 7. 创建插件上下文（使用与接口匹配的HostPluginContext实现）
            val pluginContext = HostPluginContext(
                hostActivity = hostContext,
                pluginManager = this
            )
            pluginContext.injectPluginResources(pluginResources, assetManager)
            // 8. 初始化插件
            pluginInstance.init(pluginContext, pluginResources)

            // 9. 缓存插件信息
            val loadedPlugin = LoadedPlugin(
                info = config,
                classLoader = dexClassLoader,
                resources = pluginResources,
                instance = pluginInstance,
                packageInfo = packageInfo,
                file = pluginFile
            )
            pluginMap[config.pluginId] = loadedPlugin

            Log.i("PluginManager", "插件 ${config.name}(${config.pluginId}) 加载成功")
            config
        } catch (e: Exception) {
            Log.e("PluginManager", "加载插件失败: ${e.message}", e)
            null
        }
    }

    // 读取插件配置文件
    private fun readPluginConfig(classLoader: DexClassLoader, resources: Resources): PluginInfo? {
        return try {
            // 从插件的assets目录读取配置文件
            val inputStream = resources.assets.open(HostConstants.PLUGIN_CONFIG_FILE)
            val reader = InputStreamReader(inputStream)
            val configJson = reader.readText()
            reader.close()

            val conf = Gson().fromJson(configJson, PluginInfo::class.java)
            Log.d("PluginManager", "读取插件配置文件: $conf")
            conf
        } catch (e: IOException) {
            Log.e("PluginManager", "读取插件配置文件失败: ${e.message}", e)
            null
        }
    }

    // 获取插件包信息
    private fun getPackageInfo(pluginFile: File): PackageInfo? {
        return try {
            hostContext.packageManager.getPackageArchiveInfo(
                pluginFile.absolutePath,
                PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES
            )
        } catch (e: Exception) {
            Log.e("PluginManager", "获取插件包信息失败: ${e.message}", e)
            null
        }
    }

    // 检查是否为新版本
    private fun isNewVersion(pluginId: String, newVersion: String): Boolean {
        val existingPlugin = pluginMap[pluginId] ?: return true
        return compareVersions(newVersion, existingPlugin.info.version) > 0
    }

    // 版本比较辅助方法
    private fun compareVersions(version1: String, version2: String): Int {
        val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val part1 = if (i < parts1.size) parts1[i] else 0
            val part2 = if (i < parts2.size) parts2[i] else 0
            if (part1 > part2) return 1
            if (part1 < part2) return -1
        }
        return 0
    }

    // 获取所有已加载的插件信息
    fun getAllPlugins(): List<PluginInfo> {
        return pluginMap.values.map { it.info }
    }

    // 获取插件实例
    fun getPluginInstance(pluginId: String): IPlugin? {
        return pluginMap[pluginId]?.instance
    }

    // 启动插件并传递参数
    fun launchPlugin(pluginId: String, extras: Bundle? = null) {
        val plugin = getPluginInstance(pluginId)
        if (plugin != null) {
            try {
                plugin.start(extras)
            } catch (e: Exception) {
                Log.e("PluginManager", "启动插件 $pluginId 失败: ${e.message}", e)
            }
        } else {
            Log.w("PluginManager", "插件 $pluginId 未找到")
        }
    }

    // 新增：启动插件的方法（解决Unresolved reference 'startPlugin'错误）
    fun startPlugin(pluginId: String, extras: Bundle? = null) {
        try {
            // 1. 查找插件实例
            val plugin = getPluginInstance(pluginId)
                ?: throw IllegalArgumentException("插件 $pluginId 未找到或未加载")

            // 2. 调用插件的启动方法
            plugin.start(extras)

            Log.i("PluginManager", "插件 $pluginId 启动成功")
        } catch (e: Exception) {
            Log.e("PluginManager", "启动插件 $pluginId 失败", e)
            throw e // 抛出异常让调用方处理
        }
    }

    // 卸载插件
    fun unloadPlugin(pluginId: String): Boolean {
        val plugin = pluginMap.remove(pluginId)
        return if (plugin != null) {
            // 删除插件文件
            if (plugin.file.exists()) {
                plugin.file.delete()
            }
            Log.i("PluginManager", "插件 $pluginId 已卸载")
            true
        } else {
            false
        }
    }

    // 插件加载信息内部类
    private data class LoadedPlugin(
        val info: PluginInfo,
        val classLoader: DexClassLoader,
        val resources: Resources,
        val instance: IPlugin,
        val packageInfo: PackageInfo,
        val file: File
    )



    interface PermissionRequestDelegate {
        fun requestPermission(permission: String, callback: PermissionCallback)
    }

    // 2. 权限回调接口（与HostPluginContext调用匹配）
    interface PermissionCallback {
        fun onGranted()
        fun onDenied()
    }

    // 3. 委托字段（由宿主Activity设置）
    private var permissionDelegate: PermissionRequestDelegate? = null

    // 4. 供宿主Activity调用：设置委托
    fun setPermissionDelegate(delegate: PermissionRequestDelegate) {
        this.permissionDelegate = delegate
    }

    // 5. 核心：仅转发请求给委托，不直接处理
    fun requestPermission(permission: String, callback: PermissionCallback) {
        // 检查委托是否已设置（宿主Activity是否初始化）
        val delegate = permissionDelegate ?: run {
            callback.onDenied()
            Log.e("PluginManager", "权限请求失败：未设置PermissionDelegate")
            return
        }

        // 检查权限是否已授予（提前判断，避免重复请求）
        if (ContextCompat.checkSelfPermission(hostContext, permission) == PackageManager.PERMISSION_GRANTED) {
            callback.onGranted()
            return
        }

        // 转发请求给宿主Activity
        delegate.requestPermission(permission, callback)
    }
}
