package com.example.avocadox.adapters

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.avocadox.R
import com.example.avocadox.Util.formatDecimal
import com.example.avocadox.database.CatEntry
import com.example.avocadox.database.PostEntry

@BindingAdapter("entryList")
fun bindRecyclerView(recyclerView: RecyclerView, data: ArrayList<PostEntry>?){
    val adapter = recyclerView.adapter as PostListAdapter
    adapter.submitList(data)
}

@BindingAdapter("catList")
fun bindCatRecyclerView(recyclerView: RecyclerView, data: ArrayList<CatEntry>?){
    val adapter = recyclerView.adapter as MyCatAdapter
    adapter.submitList(data)
}

@BindingAdapter("setEmotion")
fun setEmotion(imageView: ImageView, emotion: Int){
    when(emotion){
        0 -> imageView.setImageResource(R.drawable.emotion_happy_playstore)
        1 -> imageView.setImageResource(R.drawable.emotion_meh_playstore)
        else -> imageView.setImageResource(R.drawable.emotion_sad_playstore)
    }
}

@BindingAdapter("postComment")
fun setComment(textView: TextView, comment: String){
    if(comment.isEmpty()) textView.text = "--No comment--"
    else textView.text = comment
}

@BindingAdapter("durationTravelled")
fun setDuration(textView: TextView, duration: Int){
    textView.text = "${duration} sec."
}

@BindingAdapter("distanceTravelled")
fun setDistance(textView: TextView, distance: Double){
    textView.text = "${formatDecimal(distance)} meters"
}