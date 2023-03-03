package com.example.avocadox

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.avocadox.database.UserEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editRadioGroup: RadioGroup
    private lateinit var btnSignup: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private var gender = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide()

        // Initialize Firebase authentication
        mAuth = FirebaseAuth.getInstance()

        // Initialize views
        editName = findViewById(R.id.edit_name)
        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        editRadioGroup = findViewById(R.id.edit_gender)
        btnSignup = findViewById(R.id.btn_signup)

        btnSignup.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val idGender: Int = editRadioGroup.indexOfChild(findViewById(editRadioGroup.checkedRadioButtonId))

            signup(name, email, password, idGender)
        }

        btnSignup.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val idGender: Int = editRadioGroup.indexOfChild(findViewById(editRadioGroup.checkedRadioButtonId))

            signup(name, email, password, idGender)
        }
    }

    private fun signup(name: String, email: String, password: String, idGender: Int) {
        if (name.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (idGender == -1) {
            Toast.makeText(this@SignUpActivity, "Gender cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        gender = if (idGender == 0) {
            "Male"
        } else {
            "Female"
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addUserToDatabase(mAuth.currentUser?.uid!!, name, email, gender)
                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SignUpActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToDatabase(uid: String, name: String, email: String, gender: String) {
        val user = UserEntry(uid,name, email, gender)
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(uid).setValue(user)
    }

}