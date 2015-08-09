package com.bibibig.yeon.wwmbibibig.calendarevent;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsyncLoadCalendarevent extends EventListAsyncTask{

    AsyncLoadCalendarevent(EventListActivity activity) {
        super(activity);
    }

    @Override
    protected void doInBackground() throws IOException {

        DateTime now = new DateTime(System.currentTimeMillis());
        List<String> eventStrings = new ArrayList<String>();

        Events feeds = client.events().list(EventListActivity.id)
                .setMaxResults(10)
                .setTimeMax(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        model.reset(feeds.getItems());

    }
}