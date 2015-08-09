package com.bibibig.yeon.wwmbibibig.calendarevent;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import static com.google.api.client.util.Objects.toStringHelper;


class EventInfo implements Comparable<EventInfo>, Cloneable{

    String id;
    String summary;
    DateTime start;

    public EventInfo(String id, String summary) {
        this.id = id;
        this.summary = summary;
    }

    public EventInfo(Event event){
        update(event);
    }

    @Override
    public String toString() {
        return toStringHelper(EventInfo.class).add("id",id).add("summary",summary).toString();
    }

    @Override
    public int compareTo(EventInfo other) {
        return summary.compareTo(other.summary);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }catch (CloneNotSupportedException exception){

            throw new RuntimeException(exception);
        }
    }

    public void update(Event event) {
        id = event.getId();
        summary = event.getSummary();
        start =event.getStart().getDateTime();
        if(start ==null){
            start = event.getStart().getDate();
        }
    }
}
