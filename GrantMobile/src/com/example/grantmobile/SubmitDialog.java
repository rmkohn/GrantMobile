package com.example.grantmobile;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class SubmitDialog extends DialogFragment {
	public int userID;
	public int workmonthID;
	
	public void setUserID(int id){
		this.userID = id;
	}
	
	public void setWorkmonthID(int id) {
		this.workmonthID = id;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Submit Timesheet")
        	   .setMessage("Click Approve or Disapprove for this timesheet.")
               .setPositiveButton("Approve", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int id) {
				    	  CommentDialog comment = new CommentDialog();
				    	  comment.setTitle("Approve Time");
				    	  comment.setUserID(userID);
				    	  comment.setWorkMonthID(workmonthID);
				    	  comment.setApproval(true);
				    	  comment.show(getFragmentManager(), "");
				      }
				  })
		        .setNeutralButton("Disapprove", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int id) {
				    	  CommentDialog comment = new CommentDialog();
				    	  comment.setTitle("Disapprove Time");
				    	  comment.setUserID(userID);
				    	  comment.setWorkMonthID(workmonthID);
				    	  comment.setApproval(false);
				    	  comment.show(getFragmentManager(), "");
				      }
				  })
        
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				
			}
		});
        // Create the AlertDialog object and return it
        return builder.create();
    }

}