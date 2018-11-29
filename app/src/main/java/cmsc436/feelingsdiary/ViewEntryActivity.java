package cmsc436.feelingsdiary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

public class ViewEntryActivity extends AppCompatActivity {

    TextView mDateText;
    RatingBar mMoodRating;
    TextView mLocationText;
    TextView mThoughtsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entry);

        mDateText = findViewById(R.id.date);
        mMoodRating = findViewById(R.id.mood_scale);
        mLocationText = findViewById(R.id.recorded_location);
        mThoughtsText = findViewById(R.id.recorded_thoughts);

        Entry entry = (Entry) getIntent().getSerializableExtra("Entry");

        mDateText.setText(entry.getDate());
        mMoodRating.setRating(Float.parseFloat(entry.getRating()));
        mLocationText.setText("No implemented yet");
        mThoughtsText.setText(entry.getEntry());
    }
}
