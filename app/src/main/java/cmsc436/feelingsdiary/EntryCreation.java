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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class EntryCreation extends AppCompatActivity implements Serializable {

    private TextView mTextMessage;
    private SeekBar mSeekBar;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_creation);

        mTextMessage = findViewById(R.id.entry);

        /**
         * Gets the date and sets the hint for the TextView as the current date
         */
        Button mSubmitButton = findViewById(R.id.button);
        mSeekBar = findViewById(R.id.seekBar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        } else {
            finish();
        }

        // Stores diary entry, rating, time, and location data into firebase.
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Date currentTime = Calendar.getInstance().getTime();

                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy", Locale.US);
                String fbDate = sdf.format(currentTime);

                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                databaseRef = databaseRef.child(userID).child(fbDate).push();

                sdf = new SimpleDateFormat("EEE, MMM dd, yyyy, h:mm a", Locale.US);
                String entryDate = sdf.format(currentTime);
                Entry entry = new Entry(
                        entryDate,
                        "" + (mSeekBar.getProgress() + 1),
                        mTextMessage.getText().toString());
                databaseRef.setValue(entry);

                getSharedPreferences("feelingsdiary", MODE_PRIVATE).edit()
                        .putInt("lastentry", 0).apply();
                finish();
            }
        });
    }
}
