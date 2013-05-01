package com.example.grantmobile;

import java.util.Arrays;
import java.util.List;

import com.example.grantmobile.GrantSelectActivity.Grant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class GrantSelectDialog extends DialogFragment {
	private List<Grant> grants;
	private GrantService.ServiceCallback<Grant> callback;
	
	public GrantSelectDialog() { }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_grant_select, null);
		ListView dialogList = (ListView)dialogView.findViewById(R.id.dialog_grant_select_list);
		if (grants == null)
			grants = Arrays.asList((Grant[])savedInstanceState.getSerializable("grants"));
		final ArrayAdapter<Grant> adapter = new ArrayAdapter<Grant>(
//			getActivity(), android.R.layout.simple_list_item_single_choice, grants);
			getActivity(), android.R.layout.simple_list_item_1, grants);
		dialogList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				callback.run((Grant)parent.getItemAtPosition(position));
				GrantSelectDialog.this.dismiss();
			}
		});
		dialogList.setAdapter(adapter);
		
		EditText dialogText = (EditText)dialogView.findViewById(R.id.dialog_grant_select_filter);
		dialogText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				adapter.getFilter().filter(s);
				adapter.notifyDataSetChanged();
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			public void afterTextChanged(Editable s) { }
		});
		
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
		.setView(dialogView)
		.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.create();
		return dialog;
	}

	public void setGrants(List<Grant> grants) {
		this.grants = grants;
	}

	public void setCallback(GrantService.ServiceCallback<Grant> callback) {
		this.callback = callback;
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putSerializable("grants", grants.toArray(new Grant[grants.size()]));
	}

}
