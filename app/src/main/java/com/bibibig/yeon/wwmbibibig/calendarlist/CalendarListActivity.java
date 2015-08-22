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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bibibig.yeon.wwmbibibig.NavigationDrawerFragment;
import com.bibibig.yeon.wwmbibibig.R;
import com.bibibig.yeon.wwmbibibig.calendarevent.EventListActivity;
import com.bibibig.yeon.wwmbibibig.common.BasicInfo;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.services.calendar.model.Calendar;

import java.util.ArrayList;
import java.util.List;


public class CalendarListActivity extends NavigationDrawerFragment {
    static final String TAG = "CalendarListActivity";

    private final int CONTEXT_EDIT = 0;
    private final int CONTEXT_DELETE = 1;
    private final int CONTEXT_BATCH_ADD = 2;

    CalendarModel model = new CalendarModel();
    int numAsyncTasks;

    ArrayAdapter<CalendarInfo> adapter;
    private ListView listView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerForContextMenu(mDrawerListView);
    }
    @Override
    public void onResume() {
        super.onResume();
        if(BasicInfo.credential.getSelectedAccountName() !=null) {
            new AsyncLoadCalendarList(this).execute();
        }else{
            if (checkGooglePlayServicesAvailable()) {
                haveGooglePlayServices();
            }
        }
    }

    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity().getApplicationContext());
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, (Activity) getActivity().getApplicationContext(), BasicInfo.REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }


    private void haveGooglePlayServices() {
        // check if there is already an account selected
        if (BasicInfo.credential.getSelectedAccountName() == null) {
            // ask user to choose account
            chooseAccount();
        } else {
            // load calendars
            new AsyncLoadCalendarList(this).execute();
        }
    }

    private void chooseAccount() {
        startActivityForResult(BasicInfo.credential.newChooseAccountIntent(), BasicInfo.REQUEST_ACCOUNT_PICKER);

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {


            case BasicInfo.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else {
                    checkGooglePlayServicesAvailable();
                }
                break;
            case BasicInfo.REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    new AsyncLoadCalendarList(this).execute();
                } else {
                    chooseAccount();
                }
                break;
            case BasicInfo.REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        BasicInfo.credential.setSelectedAccountName(accountName);
                        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(BasicInfo.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        new AsyncLoadCalendarList(this).execute();
                    }
                }
                break;
            case BasicInfo.ADD_OR_EDIT_CALENDAR_REQUEST:
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
        adapter = new ArrayAdapter<CalendarInfo>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, model.toSortedArray()) {
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CalendarInfo calendarInfo = (CalendarInfo) parent.getAdapter().getItem(position);
                Toast.makeText(getActivity().getApplicationContext(), calendarInfo.id, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getActivity().getApplicationContext(), EventListActivity.class);
                i.putExtra("id", calendarInfo.id);
                i.putExtra("summary", calendarInfo.summary);
                startActivity(i);
            }
        });


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
                    new AlertDialog.Builder(getActivity().getApplicationContext()).setTitle(R.string.delete_title)
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
        Intent intent = new Intent(getActivity().getApplicationContext(), AddOrEditCalendarListActivity.class);
        if (calendarInfo != null) {
            intent.putExtra("id", calendarInfo.id);
            intent.putExtra("summary", calendarInfo.summary);
        }

        startActivityForResult(intent, BasicInfo.ADD_OR_EDIT_CALENDAR_REQUEST);
    }
}
