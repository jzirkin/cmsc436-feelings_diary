package cmsc436.feelingsdiary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/* Activity for creating an Entry. Entries are stored in Firebase under the user and
* date they are created. */
public class EntryCreation extends AppCompatActivity implements Serializable {

    private TextView mTextMessage;
    private SeekBar mSeekBar;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_creation);

        mTextMessage = findViewById(R.id.entry);
        Button mSubmitButton = findViewById(R.id.button);
        mSeekBar = findViewById(R.id.seekBar);

        // If there is not a user currently logged in, get out
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        } else {
            setResult(RESULT_CANCELED);
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

                // We don't check the contents of the entry because it's optional
                Entry entry = new Entry(
                        entryDate,
                        "" + (mSeekBar.getProgress() + 1),
                        mTextMessage.getText().toString());
                databaseRef.setValue(entry);

                // For notifications - reset the counter for entries
                getSharedPreferences("feelingsdiary", MODE_PRIVATE).edit()
                        .putInt("lastentry", 0).apply();
                Intent intent = new Intent();
                intent.putExtra("entry", entry);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
