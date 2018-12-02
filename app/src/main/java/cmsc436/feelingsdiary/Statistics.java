package cmsc436.feelingsdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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

import net.alhazmy13.wordcloud.WordCloud;
import net.alhazmy13.wordcloud.WordCloudView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class Statistics extends AppCompatActivity {

    private final static int PURPLE = 0x800080;
    private final static int OLIVE = 0x808000;
    private final static int MAROON = 0x741A31;
    private final static int DARKBLUE = 0x1E1844;
    private final static int TAN = 0xD2C378;
    private final static int DARKGREEN = 0x003D00;
    private final static int LIGHTGRAY = 0x9EA19D;


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

        showProgress(true);
        StatsTask task = new StatsTask();
        task.execute();
    }

    /* Hides the UI and shows a spinning-loading bar while the AsyncTask is running */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        final View barchartView = findViewById(R.id.barchart);
        final View progressView = findViewById(R.id.login_progress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            barchartView.setVisibility(show ? View.GONE : View.VISIBLE);
            barchartView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    barchartView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            barchartView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private class StatsTask extends AsyncTask<Void, Void, Void> {

        double[] averages = new double[12];
        HashMap<String, Integer> wordMap = new HashMap<>();

        @Override
        protected Void doInBackground(Void... voids) {
            // avg ratings of all entries from each month
            SparseArray<List<Integer>> map = new SparseArray<>();
            map.put(1, new ArrayList<Integer>());
            map.put(2, new ArrayList<Integer>());
            map.put(3, new ArrayList<Integer>());
            map.put(4, new ArrayList<Integer>());
            map.put(5, new ArrayList<Integer>());
            map.put(6, new ArrayList<Integer>());
            map.put(7, new ArrayList<Integer>());
            map.put(8, new ArrayList<Integer>());
            map.put(9, new ArrayList<Integer>());
            map.put(10, new ArrayList<Integer>());
            map.put(11, new ArrayList<Integer>());
            map.put(12, new ArrayList<Integer>());

            CountDownLatch latch = new CountDownLatch(1);

            // loops through each day to get all ratings for all entries on all days in a month
            getAllRatingsAndWords(mDatabaseRef, map, latch); // ratings for all entries in a given month

            try {
                latch.await();
            } catch (InterruptedException e) {
                // do nothing
            }

            for (int month = 1; month <= 12; month++) { // loop through each month
                averages[month - 1] = calculateAverage(map.get(month));
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void nothing) {
            // adding entries for average ratings per month
            BarChart barChart = findViewById(R.id.barchart);
            ArrayList<BarEntry> barList = new ArrayList<>();
            barList.add(new BarEntry((int) averages[0], 0));
            barList.add(new BarEntry((int) averages[1], 1));
            barList.add(new BarEntry((int) averages[2], 2));
            barList.add(new BarEntry((int) averages[3], 3));
            barList.add(new BarEntry((int) averages[4], 4));
            barList.add(new BarEntry((int) averages[5], 5));
            barList.add(new BarEntry((int) averages[6], 6));
            barList.add(new BarEntry((int) averages[7], 7));
            barList.add(new BarEntry((int) averages[8], 8));
            barList.add(new BarEntry((int) averages[9], 9));
            barList.add(new BarEntry((int) averages[10], 10));
            barList.add(new BarEntry((int) averages[11], 11));

            // creating dataset for Bar Group 1
            BarDataSet barDataSet = new BarDataSet(barList, "Average Monthly Ratings");
            barDataSet.setColors(ColorTemplate.PASTEL_COLORS);

            // Creating the labels
            ArrayList<String> labels = new ArrayList<>();
            labels.add("Jan");
            labels.add("Feb");
            labels.add("Mar");
            labels.add("Apr");
            labels.add("May");
            labels.add("Jun");
            labels.add("Jul");
            labels.add("Aug");
            labels.add("Sep");
            labels.add("Oct");
            labels.add("Nov");
            labels.add("Dec");

            // combine all dataset into an arraylist
            ArrayList<BarDataSet> dataSets = new ArrayList<>();
            dataSets.add(barDataSet);

            // initialize the Bardata with argument labels and dataSet
            BarData data = new BarData(labels, dataSets);
            barChart.setData(data);
            barChart.setDrawValueAboveBar(false);
            barChart.setClickable(false);
            barChart.setDescription("");

            // modify axes
            XAxis x = barChart.getXAxis();
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setLabelsToSkip(0);

            WordCloudView wordCloud = findViewById(R.id.wordCloud);
            List<WordCloud> list = new ArrayList<>();
            for (String key : wordMap.keySet()) {
                list.add(new WordCloud(key, wordMap.get(key)));
            }
            wordCloud.setDataSet(list);
            wordCloud.setColors(ColorTemplate.PASTEL_COLORS);
            wordCloud.setSize(500, 400);
            wordCloud.notifyDataSetChanged();

            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }

        /**
         * Gets all the ratings for all the months and puts them in the map in the respective months.
         * Also gets all the frequencies of all the words for the word cloud.
         * @param databaseRef
         * @param ratingMap
         * @return
         */
        private void getAllRatingsAndWords(DatabaseReference databaseRef, final SparseArray<List<Integer>> ratingMap, final CountDownLatch latch) {
            /*
             * Retrieve the diary ratings from Entry objects at the selected date and add them to the
             * ratings list.
             */
            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot date : dataSnapshot.getChildren()) {
                        String temp = date.getKey();
                        if (temp != null) {
                            // Date is always formatted the same so the month must be there
                            int month = Integer.parseInt(date.getKey().substring(0, 2));
                            for (DataSnapshot child : date.getChildren()) {
                                Entry entry = child.getValue(Entry.class);
                                if (entry != null) {
                                    // adds rating for the day
                                    ratingMap.get(month).add(Integer.parseInt(entry.getRating()));

                                    // gets all words in the entry
                                    String[] words = entry.getEntry().split(" ");
                                    for (String word : words) {
                                        if (!TextUtils.isEmpty(word)) {
                                            word = word.trim().toLowerCase();
                                            if (wordMap.containsKey(word)) {
                                                wordMap.put(word, wordMap.get(word) + 1);
                                            } else {
                                                wordMap.put(word, 1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    latch.countDown();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // ...
                }
            });
        }

        // Calculates average (rating) in a List
        private double calculateAverage(List<Integer> marks) {
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
}
