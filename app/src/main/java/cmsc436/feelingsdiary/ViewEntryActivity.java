package cmsc436.feelingsdiary;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewEntryActivity extends AppCompatActivity {

    TextView mDateText;
    RatingBar mMoodRating;
    TextView mLocationText;
    TextView mThoughtsText;

    Entry mEntry;

    private final int RESULT_DELETED_ENTRY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entry);

        String userID = null;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        } else {
            finish();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseRef = database.getReference(userID);

        mDateText = findViewById(R.id.date);
        mMoodRating = findViewById(R.id.mood_scale);
        mLocationText = findViewById(R.id.recorded_location);
        mThoughtsText = findViewById(R.id.recorded_thoughts);

        mEntry = (Entry) getIntent().getSerializableExtra("entry");

        mDateText.setText(mEntry.getDate());
        mMoodRating.setRating(Float.parseFloat(mEntry.getRating()));
        mLocationText.setText("No implemented yet");
        mThoughtsText.setText(mEntry.getEntry());

        final DatabaseReference databaseDateRef = databaseRef.child(getDate(mEntry.getDate()));

        Button deleteEntryButton = findViewById(R.id.delete_entry_button);

        // Asks the User if they're sure they want to delete the entry
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ViewEntryActivity.this);
        alertBuilder.setTitle(R.string.delete_prompt);

        alertBuilder.setPositiveButton(R.string.yes_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteEntry(databaseDateRef, mEntry);

                // Tells the CalendarActivity/SearchActivity to update the Entry list for this date
                Intent intent = new Intent();
                intent.putExtra("keyword", getIntent().getStringExtra("keyword"));
                setResult(RESULT_DELETED_ENTRY, intent);
                finish();
            }
        });

        alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        deleteEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertBuilder.show();
            }
        });
    }

    /**
     * Deleted the diary entry from the database
     *
     * @param databaseDateRef Reference all recorded entries for the current user on the date of the
     *                        currently viewed Entry
     * @param entry The Entry being viewed
     */
    private void deleteEntry(DatabaseReference databaseDateRef, final Entry entry) {
        databaseDateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through all entries created on the same date as the current entry
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    // Checks if the current child entry is the same as the one to be deleted
                    if (child.getValue(Entry.class).getDate().equals(entry.getDate()) &&
                            child.getValue(Entry.class).getEntry().equals(entry.getEntry())) {
                        child.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // ...
            }
        });
    }

    private String getDate(String dateStr) {
        Date date = null;
        try {
            date = new SimpleDateFormat("EEE, MMM dd, yyyy, h:mm a", Locale.US).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new SimpleDateFormat("MM-dd-yy", Locale.US).format(date);
    }
}
