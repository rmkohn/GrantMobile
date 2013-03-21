package com.example.grantmobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SumbitDialog {
	private int userID;
	private Context mContext;
	
	public SumbitDialog(CalendarActivity calendarActivity) {
		mContext = calendarActivity.getApplicationContext();
	}

	public void SubmitDialog(Context context)
	{
		mContext = context;
	}
	
	public void setUserID(int inID)
	{
		userID = inID;
	}
	
	protected boolean onSubmit() 
	{
		  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		  dialog.setTitle("Submit Timesheet");
		  dialog.setMessage("Click Approve or Deny for this timesheet. User ID: " + userID);
		  dialog.setNeutralButton("Deny", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int id) {
		    	  enterComment(0);
		      }
		  });
		  dialog.setPositiveButton("Approve", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int id) {
		           enterComment(1);
		      }
		  });
		  dialog.show();
		  return true;
		}

		public void enterComment(int approve) 
		{
			String title = "Approve Time";
			if (approve == 0)
				title = "Deny Time";
			  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			  final EditText input = new EditText(mContext);
			  LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
			          LinearLayout.LayoutParams.MATCH_PARENT,
			          LinearLayout.LayoutParams.MATCH_PARENT);
			  input.setLayoutParams(lp);
			  dialog.setView(input);
			  dialog.setTitle(title);
			  dialog.setMessage("Enter a comment below and click OK to submit");
			  dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            //Make web call to update database with approve/deny status and comment
			        	dialog.cancel();
			        }
			    });
			    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            dialog.cancel();
			        }
			    });
			  dialog.show();
		}
}