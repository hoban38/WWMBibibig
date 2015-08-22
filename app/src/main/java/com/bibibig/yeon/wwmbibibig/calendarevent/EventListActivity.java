package com.bibibig.yeon.wwmbibibig.calendarevent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bibibig.yeon.wwmbibibig.R;
import com.bibibig.yeon.wwmbibibig.common.BasicInfo;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class EventListActivity extends Activity {
    static final String TAG = "EventListActivity";

    private  final int CONTEXT_EDIT = 0;
    private  final int CONTEXT_DELETE = 1;
    private  final int CONTEXT_BATCH_ADD = 2;

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
        registerForContextMenu(eventlist);

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
    public void onAddClick(View v){
            startAddOrEditEventActivity(null);
    }

    private void startAddOrEditEventActivity(EventInfo eventinfo) {
        Intent i = new Intent(EventListActivity.this, AddOrEditEventActivity.class);
        if(eventinfo !=null){
            i.putExtra("id", eventinfo.id);
            i.putExtra("summary",eventinfo.summary);
            i.putExtra("starttime",eventinfo.start);
        }
        startActivityForResult(i,BasicInfo.ADD_OR_EDIT_EVENT_REQUEST);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,CONTEXT_EDIT,0,"Edit Event");
        menu.add(0,CONTEXT_DELETE,0,"Delete Evnet");
        menu.add(0,CONTEXT_BATCH_ADD,0,"Copy Event x3(Batch)");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id = (int) info.id;

        if(id < adapter.getCount()){
            final EventInfo eventinfo = adapter.getItem(id);
            switch (item.getItemId()){
                case CONTEXT_EDIT:
                    startAddOrEditEventActivity(eventinfo);
                    return true;
                case CONTEXT_DELETE:
                    new AlertDialog.Builder(this).setTitle("Delete Event?")
                            .setMessage(eventinfo.summary)
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setNegativeButton("No",null)
                            .create()
                            .show();
                    return true;
//                case CONTEXT_BATCH_ADD:
//                    List<Event> events = new ArrayList<Event>();
//                    for(int i =0; i<3 ; i++){
//                        Event event = new Event();
//                        event.setSummary(eventinfo.summary + "["+(i+1)+"]");
//                        events.add(event);
//                    }
//                    new AsyncBatchInsertEventList(this,events).execute();
//                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }


}
