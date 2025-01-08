package com.oreca.subjectsegmentationdemo

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.oreca.subjectsegmentationdemo.databinding.ActivityMainBinding
import java.io.FileNotFoundException
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var curImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectImage.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.setType("image/*")
            startActivityForResult(photoPickerIntent, 134)
        }

        binding.btnRemoveBackground.setOnClickListener {
            removeBackground()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try {
                val imageUri = data?.data
                curImageUri = imageUri
                val imageStream = contentResolver.openInputStream(imageUri!!)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                binding.curImage.setImageBitmap(selectedImage)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this@MainActivity, "You haven't picked Image", Toast.LENGTH_LONG).show()
        }
    }

    private fun removeBackground() {
        curImageUri?.let {
            binding.pbLoading.visibility = View.VISIBLE
            val image: InputImage
            try {
                image = InputImage.fromFilePath(this@MainActivity, it)
                val options = SubjectSegmenterOptions.Builder()
                    .enableForegroundBitmap()
                    .build()
                val segmenter = SubjectSegmentation.getClient(options)
                segmenter.process(image)
                    .addOnSuccessListener { result ->
                        binding.pbLoading.visibility = View.GONE
                        val foregroundBitmap = result.foregroundBitmap
                        binding.curImage.setImageBitmap(foregroundBitmap)
                        Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        binding.pbLoading.visibility = View.GONE
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "Failure", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}