package com.bibibig.yeon.wwmbibibig.calendarlist;

import com.bibibig.yeon.wwmbibibig.common.BasicInfo;
import com.google.api.services.calendar.model.CalendarList;

import java.io.IOException;


public class AsyncLoadCalendarList extends CalendarListAsyncTask {
    AsyncLoadCalendarList(CalendarListActivity activity) {
        super(activity);
    }

    @Override
    protected void doInBackground() throws IOException {
        CalendarList feed = client.calendarList().list().setFields(BasicInfo.FEED_FIELDS).execute();
        model.reset(feed.getItems());
    }

}
