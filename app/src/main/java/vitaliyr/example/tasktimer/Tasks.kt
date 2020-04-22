package vitaliyr.example.tasktimer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Tasks(val name: String, val description: String, val sortOrder: Int, var id: Long = 0): Parcelable