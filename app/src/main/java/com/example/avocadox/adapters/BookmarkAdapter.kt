package com.example.avocadox.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.avocadox.R
import com.example.avocadox.database.BookmarkEntry

class BookmarkAdapter(private val context: Context, private var entryList: List<BookmarkEntry>) : BaseAdapter() {

    override fun getCount(): Int {
        return entryList.size
    }

    override fun getItem(position: Int): Any {
        return entryList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        Log.d("Bookmark Adapter getView", "entryList[${position}]: ${entryList[position]}")
        val view: View = View.inflate(context, R.layout.bookmark_entry, null)

        val nameText: TextView = view.findViewById(R.id.bookmark_name_text)
        nameText.text = entryList[position].name

        val locationText: TextView = view.findViewById((R.id.bookmark_location_text))
        val str = "Address: ${entryList[position].address}"
        locationText.text = str

        val commentText: TextView = view.findViewById(R.id.bookmark_comment_text)
        commentText.text = entryList[position].comment

        return view
    }

    fun replace(newBookmarkEntry: List<BookmarkEntry>) {
        entryList = newBookmarkEntry
    }
}
