package com.example.avocadox.ui.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserDialogFragment: DialogFragment(), DialogInterface.OnClickListener {
    companion object {
        const val USERNAME_DIALOG = "Username"
        const val GENDER_DIALOG = "Gender"
        const val DIALOG_KEY = "user_key"
    }
    private val genderList = arrayOf("Male", "Female")
    private var selectedItemIndex = 0

    private var selectedGender = genderList[selectedItemIndex]

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        val bundle = arguments
        val builder = AlertDialog.Builder(requireActivity())

        val entryName = bundle?.getString(DIALOG_KEY)
        val input = EditText(requireActivity())

        when (entryName) {
            USERNAME_DIALOG -> {
                input.inputType = InputType.TYPE_CLASS_TEXT
            }
            GENDER_DIALOG -> {
                builder.setSingleChoiceItems(genderList, selectedItemIndex) {_, which ->
                    selectedGender = genderList[which]
                }
            }
        }

        val sharedPreference =  requireActivity().getSharedPreferences("sharedUserInfo",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreference.edit()
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        builder.setTitle(entryName)
            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                val text = input.text.toString()
                when (entryName) {
                    USERNAME_DIALOG -> {
                        mDbRef.child("user").child(mAuth.currentUser?.uid!!).child("name").setValue(text)
                    }
                    GENDER_DIALOG -> {
                        mDbRef.child("user").child(mAuth.currentUser?.uid!!).child("gender").setValue(selectedGender)
                    }
                }
                editor.commit()
            }
            .setNegativeButton("Cancel",this)

        if (entryName == USERNAME_DIALOG) {
            builder.setView(input)
        }

        dialog = builder.create()
        return dialog
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
    }
}