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
        mSearchLayout = findViewById(R.id.search_layout);
        mSearchButton = findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (NullPointerException e) {
                    // do nothing
                }

                if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
                    showProgress(true);

                    String keyword = mSearchInput.getText().toString();

                    if (keyword.equals("")) {
                        setNoResultsFound();
                        showProgress(false);
                    } else {
                        task = new SearchTask(keyword);
                        task.execute((Void) null);
                    }
                }
            }
        });

        mNoResults = new TextView(this);
        mNoResults.setText(R.string.no_search_results);

        mAdapter = new SearchListAdapter(this);
        mResultsList = new ListView(this);
        mResultsList.setFooterDividersEnabled(true);
        mResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchActivity.this, ViewEntryActivity.class);
                intent.putExtra("entry", (Entry) mAdapter.getItem(position));
                startActivity(intent);
            }
        });
        mResultsList.setAdapter(mAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
        } else {
            uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child(uID);
    }

    private void setNoResultsFound() {
        mSearchLayout.removeAllViews();
        mAdapter.clearList();
        mSearchLayout.addView(mNoResults);
    }

    private void setResultsFound(List<Entry> entries) {
        mSearchLayout.removeAllViews();
        mAdapter.clearList();
        mAdapter.addList(entries);
        mSearchLayout.addView(mResultsList);
    }

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

    /**
     * Represents an asynchronous login task used to authenticate the user.
     */
    public class SearchTask extends AsyncTask<Void, Void, Boolean> {

        String keyword;
        private final List<Entry> list = new ArrayList<>();

        public SearchTask(String keyword) {
            this.keyword = keyword;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final CountDownLatch latch = new CountDownLatch(1);

            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String message = (String) data.child("entry").getValue();
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
