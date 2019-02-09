package com.stuartharrison.obdiiscanner.Dialogues;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.stuartharrison.obdiiscanner.Managers.prefManager;
import com.stuartharrison.obdiiscanner.R;

/**
 * The dialog which is displayed to the user if a, the application has started for the first time
 * or b, the user has not yet accepted the terms and agreement.
 *
 * @author Stuart Harrison
 * @version 1.0
 */
public class AgreementDialog extends DialogFragment {

    /** Holds an instance of the prefManager object for modifying the applications
     * shared preferences  */
    private prefManager prefManager;

    /** Setter property for setting the prefManager object variable in the class */
    public void setPrefManager(prefManager value) { this.prefManager = value; }

    /**
     * Creates a Dialog with a set message asking the user to agree to the terms and
     * conditions of the application. The user can either agree to them or not. Disagreeing to the
     * terms will disable certain features within the application.
     * @param savedInstanceState
     * @return Returns the created Dialog with the two options available to the user.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Create Dialog and set the title
        AlertDialog.Builder aboutDialogue = new AlertDialog.Builder(getActivity());
        aboutDialogue.setTitle("Harrison OBD Scanner Agreement");
        aboutDialogue.setMessage(R.string.ADiaMessage) //Set message from the Strings resource file
                .setPositiveButton(R.string.ADiaAgree, new DialogInterface.OnClickListener() {
                    //Positive button will enable the features
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefManager.setApplicationUsageAgreement(true); //Set the preference to true
                    }
                })
                .setNegativeButton(R.string.ADiaDisagree, new DialogInterface.OnClickListener() {
                    //Negative button will disable the features
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefManager.setApplicationUsageAgreement(false);
                    }
                });
        return aboutDialogue.create(); //Return created dialog for display
    }
}
