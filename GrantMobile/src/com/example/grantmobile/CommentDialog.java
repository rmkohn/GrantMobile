package com.example.grantmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CommentDialog extends DialogFragment {
	private String title;
	public int userID;
	public int workmonthID;
	public boolean approval;
	
	public void setUserID(int id){
		this.userID = id;
	}
	
	public void setTitle(String inTitle){
		this.title = inTitle;
	}
	
	public void setApproval(boolean approval) {
		this.approval = approval;
	}
	
	public void setWorkMonthID(int id) {
		this.workmonthID = id;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());
        // store Activity for onSuccess.  Toast.makeText() was producing NPEs when called with getActivity(),
        // I can only imagine because the dialog was already closed and had disposed of its references
        // before the request returned
        final Activity currentActivity = getActivity();
  	  	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
  	  			LinearLayout.LayoutParams.MATCH_PARENT,
  	  			LinearLayout.LayoutParams.MATCH_PARENT);
  	  	input.setLayoutParams(lp);
        builder.setView(input)
        		.setTitle(this.title)
        		.setMessage("Enter a comment below and click OK to submit")
        		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int id) {
				    	  new JSONParser.RequestBuilder(GrantApp.requestURL)
				    	  .addParam("q", "approve")
		    			  .addParam("approval", Boolean.toString(approval))
				    	  .addParam("comment", input.getText().toString())
				    	  .makeRequest(new JSONParser.SimpleResultHandler<String>(currentActivity) {
				    		  public void onSuccess(String result) {
				    			  Toast.makeText(currentActivity, result, Toast.LENGTH_LONG).show();
				    			  
				    			  Intent quitIntent = new Intent(currentActivity, MainActivity.class);
				    			  quitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				    			  quitIntent.putExtra("quit", true);
				    			  currentActivity.startActivity(quitIntent);
				    		  }
				    	  });
				      }
				  })
		        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int id) {
				    	  dialog.cancel();
				      }
				  });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}