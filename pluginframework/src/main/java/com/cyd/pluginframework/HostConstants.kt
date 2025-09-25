package com.cyd.pluginframework

/**
 * 宿主应用与插件之间共享的常量定义
 * 该类应放在公共接口模块中，供宿主和所有插件依赖
 */
object HostConstants {
    // ====================== 意图动作常量 ======================
    /** 启动宿主主界面的动作 */
    const val ACTION_HOST_MAIN = "com.example.hostapp.ACTION_MAIN"

    /** 启动宿主设置界面的动作 */
    const val ACTION_HOST_SETTINGS = "com.example.hostapp.ACTION_SETTINGS"

    /** 插件请求宿主显示通知的动作 */
    const val ACTION_SHOW_NOTIFICATION = "com.example.hostapp.ACTION_SHOW_NOTIFICATION"

    // ====================== 插件相关常量 ======================
    /** 插件配置文件在assets中的路径 */
    const val PLUGIN_CONFIG_FILE = "plugin_config.json"

    /** 插件存储目录 */
    const val PLUGIN_STORAGE_DIR = "plugins"

    /** 插件Dex优化目录 */
    const val PLUGIN_DEX_OPT_DIR = "plugin_dex"

    // ====================== 意图 extras 键 ======================
    /** 传递插件ID的键 */
    const val EXTRA_PLUGIN_ID = "extra_plugin_id"

    /** 传递消息内容的键 */
    const val EXTRA_MESSAGE = "extra_message"

    /** 传递数据的键 */
    const val EXTRA_DATA = "extra_data"

    // ====================== 权限相关常量 ======================
    /** 插件需要的基础权限 */
    const val PERMISSION_BASE = "com.example.hostapp.permission.PLUGIN_BASE"

    /** 插件访问网络的权限 */
    const val PERMISSION_NETWORK = "com.example.hostapp.permission.PLUGIN_NETWORK"

    // ====================== 版本相关常量 ======================
    /** 最低支持的插件接口版本 */
    const val MIN_SUPPORTED_INTERFACE_VERSION = 1

    /** 当前宿主支持的接口版本 */
    const val CURRENT_INTERFACE_VERSION = 2
}
