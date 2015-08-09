package com.bibibig.yeon.wwmbibibig;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bibibig.yeon.wwmbibibig.calendarlist.CalendarListActivity;
import com.bibibig.yeon.wwmbibibig.common.BasicInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainActivity extends Activity {
    static final String TAG = "MainActivity";
    SharedPreferences session;
    SharedPreferences.Editor editor;

    private static final Level LOGGING_LEVEL = Level.OFF;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    TextView mStatusText;
    TextView useraccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusText = (TextView) findViewById(R.id.statusTxt);
        useraccount = (TextView) findViewById(R.id.Useraccount);
        Button calendarlist = (Button) findViewById(R.id.calendarlist);
        Button calendarevent = (Button) findViewById(R.id.calendarevent);
        Button tasklist = (Button) findViewById(R.id.tasklist);

        calendarlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),CalendarListActivity.class );
                startActivity(i);

            }
        });

//        calendarevent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(getApplicationContext(), );
//                startActivity(i);
//
//            }
//        });

//        tasklist.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(getApplicationContext(), );
//                startActivity(i);
//
//            }
//        });

        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
        session= getSharedPreferences(BasicInfo.MyPREFERENCES, Context.MODE_PRIVATE);
        editor = session.edit();
        BasicInfo.credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(BasicInfo.SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(session.getString(BasicInfo.PREF_ACCOUNT_NAME, null));

        TextView cre = (TextView) findViewById(R.id.credential);
        cre.setText(BasicInfo.credential.toString());

        editor.putString(BasicInfo.PREF_CREDENTIAL, BasicInfo.credential.toString());
        editor.apply();


        BasicInfo.Calendarclient = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, BasicInfo.credential)
                .setApplicationName("Google-CalendarBibibig/1.0")
                .build();

        TextView Cclient = (TextView) findViewById(R.id.calendarclient);
        Cclient.setText(BasicInfo.Calendarclient.toString());
        useraccount.setText(BasicInfo.accountnameStr);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            haveGooglePlayServices();
        }else{
            mStatusText.setText("Google Play Services required: " + "after installing, close and relaunch this app.");
        }
    }

    private void haveGooglePlayServices() {
        // check if there is already an account selected
        if (BasicInfo.credential.getSelectedAccountName() == null) {
            // ask user to choose account
            chooseAccount();
        }
    }

    private void chooseAccount() {
        startActivityForResult(BasicInfo.credential.newChooseAccountIntent(), BasicInfo.REQUEST_ACCOUNT_PICKER);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BasicInfo.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case BasicInfo.REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        BasicInfo.credential.setSelectedAccountName(accountName);
                        BasicInfo.accountnameStr = accountName;
                        editor.putString(BasicInfo.PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        useraccount.setText(BasicInfo.accountnameStr);

                    }
                }else if(resultCode == RESULT_CANCELED){
                    mStatusText.setText("Account unspecified");
                }
                break;
            case BasicInfo.REQUEST_AUTHORIZATION:
                if (resultCode != Activity.RESULT_OK) {
                    chooseAccount();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }else if(connectionStatusCode != ConnectionResult.SUCCESS){
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, MainActivity.this, BasicInfo.REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
