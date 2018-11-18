package cmsc436.feelingsdiary;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import android.widget.Button;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Calendar;
import java.util.Date;


public class EntryCreation extends AppCompatActivity implements Serializable {

    private TextView mTextMessage;
    private SeekBar mSeekBar;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_creation);

        mTextMessage = (TextView) findViewById(R.id.message);

        /**
         * Gets the date and sets the hint for the TextView as the current date
         */
        long date = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM MM dd, yyyy h:mm a");
        String dateString = sdf.format(date);

        Button mSubmitButton = findViewById(R.id.submitButton);
        mSeekBar = findViewById(R.id.seekBar2);

        final Date currentTime = Calendar.getInstance().getTime();

        /**
         * Stores diary entry, rating, time, and location data into firebase.
         */
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                databaseRef.child("users").child("diaryEntry").setValue(mTextMessage);
                databaseRef.child("users").child("ratingEntry").setValue(mSeekBar.getProgress());
                databaseRef.child("users").child("date").setValue(currentTime);
            }
        });

        mTextMessage.setHint(dateString);
    }

}
