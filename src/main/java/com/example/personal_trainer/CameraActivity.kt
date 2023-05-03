package com.example.personal_trainer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personal_trainer.ml.PersonalTrainerModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class CameraActivity: AppCompatActivity() {

    lateinit var surfaceView: SurfaceView
    lateinit var model: PersonalTrainerModel
    lateinit var imageProcessor: ImageProcessor
    lateinit var interpreter: Interpreter
    val imageSize = 224;

    val surfaceReadyCallback = object: SurfaceHolder.Callback {

        override fun surfaceCreated(p0: SurfaceHolder) {
            startCameraPreview()
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            Log.d("Surface changed", "Surface changed")
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            TODO("Not yet implemented")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_activity)

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }
        surfaceView = findViewById(R.id.surfaceView)
        surfaceView.holder.addCallback(surfaceReadyCallback)

        imageProcessor = ImageProcessor.Builder().add(ResizeOp(224,224, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = PersonalTrainerModel.newInstance(applicationContext)

    }

    override fun onDestroy() {
        model.close()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }

        recreate()
    }

    @SuppressLint("MissingPermission")
    private fun startCameraPreview() {

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (cameraManager.cameraIdList.isEmpty()) {
            // no cameras
            return
        }

        val firstCameraId = cameraManager.cameraIdList[0]

        // after getting the id we open the camera with id firstCameraId
        // we have to override the methods
        cameraManager.openCamera(firstCameraId, object: CameraDevice.StateCallback() {
            override fun onDisconnected(p0: CameraDevice) { }
            override fun onError(p0: CameraDevice, p1: Int) { }
            override fun onOpened(cameraDevice: CameraDevice) {
                // use the camera
                // . If you wanted to be more particular about which camera to use,
                // you could call getCameraCharacteristics before opening the camera to check if it has the properties you require.
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)

                // The ScalerStreamConfigurationMap class is used to retrieve information about the available
                // output foDrmats, sizes, and other properties for the streams that can be produced by the camera device.
                // The camera device's stream configurations define the supported formats, sizes,
                // and other parameters that can be used when capturing images or videos.
                cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let { streamConfigurationMap ->
                    streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)?.let { yuvSizes ->
                        val previewSize = yuvSizes.last()
                        val displayRotation = windowManager.defaultDisplay.rotation
                        val swappedDimensions = areDimensionsSwapped(displayRotation, cameraCharacteristics)
// swap width and height if needed
                        val rotatedPreviewWidth = if (swappedDimensions) previewSize.height else previewSize.width
                        val rotatedPreviewHeight = if (swappedDimensions) previewSize.width else previewSize.height
                        surfaceView.holder.setFixedSize(rotatedPreviewWidth, rotatedPreviewHeight)

                        val imageReader = ImageReader.newInstance(rotatedPreviewWidth, rotatedPreviewHeight,
                            ImageFormat.YUV_420_888, 2)
                        imageReader.setOnImageAvailableListener({
                            imageReader.acquireLatestImage()?.let { image ->
                                // process Image
                                classifyImage(image);
                                image.close()
                            }
                        }, Handler { true })

                        val previewSurface = surfaceView.holder.surface
                        val recordingSurface = imageReader.surface


                        val captureCallback = object : CameraCaptureSession.StateCallback()
                        {
                            override fun onConfigureFailed(session: CameraCaptureSession) {}

                            override fun onConfigured(session: CameraCaptureSession) {
                                // session configured
                                val previewRequestBuilder =   cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                    .apply {
                                        addTarget(previewSurface)
                                        addTarget(recordingSurface)
                                    }
                                session.setRepeatingRequest(
                                    previewRequestBuilder.build(),
                                    object: CameraCaptureSession.CaptureCallback() {},
                                    Handler { true }
                                )
                            }
                        }
                        cameraDevice.createCaptureSession(mutableListOf(previewSurface, recordingSurface), captureCallback, Handler { true })
                    }

                }
            }
        }, Handler { true })


    }

    private fun classifyImage(rawImage: Image) {
        Log.d("classifyImage", "Classifying image")
        Log.d("classifyImage", "Preprocessing the image...")
        val image = Bitmap.createBitmap(rawImage.width, rawImage.height, Bitmap.Config.ALPHA_8)
        image.copyPixelsFromBuffer(rawImage.planes[0].buffer)

        try {
            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(imageSize * imageSize)
            image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
            var pixel = 0
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val `val` = intValues[pixel++] // RGB
                    byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255))
                    byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255))
                    byteBuffer.putFloat((`val` and 0xFF) * (1f / 255))
                }
            }
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs: PersonalTrainerModel.Outputs = model.process(inputFeature0)
            val outputFeature0: TensorBuffer = outputs.outputFeature0AsTensorBuffer
            val confidences = outputFeature0.floatArray
            // find the index of the class with the biggest confidence.
            var maxPos = 0
            var maxConfidence = 0f
            for (i in confidences.indices) {
                Log.d("confidence", "" + i)

                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i

                }
            }
            val classes = arrayOf("down", "up")
            Log.d("Classifying image", "" + classes[maxPos] + " with confidence: " + confidences[maxPos])

            // Releases model resources if no longer used.
            //model.close()
        } catch (e: IOException) {
            // TODO Handle the exception
        }

    }

    // We need to check if the orientation of the Android device and the orientation of the data being output by the camera, are swapped! It is possible that the device is oriented portrait,
 // but the camera is still outputting images which are oriented landscape according to the camera sensors.
    private fun areDimensionsSwapped(displayRotation: Int, cameraCharacteristics: CameraCharacteristics): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 90 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 0 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                // invalid display rotation
            }
        }
        return swappedDimensions
    }

}