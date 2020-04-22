package vitaliyr.example.tasktimer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.settings_dialog.*
import java.lang.IndexOutOfBoundsException
import java.util.*

private const val TAG = "SettingsDialog"
const val SETTINGS_FIRST_DAY_OF_WEEK = "FirstDay"
const val SETTINGS_IGNORE_LESS_THAN = "IgnoreLessThan"
const val SETTINGS_DEFAULT_IGNORE_LESS_THAN = 0

private val deltas = intArrayOf(
    0,
    5,
    10,
    15,
    20,
    25,
    30,
    35,
    40,
    45,
    50,
    55,
    60,
    180,
    120,
    240,
    300,
    360,
    420,
    480,
    540,
    600,
    900,
    1800,
    2700
)

class SettingsDialog : AppCompatDialogFragment() {

    private val defaultFirstDayOfWeek = GregorianCalendar(Locale.getDefault()).firstDayOfWeek
    private var firstDay = defaultFirstDayOfWeek
    private var ignoreLessThan = SETTINGS_DEFAULT_IGNORE_LESS_THAN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(AppCompatDialogFragment.STYLE_NORMAL, R.style.SettingsDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, "onCreateView: starts")
        return inflater.inflate(R.layout.settings_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: starts")
        super.onViewCreated(view, savedInstanceState)

        dialog?.setTitle(R.string.action_settings)

        ignoreSeconds.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < 12){
                    ignoreSecondsTitle.text = getString(R.string.settingsIgnoreSecondsTitle,
                    deltas[progress],
                    resources.getQuantityString(R.plurals.settingsLittleUnits, deltas[progress]))
                } else{
                    val minutes = deltas[progress] / 60
                    ignoreSecondsTitle.text = getString(R.string.settingsIgnoreSecondsTitle,
                    minutes,
                    resources.getQuantityString(R.plurals.settingsBigUnits, minutes))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Don't need this one
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //Nor this one
            }
        })

        okButton.setOnClickListener {
            saveValues()
            dismiss()
        }

        cancelButton.setOnClickListener { dismiss() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored: starts")
        super.onViewStateRestored(savedInstanceState)

        readValues()

        firstDaySpinner.setSelection(firstDay - GregorianCalendar.SUNDAY)

        val seekBarValue = deltas.binarySearch(ignoreLessThan)
        if (seekBarValue < 0) {
            throw IndexOutOfBoundsException("Value $seekBarValue is not found in deltas array")
        }
        ignoreSeconds.max = deltas.size - 1
        ignoreSeconds.progress = seekBarValue

        if (ignoreLessThan < 60) {
            ignoreSecondsTitle.text = getString(
                R.string.settingsIgnoreSecondsTitle, ignoreLessThan,
                resources.getQuantityString(R.plurals.settingsLittleUnits, ignoreLessThan)
            )
        } else{
            val minutes = ignoreLessThan / 60
            ignoreSecondsTitle.text = getString(R.string.settingsIgnoreSecondsTitle, minutes,
            resources.getQuantityString(R.plurals.settingsBigUnits, minutes))
        }
    }

    private fun readValues() {
        with(PreferenceManager.getDefaultSharedPreferences(context)) {
            firstDay = getInt(SETTINGS_FIRST_DAY_OF_WEEK, defaultFirstDayOfWeek)
            ignoreLessThan = getInt(SETTINGS_IGNORE_LESS_THAN, SETTINGS_DEFAULT_IGNORE_LESS_THAN)

            Log.d(TAG, "Retrieving first day: $firstDay, and ignoreLessThan: $ignoreLessThan")
        }
    }

    private fun saveValues() {
        val newFirstDay = firstDaySpinner.selectedItemPosition + GregorianCalendar.SUNDAY
        val newIgnoreLessThan = deltas[ignoreSeconds.progress]

        Log.d(TAG, "Saving first day: $newFirstDay, ignore seconds: $newIgnoreLessThan")

        with(PreferenceManager.getDefaultSharedPreferences(context).edit()) {
            if (newFirstDay != firstDay) {
                putInt(SETTINGS_FIRST_DAY_OF_WEEK, newFirstDay)
            }
            if (newIgnoreLessThan != ignoreLessThan) {
                putInt(SETTINGS_IGNORE_LESS_THAN, newIgnoreLessThan)
            }
            apply()
        }

    }
}