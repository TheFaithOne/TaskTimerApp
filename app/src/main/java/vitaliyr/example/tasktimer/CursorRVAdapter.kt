package vitaliyr.example.tasktimer

import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.task_list_item.*

class TaskViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(task: Tasks, listener: CursorRVAdapter.OnTaskClickListener) {
        til_name.text = task.name
        til_description.text = task.description
        til_edit.visibility = View.VISIBLE
        til_delete.visibility = View.VISIBLE

        til_edit.setOnClickListener {
           listener.onEditTapped(task)
        }

        til_delete.setOnClickListener {
            listener.onDeleteTapped(task)
        }

        containerView.setOnLongClickListener {
            listener.onLongTap(task)
            true
        }
    }

}

private const val TAG = "CursorRVAdapter"

class CursorRVAdapter(private var cursor: Cursor?, private val listener: OnTaskClickListener) : RecyclerView.Adapter<TaskViewHolder>() {

    interface OnTaskClickListener{
        fun onEditTapped(task: Tasks)
        fun onDeleteTapped(task: Tasks)
        fun onLongTap(task: Tasks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        Log.d(TAG, "onCreateViewHolder: new view requested")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.task_list_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: starts")
        val cursor = cursor  //To avoid problems with smart cast

        //If there was no data to display
        if (cursor == null || cursor.count == 0) {
            Log.d(TAG, "onBindViewHolder: Providing instructions")
            holder.til_name.setText(R.string.instructions_heading)
            holder.til_description.setText(R.string.instructions)
            holder.til_edit.visibility = View.GONE
            holder.til_delete.visibility = View.GONE
        } else {
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("Couldn't move cursor to position $position")
            }
            //If everything went well, create a new Task object from the data in the cursor
            val task = Tasks(
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(TasksContract.Columns.TASK_SORT_ORDER))
            )
            //Remembering that id isn't a part of the constructor
            task.id = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))

            //Now populate the view with acquired data
            holder.bind(task, listener)
        }
    }

    override fun getItemCount(): Int {
//        Log.d(TAG, "getItemCount: starts")
        val cursor = cursor
        //        Log.d(TAG, "Returning $count")
        return if (cursor == null || cursor.count == 0) {
            1
        } else {
            cursor.count
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor
     * The returned Cursor is *not* closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns previously used cursor, or null if there wasn't one.
     * If the given new Cursor is the same instance as previously set Cursor,
     * null is also returned
     */

    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursor) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            //notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            //notify the observers about the lack of data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }

}