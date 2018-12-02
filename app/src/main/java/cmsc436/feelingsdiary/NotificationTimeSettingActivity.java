package cmsc436.feelingsdiary;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

public class NotificationTimeSettingActivity extends AppCompatActivity {

    private TimePicker mTimePicker;
    private Button mSaveNotificationTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_time_setting);

        final SharedPreferences prefs = getSharedPreferences("feelingsdiary", MODE_PRIVATE);

        //Allows the user to set the notification time
        mTimePicker = findViewById(R.id.notification_time);
        mSaveNotificationTime = findViewById(R.id.save_notification_time_btn);
        mSaveNotificationTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long hour = mTimePicker.getHour() * 3600000;
                long minute = mTimePicker.getMinute() * 60000;

                prefs.edit().putLong("notificationtime", hour + minute).apply();

                Toast.makeText(NotificationTimeSettingActivity.this, getString(R.string.restart_app), Toast.LENGTH_LONG).show();
            }
        });
    }

}
