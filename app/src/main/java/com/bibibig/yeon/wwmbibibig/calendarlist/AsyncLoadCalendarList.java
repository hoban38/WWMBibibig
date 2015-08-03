package com.bibibig.yeon.wwmbibibig.calendarlist;

import com.google.api.services.calendar.model.CalendarList;

import java.io.IOException;

/**
 * Created by yeon on 2015-08-04.
 */
public class AsyncLoadCalendarList extends CalendarListAsyncTask {
    AsyncLoadCalendarList(CalendarListActivity activity) {
        super(activity);
    }

    @Override
    protected void doInBackground() throws IOException {
        CalendarList feed = client.calendarList().list().setFields(CalendarInfo.FEED_FIELDS).execute();
        model.reset(feed.getItems());
    }

//    static void run(CalendarListActivity CalendarList) {
//        new AsyncLoadCalendarList(CalendarList).execute();
//    }
}
