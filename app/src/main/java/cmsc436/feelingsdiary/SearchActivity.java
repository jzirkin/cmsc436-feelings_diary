package cmsc436.feelingsdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/* Activity that allows users to search their entries for word(s). Either shows no entries (if
   none are found), or a list of all entries that contain the word(s).
 */
public class SearchActivity extends AppCompatActivity {

    private EditText mSearchInput;
    private Button mSearchButton;
    private LinearLayout mSearchLayout;
    private View mProgressView;

    private TextView mNoResults;
    private ListView mResultsList;
    private SearchListAdapter mAdapter;

    private SearchTask task;

    private DatabaseReference mDatabase;
    private String uID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mProgressView = findViewById(R.id.login_progress);
        mSearchInput = findViewById(R.id.search_editText);

        // Layout will be populated by either a TextView for no results found or a list of results
        mSearchLayout = findViewById(R.id.search_layout);

        mSearchButton = findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hides the keyboard on button click
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (NullPointerException e) {
                    // do nothing
                }

                // We only want to do something if this is the first time the button was pressed
                // or the previous AsyncTask is done. Otherwise weird stuff can happen.
                if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
                    // Hide the UI
                    showProgress(true);

                    String keyword = mSearchInput.getText().toString();

                    // Do nothing if no text was entered
                    if (keyword.equals("")) {
                        setNoResultsFound();
                        showProgress(false);
                    // Otherwise start the search
                    } else {
                        task = new SearchTask(keyword);
                        task.execute((Void) null);
                    }
                }
            }
        });

        // Simple TextView if no results are found
        mNoResults = new TextView(this);
        mNoResults.setText(R.string.no_search_results);

        // List that shows all results found (must be > 0)
        mAdapter = new SearchListAdapter(this);
        mResultsList = new ListView(this);
        mResultsList.setFooterDividersEnabled(true);
        // Set onClickListener to open the entry when clicked in the list
        mResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchActivity.this, ViewEntryActivity.class);
                intent.putExtra("Entry", (Entry) mAdapter.getItem(position));
                startActivity(intent);
            }
        });
        mResultsList.setAdapter(mAdapter);

        // If no one is logged in, get out
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
        } else {
            uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child(uID);
    }

    // Removes Views from the layout, clears results in the adapter, and puts mNoResults in
    private void setNoResultsFound() {
        mSearchLayout.removeAllViews();
        mAdapter.clearList();
        mSearchLayout.addView(mNoResults);
    }

    // Removes Views from the layout, clears results, adds new ones, and puts mResultsList in
    private void setResultsFound(List<Entry> entries) {
        mSearchLayout.removeAllViews();
        mAdapter.clearList();
        mAdapter.addList(entries);
        mSearchLayout.addView(mResultsList);
    }

    // Same as in LoginActivity but smaller and only hides mSearchLayout, not everything
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSearchLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            mSearchLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSearchLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mSearchLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /* AsyncTask that executes a search given keyword(s). It is not case sensitive */
    public class SearchTask extends AsyncTask<Void, Void, Boolean> {

        String keyword;
        private final List<Entry> list = new ArrayList<>();

        public SearchTask(String keyword) {
            this.keyword = keyword;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // CountDownLatch so we wait for the search do be done before finishing
            final CountDownLatch latch = new CountDownLatch(1);

            // Gets quick snapshot of database
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // For all dates with entries
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // For every entry in that date
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String message = (String) data.child("entry").getValue();
                            // If the message contains the keyword(s), add the Entry to the list
                            if (message != null && message.toLowerCase().contains(keyword.toLowerCase())) {
                                String datetime = (String) data.child("date").getValue();
                                String mood = (String) data.child("rating").getValue();
                                list.add(new Entry(datetime, mood, message));
                            }
                        }
                    }
                    latch.countDown();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    latch.countDown();
                }
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                // do nothing
            }
            return true;
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            // Set the mSearchLayout depending on if results were found or not
            if (list.size() == 0) {
                setNoResultsFound();
            } else {
                setResultsFound(list);
            }
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }
}
