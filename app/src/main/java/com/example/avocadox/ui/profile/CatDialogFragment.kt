package com.example.avocadox.ui.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

class CatDialogFragment(private val onClickListener: OnClickListener): DialogFragment(), DialogInterface.OnClickListener  {
    companion object {
        const val USERNAME_DIALOG = "Name"
        const val CATEGORY_DIALOG = "Category"
        const val GENDER_DIALOG = "Gender"
        const val AGE_DIALOG = "Age"
        const val WEIGHT_DIALOG = "Weight"
        const val PET_FOOD_DIALOG = "Pet's food"
        const val LOVE_EXPERIENCE_DIALOG = "Love experience"
        const val DIALOG_KEY = "cat_key"
    }
    private val breedList = arrayOf("American Shorthair", "Bengal", "Exotic", "Japanese Bobtail", "Persian", "Ragdoll", "Others")
    private val genderList = arrayOf("Male", "Female")
    private val loveExperienceList = arrayOf("Child", "First time in love", "Experienced in loving", "Professional Dad")
    private var selectedItemIndex = 0

    private var selectedBreed = breedList[selectedItemIndex]
    private var selectedGender = genderList[selectedItemIndex]
    private var selectedloveExperience = loveExperienceList[selectedItemIndex]

    private lateinit var catViewModel: CatProfileViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        catViewModel = ViewModelProvider(requireActivity())[CatProfileViewModel::class.java]

        val bundle = arguments
        val builder = AlertDialog.Builder(requireActivity())

        val entryName = bundle?.getString(DIALOG_KEY)
        val input = EditText(requireActivity())

        when (entryName) {
            USERNAME_DIALOG -> {
                input.inputType = InputType.TYPE_CLASS_TEXT
            }
            CATEGORY_DIALOG -> {
                builder.setSingleChoiceItems(breedList, selectedItemIndex) { _, which ->
                    selectedBreed = breedList[which]
                }
            }
            GENDER_DIALOG -> {
                builder.setSingleChoiceItems(genderList, selectedItemIndex) {_, which ->
                    selectedGender = genderList[which]
                }
            }
            AGE_DIALOG -> {
                input.inputType = InputType.TYPE_CLASS_NUMBER
            }
            WEIGHT_DIALOG -> {
                input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
            PET_FOOD_DIALOG -> {
                input.inputType = InputType.TYPE_CLASS_TEXT
            }
            LOVE_EXPERIENCE_DIALOG -> {
                builder.setSingleChoiceItems(loveExperienceList, selectedItemIndex) {_, which ->
                    selectedloveExperience = loveExperienceList[which]
                }
            }
        }

        val sharedPreference =  requireActivity().getSharedPreferences("sharedCatInfo", MODE_PRIVATE)
        val editor = sharedPreference.edit()

        builder.setTitle(entryName)
            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                val text = input.text.toString()
                when (entryName) {
                    USERNAME_DIALOG -> {
                        editor.putString("sharedUsername", text)
                        onClickListener.onClick(text, entryName)
                    }
                    AGE_DIALOG -> {
                        editor.putLong("sharedAge", text.toLong())
                        onClickListener.onClick(text, entryName)
                    }
                    WEIGHT_DIALOG -> {
                        editor.putString("sharedWeight", text)
                        onClickListener.onClick(text, entryName)
                    }
                    PET_FOOD_DIALOG -> {
                        editor.putString("sharedPetFood", text)
                        onClickListener.onClick(text, entryName)
                    }
                    CATEGORY_DIALOG -> {
                        editor.putString("sharedCategory", selectedBreed)
                        println("this is $selectedBreed")
                        onClickListener.onClick(selectedBreed, entryName)
                    }
                    GENDER_DIALOG -> {
                        editor.putString("sharedGender", selectedGender)
                        onClickListener.onClick(selectedGender, entryName)
                    }
                    LOVE_EXPERIENCE_DIALOG -> {
                        editor.putString("sharedLoveExperience", selectedloveExperience)
                        onClickListener.onClick(selectedloveExperience, entryName)
                    }
                }
                editor.commit()
            }
            .setNegativeButton("Cancel",this)

        if (entryName == USERNAME_DIALOG || entryName == WEIGHT_DIALOG || entryName == AGE_DIALOG || entryName == PET_FOOD_DIALOG) {
            builder.setView(input)
        }

        return builder.create()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
    }

    class OnClickListener(val clickListener: (input: String, type: String) -> Unit) {
        fun onClick(unit: String, unitType: String) = clickListener(unit, unitType)
    }
}

