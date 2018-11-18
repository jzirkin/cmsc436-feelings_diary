package cmsc436.feelingsdiary;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button mAddEntryButton = findViewById(R.id.add_entry_button);
        mAddEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddEntryActivity.class)); //TODO: Add class for add entry activity
            }
        });

        Button mStatsButton = findViewById(R.id.stats_button);
        mStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StatsActivity.class)); //TODO: Add class for stats activity
            }
        });

        Button mCalendarButton = findViewById(R.id.calendar_button);
        mCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CalendarActivity.class)); //TODO: Add class for calendar activity
            }
        });

        Button mRecentEntryButton = findViewById(R.id.recent_entries_button);
        mRecentEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RecentEntriesActivity.class)); //TODO: Add class for recent entry activity
            }
        });
    }

    public void openSettingsMenu(View v){

    }
}
