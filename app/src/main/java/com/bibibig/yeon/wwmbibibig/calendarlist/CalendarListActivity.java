package com.bibibig.yeon.wwmbibibig.calendarlist;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bibibig.yeon.wwmbibibig.MainActivity;
import com.bibibig.yeon.wwmbibibig.R;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.model.Calendar;

import java.util.ArrayList;
import java.util.List;


public class CalendarListActivity extends Activity {
    static final String TAG = "CalendarListActivity";

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;
    static final int REQUEST_ACCOUNT_PICKER = 2;
    static final int ADD_OR_EDIT_CALENDAR_REQUEST = 3;

    private static final int CONTEXT_EDIT = 0;
    private static final int CONTEXT_DELETE = 1;
    private static final int CONTEXT_BATCH_ADD = 2;

    private static final String PREF_ACCOUNT_NAME = "accountName";

    GoogleAccountCredential credential;
    com.google.api.services.calendar.Calendar Calendarclient;

    CalendarModel model = new CalendarModel();
    int numAsyncTasks;

    ArrayAdapter<CalendarInfo> adapter;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendarlist);

        this.credential = MainActivity.credential;
        credential.setSelectedAccountName(MainActivity.accountnameStr);
        this.Calendarclient = MainActivity.Calendarclient;


        listView = (ListView) findViewById(R.id.list);
        registerForContextMenu(listView);

    }
    @Override
    protected void onResume() {
        super.onResume();
        if(credential.getSelectedAccountName() !=null) {
            new AsyncLoadCalendarList(this).execute();
        }else{
            if (checkGooglePlayServicesAvailable()) {
                haveGooglePlayServices();
            }
        }


    }

    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, CalendarListActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    private void haveGooglePlayServices() {
        // check if there is already an account selected
        if (credential.getSelectedAccountName() == null) {
            // ask user to choose account
            chooseAccount();
        } else {
            // load calendars
            new AsyncLoadCalendarList(this).execute();
        }
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else {
                    checkGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    new AsyncLoadCalendarList(this).execute();
                } else {
                    chooseAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        new AsyncLoadCalendarList(this).execute();
                    }
                }
                break;
            case ADD_OR_EDIT_CALENDAR_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Calendar calendar = new Calendar();
                    calendar.setSummary(data.getStringExtra("summary"));
                    String id = data.getStringExtra("id");
                    if (id == null) {
                        new AsyncInsertCalendarList(this, calendar).execute();
                    } else {
                        calendar.setId(id);
                        new AsyncUpdateCalendarList(this, id, calendar).execute();
                    }
                }
                break;
        }
    }

    void refreshView() {
        adapter = new ArrayAdapter<CalendarInfo>(
                this, android.R.layout.simple_list_item_1, model.toSortedArray()) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // by default it uses toString; override to use summary instead
                TextView view = (TextView) super.getView(position, convertView, parent);
                CalendarInfo calendarInfo = getItem(position);
                view.setText(calendarInfo.summary);
                return view;
            }
        };
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.calendarlist_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                new AsyncLoadCalendarList(this).execute();
                break;
            case R.id.menu_accounts:
                chooseAccount();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CONTEXT_EDIT, 0, R.string.edit);
        menu.add(0, CONTEXT_DELETE, 0, R.string.delete);
        menu.add(0, CONTEXT_BATCH_ADD, 0, R.string.Batch_add);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int calendarIndex = (int) info.id;
        if (calendarIndex < adapter.getCount()) {
            final CalendarInfo calendarInfo = adapter.getItem(calendarIndex);
            switch (item.getItemId()) {
                case CONTEXT_EDIT:
                    startAddOrEditCalendarActivity(calendarInfo);
                    return true;
                case CONTEXT_DELETE:
                    new AlertDialog.Builder(this).setTitle(R.string.delete_title)
                            .setMessage(calendarInfo.summary)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    new AsyncDeleteCalendarList(CalendarListActivity.this, calendarInfo).execute();
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .create()
                            .show();
                    return true;
                case CONTEXT_BATCH_ADD:
                    List<Calendar> calendars = new ArrayList<Calendar>();
                    for (int i = 0; i < 3; i++) {
                        Calendar cal = new Calendar();
                        cal.setSummary(calendarInfo.summary + " [" + (i + 1) + "]");
                        calendars.add(cal);
                    }
                    new AsyncBatchInsertCalendarList(this, calendars).execute();
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    public void onAddClick(View view) {
        startAddOrEditCalendarActivity(null);
    }

    private void startAddOrEditCalendarActivity(CalendarInfo calendarInfo) {
        Intent intent = new Intent(this, AddOrEditCalendarListActivity.class);
        if (calendarInfo != null) {
            intent.putExtra("id", calendarInfo.id);
            intent.putExtra("summary", calendarInfo.summary);
        }

        startActivityForResult(intent, ADD_OR_EDIT_CALENDAR_REQUEST);
    }
}
