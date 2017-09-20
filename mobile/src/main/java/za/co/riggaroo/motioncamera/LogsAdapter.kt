package za.co.riggaroo.motioncamera

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference

/**
 * @author rebeccafranks
 * @since 2017/09/20.
 */
class LogsAdapter(ref: DatabaseReference) : FirebaseRecyclerAdapter<FirebaseImageLog, LogsViewHolder>
(FirebaseImageLog::class.java,
        R.layout.list_item_log,
        LogsViewHolder::class.java,
        ref) {
    override fun populateViewHolder(viewHolder: LogsViewHolder, model: FirebaseImageLog, position: Int) {
        viewHolder.setLog(model)
    }

}