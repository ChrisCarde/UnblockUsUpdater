package com.carde.unblockus_2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final String DEBUG_TAG = UnblockUsAPIClient.class.getCanonicalName();
    private String unblockUsEmail = null;
    private String unblockUsPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        unblockUsEmail = sharedPref.getString("account_email",null);
        unblockUsPassword = sharedPref.getString("account_password", null);
        sharedPref.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("account_email")) {
                    unblockUsEmail = sharedPref.getString("account_email", null);
                }
                if (key.equals("account_password")) {
                    unblockUsPassword = sharedPref.getString("account_password", null);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(DEBUG_TAG,"email=" + unblockUsEmail + " pw=" + unblockUsPassword);
                if (unblockUsPassword != null && unblockUsPassword.length() > 1 && unblockUsEmail != null && unblockUsEmail.length() > 1) {
                    doUpdate(view);
                } else {
                    handleUpdateWithoutCredentials(view);
                }
            }
        });

    }

    private void handleUpdateWithoutCredentials(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void doUpdate(View view) {
        setLoading(true);
        Snackbar.make(view, "Updating Unblock-Us", Snackbar.LENGTH_LONG)
                .show();
        UnblockUsAPIClient client = new UnblockUsAPIClient(unblockUsEmail,unblockUsPassword);
        UnblockUsAPIClient.UpdateResultListener listener = new UnblockUsAPIClient.UpdateResultListener() {
            private View view;
            public void setView(View view) {
                this.view = view;
            }
            public void handleUpdateResults(int state, String errorMessage) {
                Log.d("MainActivity","in handleUpdateResults(" + state + ",\"" + errorMessage + "\")");
                TextView tv = (TextView) findViewById(R.id.statusTextTextView);
                ImageView iv = (ImageView) findViewById(R.id.statusIconImageView);

                setLoading(false);
                if (errorMessage != null) {
                    Snackbar.make(view, errorMessage, Snackbar.LENGTH_INDEFINITE).show();
                    tv.setText(R.string.statusError);
                    iv.setImageResource(R.drawable.frown);
                } else {
                    switch (state) {
                        case UnblockUsAPIClient.ACCOUNT_STATE_UNINITIALIZED:
                            tv.setText(R.string.statusUnknown);
                            iv.setImageResource(R.drawable.question2);
                            break;
                        case UnblockUsAPIClient.ACCOUNT_STATE_BAD_PASSWORD:
                            tv.setText(R.string.statusBadPassword);
                            iv.setImageResource(R.drawable.frown);
                            break;
                        case UnblockUsAPIClient.ACCOUNT_STATE_TRIAL:
                            tv.setText(R.string.statusInTrial);
                            iv.setImageResource(R.drawable.smiley2);
                            break;
                        case UnblockUsAPIClient.ACCOUNT_STATE_ACTIVE:
                            tv.setText(R.string.statusActive);
                            iv.setImageResource(R.drawable.smiley2);
                            break;
                        case UnblockUsAPIClient.ACCOUNT_STATE_BAD_EMAIL:
                            tv.setText(R.string.statusBadEmail);
                            iv.setImageResource(R.drawable.frown);
                            break;
                    }
                }

            }
        };
        listener.setView(view);
        client.update(listener);
    }

    private void setLoading(boolean isLoading) {
        ImageView i = (ImageView) findViewById(R.id.statusIconImageView);
        ProgressBar p = (ProgressBar) findViewById(R.id.progressBar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        TextView statusText = (TextView) findViewById(R.id.statusTextTextView);

        i.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        p.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        fab.setEnabled(!isLoading);
        statusText.setText(isLoading ? R.string.statusInProgress : R.string.blank);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
