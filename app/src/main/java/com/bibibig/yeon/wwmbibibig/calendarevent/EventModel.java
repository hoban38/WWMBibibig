package com.bibibig.yeon.wwmbibibig.calendarevent;

import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventModel {

    private final Map<String, EventInfo> events = new HashMap<String, EventInfo>();

    int size(){
        synchronized (events) {
            return events.size();
        }
    }
    void remove(String id){
        synchronized (events) {
           events.remove(id);
        }
    }

    EventInfo get(String id) {
        synchronized (events) {
            return events.get(id);
        }
    }

    void add(Event eventToAdd) {
        synchronized (events) {
            EventInfo found = get(eventToAdd.getId());
            if (found == null) {
                events.put(eventToAdd.getId(), new EventInfo(eventToAdd));
            } else {
                found.update(eventToAdd);
            }
        }
    }

    public void reset(List<Event> eventsToAdd) {
        synchronized (events) {
            events.clear();
            for (Event event : eventsToAdd) {
                add(event);
            }
        }
    }

    public EventInfo[] toSortedArray() {
        synchronized (events) {
            List<EventInfo> result = new ArrayList<EventInfo>();
            for (EventInfo event : events.values()) {
                result.add((EventInfo) event.clone());
            }
            Collections.sort(result);
            return result.toArray(new EventInfo[0]);
        }
    }
}
