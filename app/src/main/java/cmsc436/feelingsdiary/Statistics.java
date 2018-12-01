package cmsc436.feelingsdiary;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
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


public class Statistics extends AppCompatActivity {
    private String mUserID;
    private DatabaseReference mDatabaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        /**
         * The code below generates a vertical bar chart with the average rating for all entries monthly in the current year.
         */

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mUserID = user.getUid();
        } else {
            finish();
        }
        mDatabaseRef = database.getReference(mUserID);

        // avg ratings of all entries from each month
        int jan=0, feb=0, mar=0, apr=0, may=0, jun=0, jul=0, aug=0, sep=0, oct=0, nov=0, dec=0;

        int year = Calendar.getInstance().get(Calendar.YEAR);

        for (int month = 1; month <= 12; month++) { // loop through each month
            ArrayList<Integer> temp = new ArrayList<>(); // all ratings in the month
            // loops through each day to get all ratings for all entries on all days in a month
            for (int day = 1; day <= 31; day++) {
                ArrayList<Integer> ratings = getAllRatings(mDatabaseRef, year, month, day); // ratings for all entries in a given day
                temp.addAll(ratings); // continuously merges all daily ratings with temp (monthly ratings)
            }
            int average = (int) calculateAverage(temp); // calculating average monthly rating
            if (month == 1) jan = average;
            if (month == 2) feb = average;
            if (month == 3) mar = average;
            if (month == 4) apr = average;
            if (month == 5) may = average;
            if (month == 6) jun = average;
            if (month == 7) jul = average;
            if (month == 8) aug = average;
            if (month == 9) sep = average;
            if (month == 10) oct = average;
            if (month == 11) nov = average;
            if (month == 12) dec = average;
        }

        // adding entries for average ratings per month
        HorizontalBarChart barChart = (HorizontalBarChart) findViewById(R.id.barchart);
        ArrayList<BarEntry> bargroup1 = new ArrayList<>();
        bargroup1.add(new BarEntry(jan, 0));
        bargroup1.add(new BarEntry(feb, 1));
        bargroup1.add(new BarEntry(mar, 2));
        bargroup1.add(new BarEntry(apr, 3));
        bargroup1.add(new BarEntry(may, 4));
        bargroup1.add(new BarEntry(jun, 5));
        bargroup1.add(new BarEntry(jul, 6));
        bargroup1.add(new BarEntry(aug, 7));
        bargroup1.add(new BarEntry(sep, 8));
        bargroup1.add(new BarEntry(oct, 9));
        bargroup1.add(new BarEntry(nov, 10));
        bargroup1.add(new BarEntry(3, 11)); // replace with dec later

        // creating dataset for Bar Group 1
        BarDataSet barDataSet1 = new BarDataSet(bargroup1, "Average Monthly Ratings");
        barDataSet1.setColors(ColorTemplate.COLORFUL_COLORS);

        // Creating the labels
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");
        labels.add("July");
        labels.add("August");
        labels.add("September");
        labels.add("October");
        labels.add("November");
        labels.add("December");

        // combine all dataset into an arraylist
        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet1);

        // initialize the Bardata with argument labels and dataSet
        BarData data = new BarData(labels, dataSets);
        barChart.setData(data);
    }

    /**
     * This function is supposed to return an ArrayList with all ratings from all entries on a given day.
     * @param databaseRef
     * @param year
     * @param month
     * @param dayOfMonth
     * @return
     */
    private ArrayList<Integer> getAllRatings(DatabaseReference databaseRef, int year, int month, int dayOfMonth) {
        DatabaseReference databaseDateRef = databaseRef.child(formatDate(year, month, dayOfMonth));
        final ArrayList<Entry> mSelectedDateEntryList = new ArrayList<>(); // all ratings for all entries on the day
        final ArrayList<Integer> mSelectedDateRatingsList = new ArrayList<>();

        /*
         * Retrieve the diary ratings from Entry objects at the selected date and add them to the
         * ratings list.
         */
        databaseDateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterates through each entry at the specified date
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    mSelectedDateEntryList.add(child.getValue(Entry.class)); // adds all ratings for the day
                }
                for (Entry entry : mSelectedDateEntryList) {
                    mSelectedDateRatingsList.add(Integer.parseInt(entry.getRating()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // ...
            }
        });

        return mSelectedDateRatingsList;
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


    // Calculates average (rating) in an arraylist
    private double calculateAverage(ArrayList<Integer> marks) {
        Integer sum = 0;
        if(!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return (sum.doubleValue() / marks.size());
        }
        return sum;
    }
}
