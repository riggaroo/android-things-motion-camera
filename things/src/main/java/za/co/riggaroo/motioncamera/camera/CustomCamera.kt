package za.co.riggaroo.motioncamera.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.camera2.*
import android.hardware.camera2.CameraAccessException.CAMERA_ERROR
import android.media.ImageReader
import android.os.Handler
import android.util.Log


class CustomCamera : AutoCloseable {

    private var mImageReader: ImageReader? = null
    private var mCameraDevice: CameraDevice? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private lateinit var imageCapturedListener: ImageCapturedListener


    interface ImageCapturedListener {
        fun onImageCaptured(bitmap: Bitmap)
    }

    fun initializeCamera(context: Context, backgroundHandler: Handler, imageListener: ImageCapturedListener) {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val camIds: Array<String>
        try {
            camIds = manager.cameraIdList
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Cam access exception getting ids", e)
            throw CameraAccessException(CAMERA_ERROR, "Cam access exception getting ids")
        }
        if (camIds.isEmpty()) {
            Log.e(TAG, "No cameras found")
            throw CameraAccessException(CAMERA_ERROR, "No Cameras found")
        }

        val id = camIds[0]
        mImageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT,
                ImageFormat.JPEG, MAX_IMAGES)
        imageCapturedListener = imageListener
        mImageReader?.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)
        try {
            manager.openCamera(id, mStateCallback, backgroundHandler)
        } catch (cae: Exception) {
            Log.e(TAG, "Camera access exception", cae)
        }
    }

    private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage()
        val imageBuffer = image.planes[0].buffer
        val imageBytes = ByteArray(imageBuffer.remaining())
        imageBuffer.get(imageBytes)
        image.close()
        val bitmap = getBitmapFromByteArray(imageBytes)
        imageCapturedListener.onImageCaptured(bitmap)
    }


    fun takePicture() {
        mCameraDevice?.createCaptureSession(
                arrayListOf(mImageReader?.surface),
                mSessionCallback,
                null)
    }

    private fun getBitmapFromByteArray(imageBytes: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        //For some reason the bitmap is rotated the incorrect way
        matrix.postRotate(180f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun triggerImageCapture() {
        val captureBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureBuilder?.addTarget(mImageReader!!.surface)

        captureBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        captureBuilder?.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
        Log.d(TAG, "Session initialized.")
        mCaptureSession?.capture(captureBuilder?.build(), mCaptureCallback, null)
    }

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureProgressed(session: CameraCaptureSession?, request: CaptureRequest?, partialResult: CaptureResult?) {
            Log.d(TAG, "Partial result")
        }

        override fun onCaptureFailed(session: CameraCaptureSession?, request: CaptureRequest?, failure: CaptureFailure?) {
            Log.d(TAG, "Capture session failed")
        }

        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            session?.close()
            mCaptureSession = null
            Log.d(TAG, "Capture session closed")
        }
    }

    private val mSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession?) {
            Log.w(TAG, "Failed to configure camera")
        }

        override fun onConfigured(cameraCaptureSession: CameraCaptureSession?) {
            if (mCameraDevice == null) {
                return
            }
            mCaptureSession = cameraCaptureSession
            triggerImageCapture()
        }

    }

    private val mStateCallback = object : CameraDevice.StateCallback() {
        override fun onError(cameraDevice: CameraDevice, code: Int) {
            Log.d(TAG, "Camera device error, closing")
            cameraDevice.close()
        }

        override fun onOpened(cameraDevice: CameraDevice) {
            Log.d(TAG, "Opened camera.")
            mCameraDevice = cameraDevice
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            Log.d(TAG, "Camera disconnected, closing")
            cameraDevice.close()
        }

        override fun onClosed(camera: CameraDevice) {
            Log.d(TAG, "Closed camera, releasing")
            mCameraDevice = null
        }
    }

    override fun close() {
        mCameraDevice?.close()
    }

    companion object InstanceHolder {
        val IMAGE_WIDTH = 640
        val IMAGE_HEIGHT = 480
        val MAX_IMAGES = 1
        val TAG = "CustomCamera"
        private val mCamera = CustomCamera()

        fun getInstance(): CustomCamera {
            return mCamera
        }
    }
}