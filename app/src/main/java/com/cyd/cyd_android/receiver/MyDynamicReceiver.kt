package com.cyd.cyd_android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.view.WindowMetrics
import android.widget.Toast
import androidx.annotation.RequiresApi

class MyDynamicReceiver : BroadcastReceiver() {
    private val TAG = "MyDynamicReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val action = it.action
            val data = it.getStringExtra("data") ?: "无数据"
            Log.d(TAG, "接收到动态广播 -> Action: $action, 数据: $data")

            // 显示提示消息
            context?.let { ctx ->
                Toast.makeText(ctx, "动态接收: $data", Toast.LENGTH_SHORT).show()
            }
            // 判断是否在分屏模式下
            if (isProbablyInMultiWindow(context)) {
                Log.d(TAG, "当前处于分屏模式")
            } else {
                Log.d(TAG, "当前不处于分屏模式")
            }
        }
    }

//    fun isInMultiWindow(context: Context?): Boolean {
//        if (context == null) return false
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//
//            try {
//                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
//                activityManager.isInMultiWindowMode
//            } catch (e: Exception) {
//                Log.d(TAG, "检测分屏模式时出现异常")
//                e.printStackTrace()
//                false
//            }
//
//        } else {
//            // API < 24 不支持多窗口，返回 false
//            false
//        }
//    }



//    fun isInMultiWindow(context: Context?): Boolean {
//        // 上下文为空直接返回false
//        if (context == null) return false
//
//        // 低于API 24不支持分屏
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//            return false
//        }
//
//        return try {
//            // 通过Configuration获取分屏状态（标准方式）
//            val configuration: Configuration = context.resources.configuration
//            configuration.isInMultiWindowMode
//        } catch (e: Exception) {
//            Log.e(TAG, "检测分屏模式异常: ${e.message}")
//            false
//        }
//    }

    // 辅助判断：窗口宽度是否明显小于屏幕宽度（可能处于分屏）
//    // 辅助判断：窗口宽度是否明显小于屏幕宽度（可能处于分屏）
//    private fun isWindowSizeLimited(context: Context?): Boolean {
//        if (context == null) return false
//        // 获取屏幕宽度和窗口宽度
//        val displayMetrics = context.resources.displayMetrics
//        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
//        val windowWidth = try {
//            // API 17+ 获取窗口宽度
//            val params = context as? Activity ?: return false
//            params.window.attributes.width
//        } catch (e: Exception) {
//            displayMetrics.widthPixels // 失败时使用屏幕宽度
//        }
//        Log.d(TAG, "屏幕宽度: ${displayMetrics.widthPixels}, 窗口宽度: $windowWidth")
//        // 窗口宽度小于屏幕宽度的80%，视为可能处于分屏
//        return windowWidth < displayMetrics.widthPixels
//    }

    fun getWindowWidth(context: Context?): Int {
        if (context == null) return -1
        Log.d(TAG, "获取窗口宽度")
        // 获取屏幕宽度和窗口宽度
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = windowManager.defaultDisplay
            // 创建一个Point对象来存储屏幕尺寸
            val size = Point()
            Log.d(TAG, "获取窗口宽度, size: $size")

            // 根据API版本选择合适的方法
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                // API 17+：获取真实屏幕尺寸（含分屏时的窗口尺寸）
                display.getRealSize(size)
            } else {
                // API <17：获取屏幕可视区域尺寸
                display.getSize(size)
            }

            // 分屏时，窗口宽度通常小于屏幕宽度的90%（可根据实际情况调整阈值）
            size.x
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getScreenWidth(context: Context?): Int {
        if (context == null) return -1
        Log.d(TAG, "获取屏幕宽度")
        val windowManager = context.getSystemService(WindowManager::class.java)
        return windowManager.currentWindowMetrics.bounds.width()

    }


