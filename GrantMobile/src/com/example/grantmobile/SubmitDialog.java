package com.example.grantmobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class SubmitDialog extends DialogFragment {

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Submit Timesheet")
        	   .setMessage("Click Approve or Deny for this timesheet. User ID: ")
               .setPositiveButton("Approve", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int id) {
				    	  CommentDialog comment = new CommentDialog();
				    	  comment.setTitle("Approve Time");
					      comment.show(getFragmentManager(), "");
				      }
				  })
		        .setNeutralButton("Deny", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int id) {
				    	  CommentDialog comment = new CommentDialog();
				    	  comment.setTitle("Deny Time");
					      comment.show(getFragmentManager(), "");
				      }
				  });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}