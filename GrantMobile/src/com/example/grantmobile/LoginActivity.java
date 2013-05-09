package com.example.grantmobile;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.grantmobile.EmployeeDialog.Employee;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user
 * 
 */
public class LoginActivity extends Activity {
    
	public static final String TAG_INTENT_USERID = "userid";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private AsyncTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmployeeId;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView; 
	private TextView mLoginStatusMessageView;
	//Spinner spSpoof; //Pointer to the spinner
	
	/*************************************************************************************
	 * Was used for the spinner, loading the list of users                               *
	 *                                                                                   *
	 *************************************************************************************/
	//ArrayList <String> theList = new ArrayList <String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mEmployeeId 					= getIntent().getStringExtra("");
		mEmailView 				= (EditText) findViewById(R.id.user_id);
		mEmailView.setText(mEmployeeId);
		

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView 			= findViewById(R.id.login_form);
		mLoginStatusView 		= findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		//spSpoof 				= (Spinner)findViewById(R.id.spSpoof);
		
		//setSpoof();
		

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}
	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}
		Log.i("loginactivity", "attemptlogin");

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmployeeId = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;
		/**************************************************************************************
		 * This section needs to be modified according to the grant app password controls.    *
		 *                                                                                    *
		 **************************************************************************************/
		// Check for a valid password.
//		if (TextUtils.isEmpty(mPassword)) {
//			mPasswordView.setError(getString(R.string.error_field_required));
//			focusView = mPasswordView;
//			cancel = true;
//		} else if (mPassword.length() < 4) {
//			mPasswordView.setError(getString(R.string.error_invalid_password));
//			focusView = mPasswordView;
//			cancel = true;
//		}
		/**************************************************************************************
		 * This section needs to be modified according to the grant app user id.              *
		 *                                                                                    *
		 **************************************************************************************/
		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmployeeId)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmployeeId.matches("\\d+")) {
			mEmailView.setError(getString(R.string.error_invalid_user_id));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
		    mAuthTask = new JSONParser.RequestBuilder("http://mid-state.net/mobileclass2/android")
		    .addParam("q", "login")
		    .addParam("id", mEmployeeId)
		    .addParam("pass", mPassword)
		    .makeRequest(new JSONParser.SimpleResultHandler(this) {
		        public void onPostExecute() {
        			mAuthTask = null;
        			showProgress(false);
		        }

		        public void onSuccess(Object oResult) throws JSONException {
		        	JSONObject result = (JSONObject) oResult;
		        	Employee user = Employee.fromJson(result);
		        	String name = user.firstname + " " + user.lastname;
		            Toast.makeText(LoginActivity.this, "Logged in as " + name, Toast.LENGTH_LONG).show();
		            Log.i("loginactivity", "logged in successfully");
		            continueAsUser(user);
		        }
		        public void onFailure(String message) {
    				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
    				mPasswordView.requestFocus();
		        }
		        public void onCancelled() {
		            mAuthTask = null;
		            showProgress(false);
		        }
		    });
		}
	}
	
	public void continueAsUser(Employee user) {
		Intent i = new Intent(this, MonthSelectActivity.class);
		i.putExtra(TAG_INTENT_USERID, user);
		startActivity(i);
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
}