//    fun getWindowSize(context: Context): Pair<Int, Int> {
//        // 1. 优先检查是否为Activity上下文
////        if (context is Activity) {
////            return getActivityWindowSize(context)
////        }
//
//        // 2. API 30+ 使用WindowMetrics（推荐）
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            return try {
//                val windowManager = context.getSystemService(WindowManager::class.java)
//                val metrics: WindowMetrics = windowManager.currentWindowMetrics
//                val bounds: Rect = metrics.bounds
//                Pair(bounds.width(), bounds.height())
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Pair(0, 0)
//            }
//        }
//
//        // 3. 低版本：尝试通过顶层Activity获取（需要ActivityLifecycle支持）
////        val topActivity = getTopActivity()
////        if (topActivity != null) {
////            return getActivityWindowSize(topActivity)
////        }
//
//        // 4. 所有方法失败时返回屏幕尺寸（降级方案）
//        return getScreenSize(context)
//    }
//    private fun getScreenSize(context: Context): Pair<Int, Int> {
//        return try {
//            val displayMetrics = context.resources.displayMetrics
//            Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Pair(0, 0)
//        }
//    }
//
//    fun getRealScreenSize(context: Context): Pair<Int, Int> {
//        return try {
//            // 1. 获取WindowManager
//            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//
//            // 2. 获取默认Display（API 30以下）
//            @Suppress("DEPRECATION")
//            val display: Display = windowManager.defaultDisplay
//
//            // 3. 存储尺寸的Point对象
//            val realSize = Point()
//
//            // 4. 调用getRealSize()（API 17+可用）
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                display.getRealSize(realSize)
//                Pair(realSize.x, realSize.y) // x=宽度，y=高度
//            } else {
//                // API <17 不支持getRealSize()，降级使用getSize()
//                @Suppress("DEPRECATION")
//                display.getSize(realSize)
//                Pair(realSize.x, realSize.y)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Pair(0, 0)
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.R)
//    fun isProbablyInMultiWindow(context: Context?): Boolean {
//        if (context == null) return false
//        // 获取屏幕尺寸
//        val screenWidth = getRealScreenSize(context)
//        if (screenWidth.first <= 0 || screenWidth.second <= 0) return false
//
//        // 获取屏幕原始宽度（通过DisplayMetrics）
//        val displayMetrics = context?.resources?.displayMetrics ?: return false
//        val originalScreenWidth = displayMetrics.widthPixels
//        val originalScreenHeight = displayMetrics.heightPixels
//        Log.d(TAG, "屏幕原始宽度: ${screenWidth.first},屏幕原始高度:${screenWidth.second}, 窗口宽度: ${originalScreenWidth}, 窗口高度: ${originalScreenHeight}")
//        // 窗口宽度 < 屏幕宽度的90%，视为可能处于分屏
//        return originalScreenWidth < screenWidth.first * 0.9f || originalScreenHeight < screenWidth.second * 0.9f
//    }

    /**
     * 获取当前应用窗口的尺寸（API 30+）
     * @return Pair<Int, Int> 窗口宽度和高度（像素）
     */
    fun getWindowSize(context: Context): Pair<Int, Int> {
        // 仅在API 30及以上可用
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return Pair(0, 0)
        }

        return try {
            // 1. 获取WindowManager实例
            val windowManager = context.getSystemService(WindowManager::class.java)

            // 2. 获取当前窗口的metrics
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics

            // 3. 获取窗口边界（Rect包含left, top, right, bottom）
            val bounds: Rect = windowMetrics.bounds

            // 4. 计算宽高（right - left = 宽度，bottom - top = 高度）
            val width = bounds.width()  // 等价于 bounds.right - bounds.left
            val height = bounds.height() // 等价于 bounds.bottom - bounds.top

            Pair(width, height)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(0, 0)
        }
    }

    /**
     * 获取屏幕的真实物理尺寸（API 30+）
     * 注意：此方法返回的是整个屏幕尺寸，与应用窗口无关
     */
    fun getRealScreenSize(context: Context): Pair<Int, Int> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return Pair(0, 0)
        }

        return try {
            val windowManager = context.getSystemService(WindowManager::class.java)
            // 获取屏幕的最大边界（包含所有系统UI和应用窗口）
            val maximumWindowMetrics: WindowMetrics = windowManager.maximumWindowMetrics
            val screenBounds = maximumWindowMetrics.bounds

            Pair(screenBounds.width(), screenBounds.height())
        } catch (e: Exception) {
            Log.e(TAG, "获取屏幕尺寸失败", e)
            Pair(0, 0)
        }
    }


    //        @RequiresApi(Build.VERSION_CODES.R)
    fun isProbablyInMultiWindow(context: Context?): Boolean {
        if (context == null) return false
        // 获取屏幕尺寸
        val screenSize = getRealScreenSize(context)
        if (screenSize.first <= 0 || screenSize.second <= 0) return false

        val displayScreenSize = getWindowSize(context)
        if (displayScreenSize.first <= 0 || displayScreenSize.second <= 0) return false
        Log.d(TAG, "屏幕原始宽度: ${screenSize.first},屏幕原始高度:${screenSize.second}, 窗口宽度: ${displayScreenSize.first}, 窗口高度: ${displayScreenSize.second}")
        // 窗口宽度 < 屏幕宽度的90%，视为可能处于分屏
        return displayScreenSize.first < screenSize.first * 0.9f || displayScreenSize.second < screenSize.second * 0.9f
    }

}