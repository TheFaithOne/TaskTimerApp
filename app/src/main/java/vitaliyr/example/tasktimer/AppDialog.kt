package vitaliyr.example.tasktimer

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDialogFragment
import kotlin.ClassCastException

private const val TAG = "AppDialog"

const val DIALOG_ID = "id"
const val DIALOG_MESSAGE = "message"
const val DIALOG_POSITIVE_RID = "positive_rid"
const val DIALOG_NEGATIVE_RID = "negative_rid"


class AppDialog : AppCompatDialogFragment() {
    private var dialogEvents: DialogEvents? = null

    internal interface DialogEvents{
        //In this app we're only interested in reacting to OK button
        fun onPositiveDialogResult(dialogId: Int, args: Bundle)

//        fun onNegativeDialogResult(dialogId: Int, args: Bundle)
//        fun onDialogCanceled(dialogId: Int)
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: starts")
        super.onAttach(context)

        //Activities/Fragments must implement this interface
      dialogEvents = try {
          //Is there a parent fragment? If so, that's what we'll be calling back
          parentFragment as DialogEvents
      }
      catch (e: TypeCastException){
          try {
              //No parent fragment, so callback the Activity instead
              context as DialogEvents
          }
          catch (e: ClassCastException){
              //Activity doesn't implement the inteface
              throw ClassCastException("Activity $context must implement AppDialog.DialogEvents interface")
          }
      }
        catch (e: ClassCastException){
            //Parent fragment doesn't implement the interface
            throw ClassCastException("Fragment $parentFragment must implement AppDialog.DialogEvents interface")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog: starts")

        val builder = AlertDialog.Builder(context)
        val arguments = arguments
        val dialogId: Int
        val messageString: String?
        var positiveStringId: Int
        var negativeStringId: Int

        if(arguments != null){
            dialogId = arguments.getInt(DIALOG_ID)
            messageString = arguments.getString(DIALOG_MESSAGE)

            if(dialogId == 0 || messageString == null){
                throw IllegalArgumentException("DIALOG_ID or DIALOG_MESSAGE not presented in the bundle")
            }

            positiveStringId = arguments.getInt(DIALOG_POSITIVE_RID)
            if(positiveStringId == 0){
                positiveStringId = R.string.ok
            }

            negativeStringId = arguments.getInt(DIALOG_NEGATIVE_RID)
            if(negativeStringId == 0){
                negativeStringId = R.string.cancel
            }
        } else{
            throw IllegalArgumentException("MUST PASS DIALOG_ID and DIALOG_MESSAGE in the bundle")
        }

        return builder.setMessage(messageString)
            .setPositiveButton(positiveStringId) {dialogInterface, which ->
                // Callback positive result function
                dialogEvents?.onPositiveDialogResult(dialogId, arguments)
            }
            .setNegativeButton(negativeStringId) { dialogInterface, which ->

            }.create()
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: starts")
        super.onDetach()

        // Reset the active callbacks interface, because we're no longer attached
        dialogEvents = null
    }
}