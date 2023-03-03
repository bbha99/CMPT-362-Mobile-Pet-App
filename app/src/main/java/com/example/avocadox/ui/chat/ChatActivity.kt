package com.example.avocadox.ui.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.avocadox.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageList: ArrayList<Message>
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var mDbRef: DatabaseReference

    var senderRoom: String? = null
    var receiverRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val receiverUid = intent.getStringExtra("uid")
        val name = intent.getStringExtra("name")

        // Create a unique room for both the sender and the receiver
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        // Set the title as the sender's name
        supportActionBar?.title = name

        // Initialize fields
        messageBox = findViewById(R.id.message_box)
        sendButton = findViewById(R.id.send_btn)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        mDbRef = FirebaseDatabase.getInstance().getReference()

        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        // Add data to the recycler view
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        // Add the message to the database on button click
        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val messageObj = Message(message, senderUid)

            // Update the sender room and receiver room
            mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObj).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").push().setValue(messageObj)
                }

            // Clear the message box
            messageBox.setText("")
        }
    }

}