package com.cyd.cyd_android.activity


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.reflect.Method
import java.util.*
import com.cyd.cyd_android.R
import java.util.Locale


class GetSysLanguageActivity : AppCompatActivity() {

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_sys_language)

        textView = findViewById(R.id.textView)

        displaySystemLanguageInfo()
    }

    private fun displaySystemLanguageInfo() {
        val currentLocale = getCurrentLocale()
        val userPreferredLocales = getSupportedLocales()
        val systemAvailableLocales = getSystemAvailableLocales() // 新增：尝试获取系统支持的语言

        val currentInfo = buildLocaleInfo("当前系统主语言", currentLocale)
        val userPreferredInfo = buildSupportedLocalesInfo("用户首选语言列表", userPreferredLocales)
        val systemAvailableInfo = buildSupportedLocalesInfo("系统可用语言列表 (实验性)", systemAvailableLocales)

        val fullText = """
            $currentInfo
            
            $userPreferredInfo
            
            $systemAvailableInfo
            
            注意：
            • “用户首选语言” 是您在系统设置中选择的语言。
            • “系统可用语言” 是通过反射内部 API 获取的，可能不完整或因设备/系统版本而异。
            • 没有官方 API 能 100% 获取 ROM 编译时包含的所有语言。
        """.trimIndent()

        textView.text = fullText
    }

    @Suppress("DEPRECATION")
    private fun getCurrentLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0)
        } else {
            resources.configuration.locale
        }
    }

    @Suppress("DEPRECATION")
    private fun getSupportedLocales(): List<Locale> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = resources.configuration.locales
            val result = mutableListOf<Locale>()
            for (i in 0 until localeList.size()) {
                result.add(localeList.get(i))
            }
            result
        } else {
            listOf(resources.configuration.locale)
        }
    }

    /**
     * 尝试通过反射获取系统支持的语言列表（实验性，非官方方法）
     * 此方法调用 android.widget.LocalePicker.getSystemAvailableLocales()
     * 行为可能因 Android 版本和设备制造商而异。
     */
    private fun getSystemAvailableLocales(): List<Locale> {
        return try {
            // 获取 LocalePicker 类
            val localePickerClass = Class.forName("android.widget.LocalePicker")
            // 查找 getSystemAvailableLocales 方法
            val method: Method = localePickerClass.getDeclaredMethod("getSystemAvailableLocales")
            // 调用静态方法
            val locales = method.invoke(null) as? List<Locale> ?: emptyList()
            Log.d("LocaleDemo", "通过反射获取到 ${locales.size} 个系统可用语言")
            locales
        } catch (e: ClassNotFoundException) {
            Log.e("LocaleDemo", "未找到 LocalePicker 类", e)
            emptyList()
        } catch (e: NoSuchMethodException) {
            Log.e("LocaleDemo", "未找到 getSystemAvailableLocales 方法", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("LocaleDemo", "反射调用失败", e)
            emptyList()
        }
    }

    private fun buildLocaleInfo(title: String, locale: Locale): String {
        return """
            $title:
              语言代码: ${locale.language}
              国家代码: ${locale.country}
              显示名称: ${locale.displayName}
              显示语言: ${locale.displayLanguage}
              显示国家: ${locale.displayCountry}
              完整 Locale: $locale
        """.trimIndent()
    }

    private fun buildSupportedLocalesInfo(title: String, locales: List<Locale>): String {
        if (locales.isEmpty()) {
            return "$title：空"
        }

        val sb = StringBuilder()
        sb.append("$title (共 ${locales.size} 项):")
        locales.forEachIndexed { index, locale ->
            sb.append("\n  [$index] ${locale.displayName} (${locale.toLanguageTag()})")
        }
        return sb.toString()
    }
}