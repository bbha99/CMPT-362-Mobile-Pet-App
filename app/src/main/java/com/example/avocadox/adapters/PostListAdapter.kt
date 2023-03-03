package com.example.avocadox.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.avocadox.Util
import com.example.avocadox.database.PostEntry
import com.example.avocadox.databinding.PostEntryBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

class PostListAdapter(private val onClickListener: OnClickListener): ListAdapter<PostEntry, PostListAdapter.PostEntryViewHolder>(DiffCallback) {
    private var storageJob = Job()
    private val jobScope = CoroutineScope(Dispatchers.Main + storageJob)

    class PostEntryViewHolder(private var binding: PostEntryBinding, private val onClickListener: OnClickListener, private val jobScope: CoroutineScope): RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: PostEntry){
            binding.postEntry = entry
            binding.deleteImageButton.setOnClickListener{onClickListener.onClick(entry)}
            binding.executePendingBindings()
            binding.postImage.visibility = View.GONE
            jobScope.launch{
                setPostImage(binding.postImage, entry.imageUrl)
            }
        }

        private suspend fun setPostImage(imageView: ImageView, imageUrl: String){
            withContext(Dispatchers.Main){
                val imageBytes = imageResult(imageUrl)
                if(imageBytes.isSuccessful){
                    val image = Util.toBitmap(imageBytes.result)
                    imageView.setImageBitmap(image)
                    imageView.visibility = View.VISIBLE
                }
            }
        }

        private suspend fun imageResult(imageUrl: String): Task<ByteArray> {
            return withContext(Dispatchers.IO){
                var storage = FirebaseStorage.getInstance().reference
                val imageBytes = storage.child("postImages").child(imageUrl).getBytes(100000000)
                while(!imageBytes.isComplete){ } //Wait for request to complete
                imageBytes
            }
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<PostEntry>(){
        override fun areItemsTheSame(oldItem: PostEntry, newItem: PostEntry): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PostEntry, newItem: PostEntry): Boolean {
            return oldItem.key == newItem.key
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostEntryViewHolder{
        return PostEntryViewHolder(PostEntryBinding.inflate(LayoutInflater.from(parent.context)), onClickListener, jobScope)
    }

    override fun onBindViewHolder(holder: PostEntryViewHolder, position: Int) {
        val entry = getItem(position)
        holder.bind(entry)
    }

    class OnClickListener(val clickListener: (entry: PostEntry) -> Unit){
        fun onClick(entry: PostEntry) = clickListener(entry)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        storageJob.cancel()
    }
}