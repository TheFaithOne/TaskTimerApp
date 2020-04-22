package vitaliyr.example.tasktimer

import android.app.Application
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "TaskTimerViewModel"
class TaskTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val contentObserver = object: ContentObserver(Handler()){
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "contentObserver.onChange called: uri is $uri")
            loadTasks()
        }
    }

    private var currentTiming: Timing? = null
    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
        get() = databaseCursor

    init {
        Log.d(TAG, " created")
        getApplication<Application>().contentResolver.registerContentObserver(TasksContract.CONTENT_URI, true, contentObserver)
        currentTiming = retrieveTiming()
        loadTasks()
    }

    private val taskTiming = MutableLiveData<String>()
    val timing: LiveData<String>
        get() = taskTiming

    private fun loadTasks(){
        val projection = arrayOf(TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESCRIPTION,
            TasksContract.Columns.TASK_SORT_ORDER)
        //<orderBy> Tasks.sortOrder, Tasks.Name
        val sortOrder = "${TasksContract.Columns.TASK_SORT_ORDER}, ${TasksContract.Columns.TASK_NAME}"
        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(
                TasksContract.CONTENT_URI,
                projection, null, null, sortOrder
            )

            databaseCursor.postValue(cursor)
        }
    }

    fun saveTask(task: Tasks): Tasks{
        val values = ContentValues()

        if(task.name.isNotEmpty()){
            values.put(TasksContract.Columns.TASK_NAME, task.name)
            values.put(TasksContract.Columns.TASK_DESCRIPTION, task.description)
            values.put(TasksContract.Columns.TASK_SORT_ORDER, task.sortOrder)

            if(task.id == 0L){
                GlobalScope.launch {
                    Log.d(TAG, "saveTask: adding new task")
                    val uri = getApplication<Application>().contentResolver?.insert(TasksContract.CONTENT_URI, values)
                    if(uri != null){
                        task.id = TasksContract.getId(uri)
                        Log.d(TAG, "saveTask: new ID is: ${task.id}")
                    }
                }
            } else{
                //task has an ID, so we're updating an existing one
                Log.d(TAG, "saveTask: updating task")
                GlobalScope.launch {
                    Log.d(TAG, "saveTask: updating task")
                    getApplication<Application>().contentResolver?.update(TasksContract.buildUriFromId(task.id), values, null, null)
                }
            }
        }
        return task
    }
    fun deleteTask(taskId: Long) {
        Log.d(TAG, "deleteTask: deleting task number $taskId")
        GlobalScope.launch {
            getApplication<Application>().contentResolver.delete(
                TasksContract.buildUriFromId(taskId),
                null,
                null
            )
        }
    }

    fun timeTask(task: Tasks){
        Log.d(TAG, "timeTask: starts")
        val timingRecord = currentTiming  //Using local variable to avoid problems with SmartCast

        if(timingRecord == null){
            //no task being timed, start timing new task
            currentTiming = Timing(task.id)
            saveTiming(currentTiming!!)
        } else {
            //we have a task being timed so save it
            timingRecord.setDuration()

            saveTiming(timingRecord)

            if(task.id == timingRecord.taskId){
                //Long tapped current task, stop timing
                currentTiming = null
            } else {
                // a new task is being timed
                val newTiming = Timing(task.id)
                saveTiming(newTiming)
                currentTiming = newTiming
            }
        }

        taskTiming.value = if(currentTiming != null) task.name else null
        Log.d(TAG, "***************************Timed task name is: ${taskTiming.value}")
    }

    private fun saveTiming(currentTiming: Timing){
        Log.d(TAG, "saveTiming: starts")

        val inserting = (currentTiming.duration == 0L)

        val values = ContentValues().apply {
            if(inserting){
                put(TimingsContract.Columns.TIMING_TASK_ID, currentTiming.taskId)
                put(TimingsContract.Columns.TIMING_START_TIME, currentTiming.startTime)
            }
            put(TimingsContract.Columns.TIMING_DURATION, currentTiming.duration)
        }

        GlobalScope.launch {
            if (inserting){
                val uri = getApplication<Application>().contentResolver.insert(TimingsContract.CONTENT_URI, values)
                if(uri != null){
                    currentTiming.id = TimingsContract.getId(uri)
                }
            } else{
                getApplication<Application>().contentResolver.update(TimingsContract.buildUriFromId(currentTiming.id), values, null, null)
            }
        }
    }

    private fun retrieveTiming() : Timing?{
        Log.d(TAG, "retrieveTiming: starts")
        val timing: Timing?

        val timingCursor: Cursor? = getApplication<Application>().contentResolver.query(
            CurrentTimingContract.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if(timingCursor != null && timingCursor.moveToFirst()){
            //We have an un-timed task
            val id = timingCursor.getLong((timingCursor.getColumnIndex(CurrentTimingContract.Columns.TIMING_ID)))
            val taskId = timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TASK_ID))
            val startTime = timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.START_TIME))
            val name = timingCursor.getString(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TASK_NAME))

            timing = Timing(id, taskId, startTime)

            //Update the liveData
//            taskTiming.value = name
        } else {
            timing = null
        }

        timingCursor?.close()
        return timing
    }

    override fun onCleared() {
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }
}