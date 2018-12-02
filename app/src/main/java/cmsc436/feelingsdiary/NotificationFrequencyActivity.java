package cmsc436.feelingsdiary;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class NotificationFrequencyActivity extends AppCompatActivity {

    private RadioGroup mFrequencyGroup;
    private Button mSaveNotificationFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_frequency);

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
                Toast.makeText(NotificationFrequencyActivity.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
