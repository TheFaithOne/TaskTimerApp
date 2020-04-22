package vitaliyr.example.tasktimer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_first.*


private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked, FirstFragment.OnTaskEdit,
    AppDialog.DialogEvents {
    //Decides whether the app is portrait or landscape i.e. 2pane mode.
    private var mTwoPane = false
    private var aboutDialog: AlertDialog? = null

    private val viewModel by lazy { ViewModelProvider(this).get(TaskTimerViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val fragment = supportFragmentManager.findFragmentById(R.id.tasks_details_container)
        if (fragment != null) {
            showEditPane()
        } else {
            tasks_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.view?.visibility = View.VISIBLE
        }

        //None of the logging placed inside the observe method appears in the logcat, so something must be wrong here.
        //Trying to update current_task.text outside of the observe function works fine.
        viewModel.timing.observe(this, Observer { timing ->
            current_task.text = if (timing != null) {
                Log.d(TAG, "***********timing is $timing")
                getString(R.string.timing_message, timing)
            } else {
                Log.d(TAG, "*********** timing is $timing")
                getString(R.string.no_task_message)
            }

            Log.d(TAG, "Still not getting any timing logging")
        })
    }

    private fun showEditPane() {
        tasks_details_container.visibility = View.VISIBLE
        // hide left pane if in single pane view
        mainFragment.view?.visibility = if (mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane: starts")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        //Set visibility of the right pane
        tasks_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
        //and show the left pane
        mainFragment.view?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: starts")
        val fragment = supportFragmentManager.findFragmentById(R.id.tasks_details_container)
        removeEditPane(fragment)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menumain_addTask -> taskEditRequest(null)
            R.id.menumain_showAbout -> showAboutDialog()
            R.id.menumain_settings -> {
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager, null)
            }
            android.R.id.home -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.tasks_details_container)
                if ((fragment is AddEditFragment) && fragment.isDirty()) {
                    showConfirmationDialog(
                        DIALOG_ID_CANCEL_EDIT,
                        getString(R.string.cancelEditDiag_message)
                    )
                    R.string.canelEditDiag_positive_caption
                    R.string.cancelEditDiag_negative_caption
                } else {
                    removeEditPane(fragment)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAboutDialog() {
        val messageView = layoutInflater.inflate(R.layout.about, null, false)
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)

        aboutDialog = builder.setView(messageView).create()
        aboutDialog?.setCanceledOnTouchOutside(true)

        val aboutVersion = messageView.findViewById(R.id.about_version) as TextView
        aboutVersion.text = BuildConfig.VERSION_NAME

        aboutDialog?.show()
    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.tasks_details_container)
        if (fragment == null || mTwoPane) {
            super.onBackPressed()
        } else {
            if ((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(
                    DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDiag_message),
                    R.string.canelEditDiag_positive_caption,
                    R.string.cancelEditDiag_negative_caption
                )
            } else {
                removeEditPane(fragment)
            }
        }
    }

    private fun taskEditRequest(task: Tasks?) {
        Log.d(TAG, "taskEditRequest: starts ")
        val newFragment = AddEditFragment.newInstance(task)
        supportFragmentManager.beginTransaction().replace(R.id.tasks_details_container, newFragment)
            .commit()

        showEditPane()
        Log.d(TAG, "tasEditRequest: ends")
    }

    override fun onTaskEdit(task: Tasks) {
        taskEditRequest(task)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: starts")
        if (dialogId == DIALOG_ID_CANCEL_EDIT) {
            val fragment = supportFragmentManager.findFragmentById(R.id.tasks_details_container)
            removeEditPane(fragment)
        }
    }

    override fun onStop() {
        super.onStop()
        if (aboutDialog?.isShowing == true) {
            aboutDialog?.dismiss()
        }
    }
}
