package cmsc436.feelingsdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.CountDownLatch;

/* Activity for logging. Utilizes an AsyncTask for the logging in, as it could take more than
* a few seconds. */
public class LoginActivity extends AppCompatActivity {

    private final long TWELVE_PM = 43200000;

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mForgotPasswordView;
    private TextView mSignUpView;

    private AlarmManager mAlarmManager;

    // Firebase reference
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Create Alarm that goes off every day. Sends Intent to NotificationReminderReceiver
        // and response is handled there. This Alarm allows for notifications to occur every day
        // and while the app is not currently open.
        if (null == savedInstanceState) {
            mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(this, NotificationReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            // set alarm to send out intent every day
            // inexact because the notification isn't urgent - it's not necessary that it happens every day
            long time = getSharedPreferences("feelingsdiary", MODE_PRIVATE).getLong("notificationtime", TWELVE_PM);
            mAlarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    AlarmManager.INTERVAL_DAY + time,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }

        // Set up the login form.
        // Email
        mEmailView = findViewById(R.id.email);

        // Password
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    attemptLogin();
                }
                return true;
            }
        });

        mForgotPasswordView = findViewById(R.id.forgot_password);
        mForgotPasswordView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });


        // Log in button
        Button mLoginButton = findViewById(R.id.log_in_button);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mSignUpView = findViewById(R.id.sign_up_link);
        mSignUpView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        mLoginFormView = findViewById(R.id.email_login_form);
        mProgressView = findViewById(R.id.login_progress);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If user is already logged in, open up the real stuff
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    /* Starts the logging in chain of events. First checks for email/password validity, then
       sends the AsyncTask to do its thing.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Hides the keyboard
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException e) {
            // do nothing
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (TextUtils.isEmpty(email)) {
            mEmailView.requestFocus();
            mEmailView.setError(getString(R.string.error_field_required));
            return;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.requestFocus();
            mPasswordView.setError(getString(R.string.error_field_required));
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        mAuthTask = new UserLoginTask(email, password);
        mAuthTask.execute((Void) null);
    }

    /* Hides the UI and shows a spinning-loading bar while the AsyncTask is running */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /* AsyncTask that logs the user in given an email and password. Uses FirebaseAuth
       to accomplish this task simply.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private boolean loginSuccess;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            loginSuccess = false;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Super cool class that stops the Thread until its counted to 0
            // Lets us wait for the query before onPostExecute() goes
            final CountDownLatch latch = new CountDownLatch(1);

            mAuth.signInWithEmailAndPassword(mEmail, mPassword)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // If we log in successfully, update loginSuccess
                    if (task.isSuccessful()) {
                        setLoginSuccess();
                    }
                    // Let the thread through the "latch.await()" call
                    latch.countDown();
                }
            });

            // Wait for logging in to finish before ending the AsyncTask
            try {
                latch.await();
            } catch (InterruptedException e) {
                return false;
            }

            return loginSuccess;
        }

        private void setLoginSuccess() {
            loginSuccess = true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            // reshow the UI
            showProgress(false);

            // If successful, open the main menu
            if (success) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            // Otherwise the password did not match an email in Firebase
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

