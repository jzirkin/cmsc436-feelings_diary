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

        String thoughtsStr = "This is a really really long String. I am going to fill it up to see how the text wraps. " +
                "Hello my name is Joshua Zirkin and I am taking CMSC436. This is a really really long String. I am going to " +
                "fill it up to see how the text wraps. Hello my name is Joshua Zirkin and I am taking CMSC436.This is a really " +
                "really long String. I am going to fill it up to see how the text wraps. Hello my name is Joshua Zirkin and I am " +
                "taking CMSC436.This is a really really long String. I am going to fill it up to see how the text wraps. Hello my " +
                "name is Joshua Zirkin and I am taking CMSC436.This is a really really long String. I am going to fill it up to see " +
                "how the text wraps. Hello my name is Joshua Zirkin and I am taking CMSC436.This is a really really long String. " +
                "I am going to fill it up to see how the text wraps. Hello my name is Joshua Zirkin and I am taking CMSC436." +
                "This is a really really long String. I am going to fill it up to see how the text wraps. Hello my name is " +
                "Joshua Zirkin and I am taking CMSC436.This is a really really long String. I am going to fill it up to see " +
                "how the text wraps. Hello my name is Joshua Zirkin and I am taking CMSC436.This is a really really long String. " +
                "I am going to fill it up to see how the text wraps. Hello my name is Joshua Zirkin and I am taking CMSC436.";

        mDateText.setText("November 10, 2018");
        mMoodRating.setRating(4);
        mLocationText.setText("Google Maps Link");
        mThoughtsText.setText(thoughtsStr);
    }
}
