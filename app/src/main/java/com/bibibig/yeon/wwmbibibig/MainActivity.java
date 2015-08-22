package com.bibibig.yeon.wwmbibibig;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    static final String TAG = "MainActivity";
    /*NAVI*/
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    /*Setting*/
    SharedPreferences session;
    SharedPreferences.Editor editor;
    private static final Level LOGGING_LEVEL = Level.OFF;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*NAVI*/
        mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        /*Setting*/
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);

        session= getSharedPreferences(BasicInfo.MyPREFERENCES, Context.MODE_PRIVATE);
        editor = session.edit();

        BasicInfo.credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(BasicInfo.SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(session.getString(BasicInfo.PREF_ACCOUNT_NAME, null));

        editor.putString(BasicInfo.PREF_CREDENTIAL, BasicInfo.credential.toString());
        editor.apply();


        BasicInfo.Calendarclient = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, BasicInfo.credential)
                .setApplicationName("Google-CalendarBibibig/1.0")
                .build();

    }
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        public static TextView mStatusText;
        public static TextView useraccount;
        public static Button calendarlist;
        public static Button calendarevent;
        public static Button tasklist;

        public static TextView credentialText;
        public static TextView clientText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mStatusText = (TextView)rootView.findViewById(R.id.statusTxt);
        useraccount = (TextView)rootView.findViewById(R.id.Useraccount);
        useraccount.setText(BasicInfo.accountnameStr);

        calendarlist = (Button)rootView.findViewById(R.id.calendarlist);
        calendarevent = (Button)rootView.findViewById(R.id.calendarevent);
        tasklist = (Button)rootView.findViewById(R.id.tasklist);

        credentialText = (TextView)rootView.findViewById(R.id.credential);
        credentialText.setText(BasicInfo.credential.toString());

        clientText = (TextView)rootView.findViewById(R.id.calendarclient);
        clientText.setText(BasicInfo.Calendarclient.toString());


        calendarlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(),CalendarListActivity.class );
                startActivity(i);

            }
        });

        calendarevent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "show me?", Toast.LENGTH_LONG).show();
                Log.d(TAG, "calendar evnet log debug test");
            }
        });

//        tasklist.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(getApplicationContext(), );
//                startActivity(i);
//
//            }
//        });

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            haveGooglePlayServices();
        }else{
            PlaceholderFragment.mStatusText.setText("Google Play Services required: " + "after installing, close and relaunch this app.");
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
                        PlaceholderFragment.useraccount.setText(BasicInfo.accountnameStr);

                    }
                }else if(resultCode == RESULT_CANCELED){
                   PlaceholderFragment.mStatusText.setText("Account unspecified");
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
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }


}
