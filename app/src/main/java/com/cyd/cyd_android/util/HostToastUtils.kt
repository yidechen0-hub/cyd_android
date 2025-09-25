package com.cyd.cyd_android.util

import android.content.Context
import android.widget.Toast

object HostToastUtils {
    // 显示短时间Toast
    fun show(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // 显示长时间Toast
    fun showLong(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
