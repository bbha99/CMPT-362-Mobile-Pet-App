package com.example.avocadox.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.avocadox.Util
import com.example.avocadox.database.CatEntry
import com.example.avocadox.databinding.CatPersonalMomentsListviewBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

class MyCatAdapter(): ListAdapter<CatEntry, MyCatAdapter.CatEntryViewHolder>(DiffCallback){
    private var storageJob = Job()
    private val jobScope = CoroutineScope(Dispatchers.Main + storageJob)

    private lateinit var mListener: onItemClickListener
    interface onItemClickListener{
        fun onItemClick(cat: CatEntry)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class CatEntryViewHolder(private var binding: CatPersonalMomentsListviewBinding, private val jobScope: CoroutineScope): RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: CatEntry, position: Int){
            if (entry.name == "") {
                binding.catName.text = "Cat #${position+1}"
            } else {
                binding.catName.text = entry.name
            }
            binding.executePendingBindings()
            jobScope.launch{
                setPostImage(binding.imageProfile, entry.imageUrl)
            }
        }

        private suspend fun setPostImage(imageView: ImageView, imageUrl: String){
            withContext(Dispatchers.Main){
                val imageBytes = imageResult(imageUrl)
                if(imageBytes.isSuccessful){
                    val image = Util.toBitmap(imageBytes.result)
                    imageView.setImageBitmap(image)
                }
            }
        }

        private suspend fun imageResult(imageUrl: String): Task<ByteArray> {
            return withContext(Dispatchers.IO){
                var storage = FirebaseStorage.getInstance().reference
                val imageBytes = storage.child("catProfile").child(imageUrl).getBytes(100000000)
                while(!imageBytes.isComplete){ } //Wait for request to complete
                imageBytes
            }
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<CatEntry>(){
        override fun areItemsTheSame(oldItem: CatEntry, newItem: CatEntry): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: CatEntry, newItem: CatEntry): Boolean {
            return oldItem.key == newItem.key
        }
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatEntryViewHolder {
        // inflates the card_view_design view that is used to hold list item
        return CatEntryViewHolder(CatPersonalMomentsListviewBinding.inflate(LayoutInflater.from(parent.context)), jobScope)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: CatEntryViewHolder, position: Int) {
        val entry = getItem(position)
        holder.itemView.setOnClickListener{
            mListener.onItemClick(entry)
        }
        holder.bind(entry, position)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        storageJob.cancel()
    }
}