package com.cyd.cyd_android.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cyd.cyd_android.R
import com.cyd.cyd_android.contentProvider.Students

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.add_btn).setOnClickListener { onClickAddName(it) }
        findViewById<Button>(R.id.get_btn).setOnClickListener { onClickRetrieveName(it) }
    }
    fun onClickAddName(view: View) {
        // 创建内容值对象并添加数据
        val contentValues = ContentValues().apply {
            put(Students.Companion.NAME, (findViewById<EditText>(R.id.add_edi)).text.toString())
        }

        println(contentValues)

        // 插入数据到内容提供者
        val uri = contentResolver.insert(Students.Companion.CONTENT_URI, contentValues)

        println(uri)

        // 显示插入结果
        Toast.makeText(baseContext, uri.toString(), Toast.LENGTH_LONG).show()
    }

    @SuppressLint("Range")
    fun onClickRetrieveName(view: View) {
        // 查询学生记录
        val cursor = contentResolver.query(Students.Companion.CONTENT_URI, null, "name=?", arrayOf("cyd"), null)
        Log.d(TAG, "onClickRetrieveName1: $cursor")
        // 遍历查询结果
        if (cursor?.moveToFirst() == true) {
            Log.d(TAG, "onClickRetrieveName2: ${cursor.getString(cursor.getColumnIndex(Students.Companion.NAME)       )}")
            Log.d(TAG, "onClickRetrieveName2: ${cursor.getString(cursor.getColumnIndex(Students.Companion._ID)        )}")
            do {
                // 更新文本视图显示
                findViewById<TextView>(R.id.get_edi).text =
                    cursor.getString(cursor.getColumnIndex(Students.Companion.NAME))

                // 显示每条记录的信息
                val id = cursor.getString(cursor.getColumnIndex(Students.Companion._ID))
                val name = cursor.getString(cursor.getColumnIndex(Students.Companion.NAME))
                Toast.makeText(this, "$id, $name", Toast.LENGTH_SHORT).show()
            } while (cursor.moveToNext())
        }else{
            Log.d(TAG, "onClickRetrieveName3: 没有数据")
            findViewById<TextView>(R.id.get_edi).text = "没有数据"
        }

        // 关闭游标释放资源
        cursor?.close()
    }
    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
    }
}