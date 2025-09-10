package com.cyd.cyd_android.activity





import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyd.cyd_android.R
import com.cyd.cyd_android.adapter.OnItemClickListener
import com.cyd.cyd_android.adapter.RecyclerViewAdapter

class RecyclerViewActivity : AppCompatActivity(), OnItemClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)

        // 获取RecyclerView实例
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        // 设置布局管理器
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 准备数据
        val data = mutableListOf<String>()
        for (i in 0 until 50) {
            data.add("Item ${i + 1}")
        }

        // 设置适配器，并传入点击事件监听器
        recyclerView.adapter = RecyclerViewAdapter(data, this)
    }

    // 实现点击事件接口
    override fun onItemClick(position: Int, item: String) {
        // 处理点击事件，这里用Toast显示点击的内容和位置
        Toast.makeText(this, "点击了第${position + 1}项: $item", Toast.LENGTH_SHORT).show()

        // 可以根据需要添加更多逻辑，如跳转页面、修改数据等
    }
}
