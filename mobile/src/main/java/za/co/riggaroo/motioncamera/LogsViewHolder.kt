package za.co.riggaroo.motioncamera

import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.FirebaseStorage


/**
 * @author rebeccafranks
 * @since 2017/09/20.
 */
class LogsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val imageViewLog: ImageView = view.findViewById(R.id.image_view_screenshot)
    private val timeStampTextView: TextView = view.findViewById(R.id.text_view_timestamp)
    private val facesDetectedTextView: TextView = view.findViewById(R.id.text_view_faces)

    fun setLog(log: FirebaseImageLog) {
        log.timestamp?.let {
            val timeDifference = DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
            timeStampTextView.text = timeDifference

        }
        log.imageRef?.let {
            Glide.with(imageViewLog.context)
                    .using(FirebaseImageLoader())
                    .load(FirebaseStorage.getInstance().getReference(it))
                    .into(imageViewLog)
        }
        log.containsFace?.let {
            facesDetectedTextView.text = if (it) {
                facesDetectedTextView.context.getString(R.string.faces_detected)
            } else {
                facesDetectedTextView.context.getString(R.string.no_faces_detected)
            }
        }


    }

}