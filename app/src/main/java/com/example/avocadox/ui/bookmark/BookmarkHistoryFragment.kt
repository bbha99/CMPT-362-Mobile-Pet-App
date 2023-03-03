package com.example.avocadox.ui.bookmark

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.avocadox.MainActivity
import com.example.avocadox.R
import com.example.avocadox.adapters.BookmarkAdapter
import com.example.avocadox.database.BookmarkEntry
import com.example.avocadox.database.PetBloomDatabaseRepository
import com.example.avocadox.databinding.FragmentBookmarkHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class BookmarkHistoryFragment : Fragment() {
    private lateinit var bookmarkViewModel: BookmarkViewModel

    private lateinit var historyListView: ListView
    private lateinit var historyList: ArrayList<BookmarkEntry>
    private lateinit var historyAdapter: BookmarkAdapter

    private val selectedFilter = "all"
    private var currentSearchText = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val db = Firebase.database.reference
        val mAuth = FirebaseAuth.getInstance()
        val repository = PetBloomDatabaseRepository(db, mAuth.currentUser?.uid!!)
        val viewModelFactory = BookmarkViewModelFactory(repository)
        bookmarkViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[BookmarkViewModel::class.java]

        val binding: FragmentBookmarkHistoryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bookmark_history, container, false)
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)

        historyList = ArrayList()
        historyAdapter = BookmarkAdapter(requireActivity(), historyList)
        historyListView = binding.historyList
        historyListView.adapter = historyAdapter

        bookmarkViewModel.bookmarks.observe(viewLifecycleOwner) {
            Log.d("Bookmark View Model", "Observing bookmarks")
            historyList = it
            historyAdapter.replace(it)
            historyAdapter.notifyDataSetChanged()
        }

        historyListView.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
            Log.d("History List View", "Item clicked")

            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Do you want to remove the selected bookmark ?")
            builder.setTitle("Bookmark Selected")
            builder.setCancelable(true)

            val positiveButtonClick = { _: DialogInterface, _: Int ->
                Toast.makeText(requireContext(), "Bookmark deleted", Toast.LENGTH_SHORT).show()
                Log.d("deleting bookmark with id:", position.toString())
                val key = bookmarkViewModel.bookmarks.value!![position].key
                bookmarkViewModel.deleteBookmark(key)
                Log.d("after deletion:", bookmarkViewModel.bookmarks.toString())

                historyAdapter.notifyDataSetChanged()
            }
            val negativeButtonClick = { dialog: DialogInterface, _: Int ->
                dialog.cancel()
            }

            builder.setPositiveButton("Yes", DialogInterface.OnClickListener(positiveButtonClick))
            builder.setNegativeButton("No", DialogInterface.OnClickListener(negativeButtonClick))

            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.bookmark_search_menu, menu)
        val item: MenuItem = menu.findItem(R.id.search)
        val searchView = SearchView((context as MainActivity).supportActionBar!!
            .themedContext)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_IF_ROOM)
        item.actionView = searchView

        val searchManager: SearchManager = activity!!.getSystemService(SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView.isSubmitButtonEnabled = true

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d("onCreateOptionsMenu Search Interface onQueryTextChange", "newText: $newText")
                currentSearchText = newText
                val filteredBookmark: ArrayList<BookmarkEntry> = ArrayList()

                for (bookmark in historyList) {
                    val containsName = bookmark.name.lowercase(Locale.getDefault())
                        .contains(newText.lowercase(Locale.getDefault()))
                    val containsAddress = bookmark.address.lowercase(Locale.getDefault())
                        .contains(newText.lowercase(Locale.getDefault()))
                    val containsLocation = bookmark.location.lowercase(Locale.getDefault())
                        .contains(newText.lowercase(Locale.getDefault()))

                    if (containsName || containsAddress || containsLocation) {
                        if (selectedFilter == "all") {
                            filteredBookmark.add(bookmark)
                        } else {
                            if (bookmark.name.lowercase(Locale.getDefault()).contains(selectedFilter)) {
                                filteredBookmark.add(bookmark)
                            }
                        }
                    }
                }

                historyAdapter.replace(filteredBookmark)
                historyAdapter.notifyDataSetChanged()
                historyListView.adapter = historyAdapter

                return false
            }
        })
        searchView.setOnClickListener {
        }
    }
}
