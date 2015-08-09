package com.bibibig.yeon.wwmbibibig.calendarevent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bibibig.yeon.wwmbibibig.R;
import com.bibibig.yeon.wwmbibibig.common.BasicInfo;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class EventListActivity extends Activity {
    static final String TAG = "EventListActivity";

    EventModel model = new EventModel();
    int numAsyncTasks;

    ArrayAdapter<EventInfo> adapter;
    private ListView eventlist;
    public static String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_event_list);

        Intent i = getIntent();
        id= i.getStringExtra("id");
        eventlist = (ListView) findViewById(R.id.eventlist);
    }
    protected void onResume(){
        super.onResume();
        if(BasicInfo.credential.getSelectedAccountName() !=null) {
            new AsyncLoadCalendarevent(this).execute();
        }
    }
    void refreshView(){
        adapter = new ArrayAdapter<EventInfo>(this,android.R.layout.simple_list_item_1,model.toSortedArray()){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // by default it uses toString; override to use summary instead
                TextView view = (TextView) super.getView(position, convertView, parent);
                EventInfo eventinfo = getItem(position);
                view.setText(eventinfo.summary);
                return view;
            }
        };
        eventlist.setAdapter(adapter);
    }
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, EventListActivity.this, BasicInfo.REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }
}
