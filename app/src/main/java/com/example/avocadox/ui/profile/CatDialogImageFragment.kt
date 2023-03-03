package com.example.avocadox.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.avocadox.R
import com.example.avocadox.Util
import java.io.File
import java.lang.reflect.InvocationTargetException

class CatDialogImageFragment: DialogFragment(), DialogInterface.OnClickListener  {

    private lateinit var addImgDialogView: View
    private val imgFileName = "cat_img.jpg"
    private lateinit var imgFile: File
    private lateinit var imgUri: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>

    private lateinit var catProfileViewModel: CatProfileViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        imgFile = File(requireContext().getExternalFilesDir(null), imgFileName)
        imgUri = FileProvider.getUriForFile(requireContext(), "com.example.avocadox.ui", imgFile)

        catProfileViewModel = ViewModelProvider(requireActivity())[CatProfileViewModel::class.java]

        handleResults()

        lateinit var ret: Dialog
        val builder = AlertDialog.Builder(requireActivity())
        addImgDialogView =
            requireActivity().layoutInflater.inflate(
                R.layout.fragment_image_dialog_cat,
                null)
        builder.setView(addImgDialogView)
        builder.setTitle("Select Profile Image")
        builder.setNegativeButton("cancel", this)

        val takeFromCameraButton: TextView =
            addImgDialogView.findViewById(R.id.takeCameraPhotoText)
        takeFromCameraButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)    // value = image URI
            cameraResult.launch(intent)
        }

        val getImageGalleryButton: TextView =
            addImgDialogView.findViewById(R.id.getImageGalleryText)
        getImageGalleryButton.setOnClickListener {
            Log.d("Dialog Fragment", "Opening gallery to select image")
            val intent: Intent
            try {
                intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                Intent.createChooser(intent, "Select Picture")
                println("Launching ACTION_PICK intent")
                galleryResult.launch(intent)
            } catch (exception: InvocationTargetException) {
                Log.d("InvocationTargetException", exception.toString())
            }
        }
        ret = builder.create()
        return ret
    }

    private fun handleResults() {
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            if (it.resultCode == Activity.RESULT_OK) {
                Log.d("Camera Result", "RESULT_OK")
                val bitmap = Util.getBitmap(requireActivity(), imgUri)
                catProfileViewModel.image.value = bitmap
                Log.d("Camera Result Bitmap", bitmap.toString())
                this.dismiss()
            }
        }

        galleryResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    Log.d("Gallery Result", "RESULT_OK")
                    try {
                        val data: Intent? = it.data
                        if (data != null) {
                            Log.d("Gallery Result data", data.toString())
                            val selectedUri = data.data
                            println(selectedUri)
                            if (selectedUri != null) {
                                val bitmap = Util.getBitmap(requireContext(), selectedUri)
                                catProfileViewModel.image.value = bitmap
                            }
                        }
                    } catch (exception: InvocationTargetException) {
                        Log.d("InvocationTargetException", exception.toString())
                    }
                    this.dismiss()
                }
            }
    }

    override fun onClick(dialog: DialogInterface, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
        }
    }
}