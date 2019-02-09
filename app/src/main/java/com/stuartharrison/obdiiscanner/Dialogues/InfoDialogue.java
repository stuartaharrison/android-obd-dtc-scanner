package com.stuartharrison.obdiiscanner.Dialogues;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.stuartharrison.obdiiscanner.R;

/**
 * Displays a message to the user in the event of an error or some information that is needed
 * to be displayed should occur. Version 2 is now a single class with a property for passing
 * in the desired message.
 *
 * @author Stuart Harrison
 * @version 2.0
 */
public class InfoDialogue extends DialogFragment {

    /** The message that will be displayed with the dialogue */
    private String message;

    /** The setter property for the message */
    public void setMessage(String message) { this.message = message; }

    /**
     * Creates a dialog with the appropriate message for the user. The message is based on the
     * value that should have been passed in before hand using the setMessage property.
     * @param savedInstanceState
     * @return Returns a created dialogue with correct message and buttons
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Create the dialog and set the title
        AlertDialog.Builder aboutDialogue = new AlertDialog.Builder(getActivity());
        aboutDialogue.setTitle("Harrison OBDII Scanner");
        //Assign the message to the message that should have been passed in
        aboutDialogue.setMessage(message)
                .setPositiveButton(R.string.DiaDismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //The positive button doesn't need to do anything other than close the
                        //dialog
                    }
                });
        return aboutDialogue.create();
    }
}
