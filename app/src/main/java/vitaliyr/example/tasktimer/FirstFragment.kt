package vitaliyr.example.tasktimer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_first.*


private const val TAG = "FirstFragment"
private const val DIALOG_ID_DELETE = 1
private const val DIALOG_TASK_ID = "task_id"

class FirstFragment : Fragment(), CursorRVAdapter.OnTaskClickListener, AppDialog.DialogEvents {
    private val viewModel by lazy { ViewModelProvider(this).get(TaskTimerViewModel::class.java) }
    private val mAdapter = CursorRVAdapter(null, this)

    interface OnTaskEdit{
        fun onTaskEdit(task: Tasks)
    }

    override fun onEditTapped(task: Tasks) {
        (activity as OnTaskEdit).onTaskEdit(task)
    }

    override fun onDeleteTapped(task: Tasks) {
        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_ID_DELETE)
            putString(DIALOG_MESSAGE, getString(R.string.deldiag_message, task.id, task.name))
            putInt(DIALOG_POSITIVE_RID, R.string.delediag_positive_caption)
            putLong(DIALOG_TASK_ID, task.id)    // Passing the task ID, so we can retrieve them when deleting in onPositiveDialogResult
        }

        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(childFragmentManager, null)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: starts ")
        val taskId = args.getLong(DIALOG_TASK_ID)
        viewModel.deleteTask(taskId)
    }

    override fun onLongTap(task: Tasks) {
       viewModel.timeTask(task)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task_list.layoutManager = LinearLayoutManager(context)
        task_list.adapter = mAdapter
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        viewModel.cursor.observe(this, Observer { cursor -> mAdapter.swapCursor(cursor)?.close()})
    }
}
