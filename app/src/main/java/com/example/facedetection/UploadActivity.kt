package com.example.facedetection

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.facedetection.databinding.ActivityUploadBinding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class UploadActivity : AppCompatActivity(), UploadRequestBody.UploadCallback {

    private val binding by lazy { ActivityUploadBinding.inflate(layoutInflater) }

    private var selectedFileUri: Uri? = null

    // Define the ActivityResultLauncher
    private val fileChooserLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedFileUri = data?.data
                binding.fileView.setImageURI(selectedFileUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.fileView.setOnClickListener {
            openFileChooser()
        }
        binding.buttonUpload.setOnClickListener {
            uploadFile()
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            val mimeTypes = arrayOf("image/*", "text/*")
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        fileChooserLauncher.launch(intent)
    }


    private fun uploadFile() {
        if (selectedFileUri == null) {
            binding.layoutRoot.snackbar("Select file")
            return
        }

        // Obtain the ParcelFileDescriptor
        val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri!!, "r", null)
        if (parcelFileDescriptor == null) {
            binding.layoutRoot.snackbar("Unable to open file descriptor")
            return
        }

        try {
            // Use the ParcelFileDescriptor
            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file = File(cacheDir, contentResolver.getFileName(selectedFileUri!!))
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            // Upload the file
            binding.progressBar.progress = 0
            val body = UploadRequestBody(file, "image", this)
            MyAPI().uploadFile(
                MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    body
                ),
                RequestBody.create(MediaType.parse("multipart/form-data"), "json")
            ).enqueue(object : Callback<UploadResponse> {
                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    binding.layoutRoot.snackbar(t.message!!)
                    binding.progressBar.progress = 0
                }

                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    response.body()?.let {
                        binding.layoutRoot.snackbar(it.message)
                        binding.progressBar.progress = 100
                    }
                }
            })

        } finally {
            // Ensure that the ParcelFileDescriptor is closed
            try {
                parcelFileDescriptor.close()
            } catch (e: IOException) {
                // Handle the exception if needed
                e.printStackTrace()
            }
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        binding.progressBar.progress = percentage
    }


}