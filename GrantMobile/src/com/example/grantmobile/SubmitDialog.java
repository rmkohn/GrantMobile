package com.example.grantmobile;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

public class SubmitDialog extends DialogFragment {
	public int userID;
	
	public void setUserID(int id){
		this.userID = id;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Submit Timesheet")
        	   .setMessage("Click Approve or Deny for this timesheet.")
               .setPositiveButton("Approve", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int id) {
				    	  CommentDialog comment = new CommentDialog();
				    	  comment.setTitle("Approve Time");
				    	  comment.setUserID(userID);
				    	  comment.show();
				      }
				  })
		        .setNeutralButton("Deny", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int id) {
				    	  CommentDialog comment = new CommentDialog();
				    	  comment.setTitle("Deny Time");
				    	  comment.setUserID(userID);
				    	  comment.show();
				      }
				  });
        // Create the AlertDialog object and return it
        return builder.create();
    }

	public void show() {
		// TODO Auto-generated method stub
		FragmentManager manager = getFragmentManager();
		super.show(manager, "");	
	}
}