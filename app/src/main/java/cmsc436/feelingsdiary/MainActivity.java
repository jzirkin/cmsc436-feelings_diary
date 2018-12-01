package cmsc436.feelingsdiary;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mAddEntryButton = findViewById(R.id.add_entry_button);
        mAddEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start for result to open up created entry if successful
                startActivityForResult(new Intent(MainActivity.this, EntryCreation.class), 0);
            }
        });

        Button mStatsButton = findViewById(R.id.stats_button);
        mStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Statistics.class)); //TODO: Add class for stats activity
            }
        });

        Button mCalendarButton = findViewById(R.id.calendar_button);
        mCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CalendarActivity.class));
            }
        });

        Button mRecentEntryButton = findViewById(R.id.search_button);
        mRecentEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });

        findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sign out
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // If no one is logged in, get out
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If EntryCreation exited successfully, open up the entry that was just created
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, ViewEntryActivity.class);
            intent.putExtra("entry", data.getSerializableExtra("entry"));
            startActivity(intent);
        }
    }

    //Creates a menu in the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(super.onCreateOptionsMenu(menu)){
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.settings_menu:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.logout_menu:
                FirebaseAuth.getInstance().signOut();
                // remove the user id from sharedpreferences
                getSharedPreferences("feelingsdiary", MODE_PRIVATE).edit().putString("uid", null).apply();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
