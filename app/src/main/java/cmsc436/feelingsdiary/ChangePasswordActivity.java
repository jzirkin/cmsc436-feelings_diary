package cmsc436.feelingsdiary;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText mPasswordText;
    private EditText mConfirmText;
    private Button mSavePasswordChange;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //Allows the user to change their password
        mPasswordText = findViewById(R.id.password);
        mConfirmText = findViewById(R.id.confirm_password);
        mSavePasswordChange = findViewById(R.id.change_password_btn);
        mSavePasswordChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passwordText = mPasswordText.getText().toString();
                String confirmText = mConfirmText.getText().toString();
                if(!isPasswordValid(passwordText)){
                    mPasswordText.setError(getString(R.string.error_invalid_password));
                } else if (!passwordText.equals(confirmText)){
                    mConfirmText.setError(getString(R.string.error_passwords_dont_match));
                } else {
                    if(mAuth.getCurrentUser() != null){
                        mAuth.getCurrentUser().updatePassword(passwordText);
                    }
                }
                Toast.makeText(ChangePasswordActivity.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean isPasswordValid(String password) {
        return (password.length() > 5) &&
                (password.matches(".*[0-9].*"));
    }

}
