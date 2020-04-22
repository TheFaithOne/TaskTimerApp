package vitaliyr.example.tasktimer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_add_edit.*

private const val TAG = "AddEditFragment"

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_TASKS = "tasks"

/**
 * A simple [Fragment] subclass.
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditFragment : Fragment() {
    private var task: Tasks? = null
    private var listener: OnSaveClicked? = null
    private val viewModel by lazy { ViewModelProvider(this).get(TaskTimerViewModel::class.java) }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate is called: ")
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASKS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView is called: ")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: starts")
        if (savedInstanceState == null) {
            val task = task
            if (task != null) {
                Log.d(TAG, "onViewCreated: Task details found, editing task ${task.id}")
                addedit_name.setText(task.name)
                addedit_description.setText(task.description)
                addedit_sortorder.setText(task.sortOrder.toString())
            } else {
                //No task must be creating new task
                Log.d(TAG, "onViewCreated: No arguments, adding a new task")
            }
        }
    }

    private fun taskFromUi(): Tasks {
        val sortOrder = if (addedit_sortorder.text.isNotEmpty()) {
            Integer.parseInt(addedit_sortorder.text.toString())
        } else {
            0
        }
        val newTask =
            Tasks(addedit_name.text.toString(), addedit_description.text.toString(), sortOrder)
        newTask.id = task?.id ?: 0

        return newTask
    }

    fun isDirty(): Boolean {
        val newTask = taskFromUi()
        return ((newTask != task) && (newTask.name.isNotBlank() ||
                newTask.description.isNotBlank() ||
                newTask.sortOrder != 0)
                )
    }

    private fun saveTask() {
        //Create a newTask object with the details to be saved, then
        //call the ViewModel's save function to save it.
        //Task is now a data class, so we can compare the new details with the original task,
        //and only save if different
        val newTask = taskFromUi()
        if (newTask != task) {
            Log.d(TAG, "saveTask: saving task, id is ${newTask.id}")
            task = viewModel.saveTask(newTask)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: starts")
        super.onActivityCreated(savedInstanceState)

        if (listener is AppCompatActivity) {
            val actionBar = (listener as AppCompatActivity?)?.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        addedit_save.setOnClickListener {
            saveTask()
            listener?.onSaveClicked()
        }
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach is called: ")
        super.onAttach(context)
        if (context is OnSaveClicked) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + "must implement onSaveClicked")
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach is called: ")
        super.onDetach()
        listener = null
    }

    interface OnSaveClicked {
        fun onSaveClicked()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task The task to be edited. Null for a new task
         * @return A new instance of fragment AddEditFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(task: Tasks?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASKS, task)
                }
            }
    }
}
