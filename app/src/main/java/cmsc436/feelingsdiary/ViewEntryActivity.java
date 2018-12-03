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
import android.location.Geocoder;
import android.location.Location;
import android.location.Address;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewEntryActivity extends AppCompatActivity {

    TextView mDateText;
    RatingBar mMoodRating;
    TextView mLocationText;
    TextView mThoughtsText;
    TextView mTags;

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
        mTags = findViewById(R.id.recorded_tags);
        mEntry = (Entry) getIntent().getSerializableExtra("entry");

        mDateText.setText(mEntry.getDate());
        mMoodRating.setRating(Float.parseFloat(mEntry.getRating()));

        //Gets the location and builds a string out of it
        String locationString = "";
        Location loc = mEntry.getLocation();
        if(loc == null)
            locationString = getString(R.string.invalid_location);
        else{
            double latitude = loc.getLatitude();
            double longitude = loc.getLongitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try{
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String cityName = addresses.get(0).getAddressLine(0);
                String stateName = addresses.get(0).getAddressLine(1);
                String countryName = addresses.get(0).getAddressLine(2);
                locationString = cityName + ", " + stateName + " " + countryName;
            }
            catch (IOException e){
                locationString = getString(R.string.invalid_location);
            }
        }

        mLocationText.setText(locationString);
        List<String> tags = mEntry.getTags();
        StringBuilder tag;
        if (tags == null) {
            tag = new StringBuilder("None");
        } else {
            tag = new StringBuilder(tags.get(0).trim());
            for (int i = 1; i < tags.size(); i++) {
                tag.append(", ").append(tags.get(i).trim());
            }
        }
        mTags.setText(tag);
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
