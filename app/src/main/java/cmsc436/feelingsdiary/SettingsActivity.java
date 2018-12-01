package cmsc436.feelingsdiary;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private TimePicker mTimePicker;
    private Button mSaveNotificationTime;
    private EditText mPasswordText;
    private EditText mConfirmText;
    private Button mSavePasswordChange;
    private RadioGroup mFrequencyGroup;
    private Button mSaveNotificationFrequency;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();



    private boolean isPasswordValid(String password) {
        return (password.length() > 5) &&
                (password.matches(".*[0-9].*"));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final SharedPreferences prefs = getSharedPreferences("feelingsdiary", MODE_PRIVATE);

        //Allows the user to set the notification frequency

        // Checks the button that is the current setting
        mFrequencyGroup = findViewById(R.id.frequency_group);
        mFrequencyGroup.check(mFrequencyGroup.getChildAt(prefs.getInt("notificationsetting", 0)).getId());

        mSaveNotificationFrequency = findViewById(R.id.save_notification_frequency_btn);
        mSaveNotificationFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton selected = findViewById(mFrequencyGroup.getCheckedRadioButtonId());
                prefs.edit().putInt("notificationsetting", mFrequencyGroup.indexOfChild(selected)).apply();
                Toast.makeText(SettingsActivity.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }
        });

        //Allows the user to set the notification time
        mTimePicker = findViewById(R.id.notification_time);
        mSaveNotificationTime= findViewById(R.id.save_notification_time_btn);
        mSaveNotificationTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long hour = mTimePicker.getHour() * 3600000;
                long minute = mTimePicker.getMinute() * 60000;

                prefs.edit().putLong("notificationtime", hour + minute).apply();

                Toast.makeText(SettingsActivity.this, getString(R.string.restart_app), Toast.LENGTH_LONG).show();
            }
        });

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
                Toast.makeText(SettingsActivity.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }
        });


    }
}