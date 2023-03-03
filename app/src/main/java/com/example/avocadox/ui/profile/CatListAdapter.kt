package com.example.avocadox.ui.profile

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.avocadox.R

class CatListAdapter(
    private val context: Activity,
    private val setting_options: Array<String>,
    private val drawableIds: Array<Int>,
    private val valueList: Array<String>
) : BaseAdapter(){
    override fun getCount(): Int {
        return setting_options.size
    }

    override fun getItem(position: Int): Any {
        return setting_options[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {

        val view = View.inflate(context, R.layout.listview_cat_options, null)

        val imgIcon = view.findViewById<ImageView>(R.id.imgIcon)
        val textView = view.findViewById<TextView>(R.id.textview)
        val textView2 = view.findViewById<TextView>(R.id.valuetextview)

        imgIcon.setImageResource(drawableIds[position])
        textView.text = setting_options[position]
        textView2.text = valueList[position]

        return view
    }

}
