package com.cyd.cyd_android.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyd.cyd_android.adapter.User
import com.cyd.cyd_android.adapter.UserAdapter
import com.cyd.cyd_android.R

class AdapterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adapter)

        // 初始化数据
        val users = listOf(
            User(1, "张三", "zhangsan@example.com"),
            User(2, "李四", "lisi@example.com"),
            User(3, "王五", "wangwu@example.com"),
            User(4, "赵六", "zhaoliu@example.com"),
            User(5, "钱七", "qianqi@example.com")
        )

        // 获取RecyclerView并设置布局管理器
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 创建并设置适配器
        val adapter = UserAdapter { user ->
            // 处理列表项点击事件
            Toast.makeText(this, "点击了 ${user.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        // 提交数据到适配器
        adapter.submitList(users)
    }
}