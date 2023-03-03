package com.example.avocadox.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.avocadox.R
import com.example.avocadox.database.UserEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatsFragment : Fragment() {

    private lateinit var userRecylerView: RecyclerView
    private lateinit var userList: ArrayList<UserEntry>
    private lateinit var adapter: UserAdapter

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        userList = ArrayList()
        adapter = UserAdapter(requireActivity(), userList)

        userRecylerView = view.findViewById(R.id.user_recycler_view)
        userRecylerView.layoutManager = LinearLayoutManager(requireActivity())
        userRecylerView.adapter = adapter

        mDbRef.child("user").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(UserEntry::class.java)

                    // Add every user to the list except for the logged in user
                    if (mAuth.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return view
    }

}