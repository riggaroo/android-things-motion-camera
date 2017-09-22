package za.co.riggaroo.motioncamera

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author rebeccafranks
 * @since 2017/09/20.
 */
class LogsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val imageViewLog: ImageView = view.findViewById(R.id.image_view_screenshot)
    private val timeStampTextView: TextView = view.findViewById(R.id.text_view_timestamp)

    fun setLog(log: FirebaseImageLog) {
        log.timestamp?.let {
            val date = Date(it)
            val dateFormatted = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.UK)
            timeStampTextView.text = dateFormatted.format(date)

        }
        log.imageRef?.let {
            Glide.with(imageViewLog.context)
                    .using(FirebaseImageLoader())
                    .load(FirebaseStorage.getInstance().getReference(it))
                    .into(imageViewLog)
        }

    }

}