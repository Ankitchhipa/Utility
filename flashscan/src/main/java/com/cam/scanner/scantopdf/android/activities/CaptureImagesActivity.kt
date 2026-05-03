package com.cam.scanner.scantopdf.android.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.ImageCaptureException
import com.bumptech.glide.Glide
import com.cam.scanner.scantopdf.android.R
import com.cam.scanner.scantopdf.android.databinding.CameraPreviewLayoutBinding

class CaptureImagesActivity : BaseActivity() {

    private val TAG = CaptureImagesActivity::class.java.simpleName
    lateinit var binding: CameraPreviewLayoutBinding

    lateinit var imageCaptureHelper: ImageCaptureHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CameraPreviewLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCameraHelper()
        clickListeners()

    }

    private fun clickListeners() {
        binding.apply {
            ivCapture.setOnClickListener {
                imageCaptureHelper.captureImage()
            }
            tvDone.setOnClickListener {
                if (imageCaptureHelper.capturedImagesPaths.isNotEmpty()) {
                    val data = Intent()
                    data.putExtra("cam_paths", imageCaptureHelper.capturedImagesPaths)
                    setResult(RESULT_OK, data)
                    super.finish()
                } else {
                    Toast.makeText(
                        this@CaptureImagesActivity,
                        getString(R.string.capture_limit),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun initCameraHelper() {
        imageCaptureHelper = ImageCaptureHelper(
            this,
            this@CaptureImagesActivity,
            binding.previewView,
            object : ImageCaptureHelper.OnImageCaptureListener {
                override fun onImageCaptured(uri: Uri) {
                    // Handle captured image URI
                    Log.i(TAG, "onImageCaptured::${imageCaptureHelper.capturedImagesPaths}")
                    if (imageCaptureHelper.capturedImagesPaths.isNotEmpty()) {
                        if (binding.rlImages.visibility == View.GONE) {
                            binding.rlImages.visibility = View.VISIBLE
                        }
                        binding.tvImagesCount.text =
                            imageCaptureHelper.capturedImagesPaths.size.toString()
                        Glide.with(applicationContext)
                            .load(uri)
                            .into(binding.ivImages)
                    }
                }

                override fun onImageCaptureError(exception: ImageCaptureException) {
                    // Handle image capture error
                    Log.i(TAG, "onImageCaptureError::${exception.imageCaptureError}")
                }

                override fun onImageCaptureComplete() {
                    // Handle image capture completion
                    Log.i(TAG, "onImageCaptureComplete")
                }
            },
            this.mainExecutor
        )
        imageCaptureHelper.startCamera()
    }

    override fun onDestroy() {
        if (::imageCaptureHelper.isInitialized) {
            imageCaptureHelper.clearCapturedImages()
        }
        super.onDestroy()
    }
}
