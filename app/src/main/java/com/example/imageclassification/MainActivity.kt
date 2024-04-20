//References
//Collecting Image Dataset and Training Model
//https://www.kaggle.com/datasets?search=image
//
//https://teachablemachine.withgoogle.com/train/image
//
//Integrating NNAPI(TFlite) with Android and writing Native code
//https://youtu.be/Aoyqt7e1AgU?si=3ptAQuZgWOGV9jOI
//
//https://youtu.be/jhGm4KDafKU?si=0bGVM-42RNGhqDeS
//
//https://developer.android.com/ndk/guides/neuralnetworks
//
//https://stackoverflow.com/questions/26096614/c-and-java-communication-for-image-processing-application
//
//https://youtu.be/Sn3YhfY5jqg?si=7nrk3ccz13II0SAu
//
//https://youtu.be/bI5_lLovoy4?si=I8Jf4k0iOqbYmZui
//
//https://www.codementor.io/@minhaz/how-to-read-an-image-file-in-c-in-android-with-ndk-1z8aqn24co

package com.example.imageclassification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    var result: TextView? = null
    var confidence: TextView? = null
    var imageView: ImageView? = null
    var picture: Button? = null
    var imageSize = 224

    // Load native library
    init {
        System.loadLibrary("native-lib")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        result = findViewById<TextView>(R.id.result)
        confidence = findViewById<TextView>(R.id.confidence)
        imageView = findViewById<ImageView>(R.id.imageView)
        picture = findViewById<Button>(R.id.button)
        picture!!.setOnClickListener {
            // Launch camera if we have permission
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 1)
            } else {
                //Request camera permission if we don't have it.
                requestPermissions(arrayOf<String>(Manifest.permission.CAMERA), 100)
            }
        }
    }

    // Native function for image preprocessing
    external fun preprocessImage(width: Int, height: Int, pixels: IntArray)

    fun classifyImage(image: Bitmap) {
        try {
            // Convert Bitmap to 1D pixel array
            val intValues = IntArray(image.width * image.height)
            image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

            // Preprocess the image using JNI
            preprocessImage(image.width, image.height, intValues)

            // Continue with the classification process
            // ...
        } catch (e: IOException) {
            // Handle the exception
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            var image = data!!.extras!!["data"] as Bitmap?
            val dimension = min(image!!.width.toDouble(), image.height.toDouble()).toInt()
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
            imageView!!.setImageBitmap(image)
            classifyImage(image!!)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
