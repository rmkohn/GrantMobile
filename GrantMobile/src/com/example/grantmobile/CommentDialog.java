package com.example.grantmobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CommentDialog extends DialogFragment {
	private String title;
	public int userID;
	
	public void setUserID(int id){
		this.userID = id;
	}
	
	public void setTitle(String inTitle){
		this.title = inTitle;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());
  	  	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
  	  			LinearLayout.LayoutParams.MATCH_PARENT,
  	  			LinearLayout.LayoutParams.MATCH_PARENT);
  	  	input.setLayoutParams(lp);
        builder.setView(input)
        		.setTitle(this.title)
        		.setMessage("Enter a comment below and click OK to submit")
        		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int id) {
				    	  Toast.makeText(getActivity(), "Grant Hours Approved! Comments: " + input.getText(), Toast.LENGTH_LONG).show();
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

	public void show() {
		FragmentManager manager = getFragmentManager();
		super.show(manager, "");
	}
}