package vitaliyr.example.tasktimer

import android.util.Log
import java.util.*

private const val TAG = "Timing"
class Timing(val taskId: Long, val startTime: Long = Date().time / 1000, var id: Long = 0) {

    var duration: Long = 0
    private set

    fun setDuration(){
        //Calculate the duration from startTime to currentTime
        duration = Date().time / 1000 - startTime  //Diving by 1000 because Date().time returns milliseconds
        Log.d(TAG, "$taskId start time: $startTime | Duration: $duration")
    }
}