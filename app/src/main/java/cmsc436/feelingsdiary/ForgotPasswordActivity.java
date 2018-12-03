package cmsc436.feelingsdiary;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/* Activity that allows users to send themselves an email to reset their password if they
* forgot it. Only works if the email is already registered in Firebase */
public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        final EditText mEmailView = findViewById(R.id.email);
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        Button mResetPasswordButton = findViewById(R.id.reset_password);
        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Makes the keyboard go down upon button press
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (NullPointerException e) {
                    // do nothing
                }

                String email = mEmailView.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    mEmailView.setError(getString(R.string.error_field_required));
                } else {
                    mEmailView.clearFocus();

                    // Send password email
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivity.this, R.string.toast_email_sent, Toast.LENGTH_LONG).show();
                            } else {
                                mEmailView.requestFocus();
                                mEmailView.setError(getString(R.string.error_bad_email));
                            }
                        }
                    });
                }
            }
        });
    }
}
