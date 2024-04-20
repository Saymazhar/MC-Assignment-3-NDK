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
import com.example.imageclassification.ml.Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
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

    private fun classifyImage(image: Bitmap) {
        try {
            val model: Model = Model.newInstance(applicationContext)

            // Preprocess the image using JNI
            val scaledImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
            val intValues = IntArray(imageSize * imageSize)
            scaledImage.getPixels(
                intValues,
                0,
                scaledImage.width,
                0,
                0,
                scaledImage.width,
                scaledImage.height
            )
            preprocessImage(imageSize, imageSize, intValues)

            // Creates inputs for reference.
            val inputFeature0: TensorBuffer =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            // get 1D array of 224 * 224 pixels in image
            var pixel = 0
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val `val` = intValues[pixel++] // RGB
                    byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255f))
                    byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255f))
                    byteBuffer.putFloat((`val` and 0xFF) * (1f / 255f))
                }
            }
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs: Model.Outputs = model.process(inputFeature0)
            val outputFeature0: TensorBuffer = outputs.getOutputFeature0AsTensorBuffer()
            val confidences: FloatArray = outputFeature0.floatArray
            // find the index of the class with the biggest confidence.
            var maxPos = 0
            var maxConfidence = 0f
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i
                }
            }
            val classes = arrayOf("Bishop","King","Queen","Knight","Pawn","Rook")
            result?.text = classes[maxPos]
            var s = ""
            for (i in classes.indices) {
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100)
            }
            confidence?.text = s

            // Releases model resources if no longer used.
            model.close()
        } catch (e: IOException) {
            // Handle the exception
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            var image = data?.extras?.get("data") as Bitmap?
            if (image != null) {
                val dimension = min(image.width.toDouble(), image.height.toDouble()).toInt()
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
                imageView?.setImageBitmap(image)
                classifyImage(image)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}