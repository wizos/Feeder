package com.nononsenseapps.feeder.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class FooItemAdapter(
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: List<UiItem> = emptyList()

    fun setData(items: List<UiItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        items[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (parseFooItemInt(viewType)) {
            FooItemType.HEADER -> HeaderViewHolder(parent)
            FooItemType.TEXT -> TextViewHolder(parent)
            FooItemType.IMAGE -> ImageViewHolder(parent)
            FooItemType.BLOCKQUOTE -> BlockquoteViewHolder(parent)
            FooItemType.TABLE -> TableViewHolder(parent)
            FooItemType.LIST -> ListViewHolder(parent)
            FooItemType.VIDEO -> VideoViewHolder(parent)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BindableViewHolder) {
            holder.bind(context, items[position])
        }
    }

    override fun getItemCount(): Int = items.size
}

interface BindableViewHolder {
    fun bind(context: Context, item: UiItem)
}

class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BindableViewHolder {
    constructor(parent: ViewGroup) : this(
        LayoutInflater.from(parent.context).inflate(TODO() as Int, parent, false)
    )

    override fun bind(context: Context, item: UiItem) {
        TODO("Not yet implemented")
    }
}

class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BindableViewHolder {
    constructor(parent: ViewGroup) : this(
        LayoutInflater.from(parent.context).inflate(TODO() as Int, parent, false)
    )

    override fun bind(context: Context, item: UiItem) {
        if (item !is TextItem) {
            return
        }
        
        TODO("Not yet implemented with ${item.spanBuilder}")
    }
}

class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BindableViewHolder {
    constructor(parent: ViewGroup) : this(
        LayoutInflater.from(parent.context).inflate(TODO() as Int, parent, false)
    )

    override fun bind(context: Context, item: UiItem) {
        TODO("Not yet implemented")
    }
}

class BlockquoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BindableViewHolder {
    constructor(parent: ViewGroup) : this(
        LayoutInflater.from(parent.context).inflate(TODO() as Int, parent, false)
    )

    override fun bind(context: Context, item: UiItem) {
        TODO("Not yet implemented")
    }
}

class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BindableViewHolder {
    constructor(parent: ViewGroup) : this(
        LayoutInflater.from(parent.context).inflate(TODO() as Int, parent, false)
    )

    override fun bind(context: Context, item: UiItem) {
        TODO("Not yet implemented")
    }
}

class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BindableViewHolder {
    constructor(parent: ViewGroup) : this(
        LayoutInflater.from(parent.context).inflate(TODO() as Int, parent, false)
    )

    override fun bind(context: Context, item: UiItem) {
        TODO("Not yet implemented")
    }
}

class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BindableViewHolder {
    constructor(parent: ViewGroup) : this(
        LayoutInflater.from(parent.context).inflate(TODO() as Int, parent, false)
    )

    override fun bind(context: Context, item: UiItem) {
        TODO("Not yet implemented")
    }
}
