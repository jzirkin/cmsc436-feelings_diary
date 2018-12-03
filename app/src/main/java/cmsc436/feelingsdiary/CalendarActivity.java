package cmsc436.feelingsdiary;

import android.content.Intent;
import android.graphics.Color;
import android.content.Context;
import android.location.LocationProvider;
import android.view.ViewGroup;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    // Adapter for entry list modified so that the background color changes according to the mood rating.
    private class MyAdapter extends ArrayAdapter {
        public MyAdapter(Context context, int resource, ArrayList objects) {
            super(context, resource, objects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            int rating = Integer.parseInt(mSelectedDateEntryList.get(position).getRating());
            if (rating >= 4)
                view.setBackgroundColor(Color.parseColor("#849974"));
            else if (rating > 2)
                view.setBackgroundColor(Color.parseColor("#E9DCCD"));
            else
                view.setBackgroundColor(Color.parseColor("#36384C"));

            return view;
        }
    }

    private String mUserID;
    private ListView mEntryListView;
    private ArrayList<Entry> mSelectedDateEntryList;
    private final String TAG = "CALENDAR_ACTIVITY";

    private final int RESULT_DELETED_ENTRY = 2;

    private DatabaseReference mDatabaseRef;

    private int mCurrSelectedYear;
    private int mCurrSelectedMonth;
    private int mCurrSelectedDayOfMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mEntryListView = findViewById(R.id.entry_list_view);

        CalendarView calendar = findViewById(R.id.calendar_view);
        calendar.setMaxDate(System.currentTimeMillis());

        mSelectedDateEntryList = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mUserID = user.getUid();
        } else {
            finish();
        }

        mDatabaseRef = database.getReference(mUserID);

        /*
         * The calendar will open with the current date selected. This will load the entries into
         * the list.
         */
        updateEntryList(mDatabaseRef,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        mCurrSelectedYear = Calendar.getInstance().get(Calendar.YEAR);
        mCurrSelectedMonth = Calendar.getInstance().get(Calendar.MONTH);
        mCurrSelectedDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        /*
         * When an entry item is selected. A ViewEntryActivity will be started.
         */
        mEntryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "In onItemSelected");
                Intent viewEntryIntent = new Intent(CalendarActivity.this, ViewEntryActivity.class);

                viewEntryIntent.putExtra("entry", mSelectedDateEntryList.get(position));

                startActivityForResult(viewEntryIntent ,2);
            }
        });

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                clearEntryList();

                mCurrSelectedYear = year;
                mCurrSelectedMonth = month;
                mCurrSelectedDayOfMonth = dayOfMonth;

                updateEntryList(mDatabaseRef, year, month, dayOfMonth);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the previously viewed Entry was deleted, the list of entries has to be refreshed
        if (resultCode == RESULT_DELETED_ENTRY) {
            clearEntryList();
            updateEntryList(mDatabaseRef, mCurrSelectedYear,
                    mCurrSelectedMonth, mCurrSelectedDayOfMonth);
        }
    }

    /**
     * When a date is selected on the calendar. The list below it will be updated to display the
     * time of all the recorded entries on that date. If there are no entries, a toast message will
     * display "No recorded entries".
     *
     * @param databaseRef Reference all recorded entries for the current user
     * @param year        Year in the form yyyy
     * @param month       Month in range 0-11.
     * @param dayOfMonth  Day of the month
     */
    private void updateEntryList(DatabaseReference databaseRef, int year, int month, int dayOfMonth) {
        DatabaseReference databaseDateRef = databaseRef.child(formatDate(year, month, dayOfMonth));

        Log.i(TAG, "in onSelectedDayChange");

        final ArrayList<String> entryTimeList = new ArrayList<>();

        /*
         * Retrieve the diary Entry objects at the selected date and add them to the
         * entries list.
         */
        databaseDateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterates through each entry at the specified date
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    mSelectedDateEntryList.add(child.getValue(Entry.class));
                    Log.i(TAG, mSelectedDateEntryList.get(0).getDate());
                }

                if (mSelectedDateEntryList.size() == 0) {
                    Toast.makeText(getApplicationContext(), "No recorded entries", Toast.LENGTH_SHORT).show();
                } else {
                    for (Entry entry : mSelectedDateEntryList) {
                        String message = "    ";
                        if (entry.getEntry().length() > 36) {
                            message += entry.getEntry().substring(0, 36) + "...";
                        } else {
                            message += entry.getEntry();
                        }
                        entryTimeList.add(getTime(entry.getDate()) + message);

                    }

                    ArrayAdapter<String> adapter = new MyAdapter(CalendarActivity.this,
                            android.R.layout.simple_list_item_1, entryTimeList);

                    for (String time : entryTimeList) {
                        Log.i(TAG, time);
                    }

                    mEntryListView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // ...
            }
        });
    }

    /**
     * Creates a properly formatted date String from the given date information.
     *
     * @param year       Year in the form yyyy
     * @param month      Month in range 0-11.
     * @param dayOfMonth Day of the month
     * @return The specified date in the form MM-dd-yy
     */
    private String formatDate(int year, int month, int dayOfMonth) {
        String dateStr = (month + 1) + "-" + dayOfMonth + "-" + year;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy", Locale.US);
        Date date = null;

        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return sdf.format(date);
    }

    /**
     * Parses the time from a date string
     *
     * @param dateStr A date String in the form EEE, MMM dd, yyyy, h:mm a
     * @return An String of the form h:mm with AM/PM representing the time
     */
    private String getTime(String dateStr) {
        Date date = null;
        try {
            date = new SimpleDateFormat("EEE, MMM dd, yyyy, h:mm a", Locale.US).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new SimpleDateFormat("h:mm a", Locale.US).format(date);
    }

    private void clearEntryList() {
        mSelectedDateEntryList.clear();

        ArrayAdapter<String> adapter = new MyAdapter(CalendarActivity.this,
                android.R.layout.simple_list_item_1, new ArrayList<String>());

        mEntryListView.setAdapter(adapter);
    }
}
