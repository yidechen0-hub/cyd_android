package com.cyd.cyd_android.adapter



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cyd.cyd_android.R
// 定义点击事件接口
interface OnItemClickListener {
    fun onItemClick(position: Int, item: String)
}

class RecyclerViewAdapter(
    private val data: List<String>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    // ViewHolder类
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    // 创建ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(view)
    }

    // 绑定数据
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = data[position]
        holder.textView.text = item

        // 设置点击事件
        holder.itemView.setOnClickListener {
            listener.onItemClick(position, item)
        }

        // 可选：添加长按事件
        holder.itemView.setOnLongClickListener {
            // 处理长按逻辑
            true
        }
    }

    // 获取数据数量
    override fun getItemCount() = data.size
}
