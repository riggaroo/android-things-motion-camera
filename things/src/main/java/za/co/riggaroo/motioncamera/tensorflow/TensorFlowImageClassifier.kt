package za.co.riggaroo.motioncamera.tensorflow

import android.graphics.Bitmap
import za.co.riggaroo.motioncamera.tensorflow.Classifier

/**
 * @author rebeccafranks
 * @since 2017/09/25.
 */
class TensorFlowImageDetector {
    private var detector: Classifier? = null

    fun detectImage(bitmap: Bitmap) {
        val results = detector?.recognizeImage(bitmap)

    }
}