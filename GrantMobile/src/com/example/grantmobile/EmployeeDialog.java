package com.example.grantmobile;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.grantmobile.GrantService.GrantData;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.Toast;

public class EmployeeDialog extends SelectionDialog<EmployeeDialog.Employee> {
	public static class Employee implements Serializable {
		private static final long serialVersionUID = 0L;
		String firstname;
		String lastname;
		int id;
		
		public Employee(String firstname, String lastname, int id) {
			this.firstname = firstname;
			this.lastname = lastname;
			this.id = id;
		}
		
		public String toString() { return firstname + " " + lastname; }
		
		public static Employee fromJson(JSONObject obj) {
			try {
				return new Employee(obj.getString("firstname"), obj.getString("lastname"), obj.getInt("id"));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	GrantData data;
	int grantid = -1;

	public void onResult(Employee result) {
		// this dialog will probably be gone by the time the request returns
		final Context c = getActivity();
		new JSONParser.RequestBuilder()
		.setUrl(GrantApp.requestURL)
		.addParam("q", "sendrequest")
		.addParam("employee", String.valueOf(data.employeeid))
		.addParam("year", String.valueOf(data.year))
		.addParam("month", String.valueOf(data.month))
		.addParam("grant", String.valueOf(grantid))
		.addParam("supervisor", String.valueOf(result.id))
		.makeRequest(new JSONParser.SimpleResultHandler<Object>(c) {
			public void onSuccess(Object result) {
				Toast.makeText(c, result.toString(), Toast.LENGTH_LONG).show();
			}
			public void onFailure(String errorMessage) {
				String userMessage;
				Matcher msgMatcher = Pattern.compile("request is already (\\w+)").matcher(errorMessage);
				if (msgMatcher.find()) {
					String approvalType = msgMatcher.group(1);
					if (approvalType.equals("pending"))
						userMessage = "This grant request has already been sent, and is still awaiting approval.";
					else if (approvalType.equals("approved"))
						userMessage = "This grant request has already been approved.";
					else
						userMessage = "This grant request is already " + approvalType + ".";
				} else {
					userMessage = "An unknown error occurred: " + errorMessage;
				}
				new AlertDialog.Builder(c)
				.setTitle("Cannot send email")
				.setMessage(userMessage)
				.setCancelable(false)
				.setNeutralButton("OK", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { }
				})
				.show();
			}
		});
	}

	public void setData(GrantData data) {
		this.data = data;
	}

	public void setGrantid(int grantid) {
		this.grantid = grantid;
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		arg0.putInt("grantid", grantid);
		arg0.putSerializable("grantdata", data);
		super.onSaveInstanceState(arg0);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		grantid = grantid == -1   ? savedInstanceState.getInt("grantid") : grantid;
		data    = data    == null ? (GrantData)savedInstanceState.getSerializable("grantdata") : data;
		return super.onCreateDialog(savedInstanceState);
	}
}
