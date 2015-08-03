package com.bibibig.yeon.wwmbibibig.calendarlist;

import android.os.AsyncTask;
import android.view.View;

import com.bibibig.yeon.wwmbibibig.R;
import com.bibibig.yeon.wwmbibibig.common.Utils;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.IOException;

abstract class CalendarListAsyncTask extends AsyncTask<Void,Void,Boolean>{
    final CalendarListActivity activity;
    final CalendarModel model;
    final com.google.api.services.calendar.Calendar client;
    private final View progressBar;

    CalendarListAsyncTask(CalendarListActivity activity) {
        this.activity = activity;
        model = activity.model;
        client = activity.Calendarclient;
        progressBar = activity.findViewById(R.id.title_refresh_progress);
    }

    abstract protected void doInBackground() throws IOException;
    @Override
    protected final Boolean doInBackground(Void... ignored) {
        try {
            doInBackground();
            return true;
        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            activity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());
        } catch (UserRecoverableAuthIOException userRecoverableException) {
            activity.startActivityForResult(
                    userRecoverableException.getIntent(), CalendarListActivity.REQUEST_AUTHORIZATION);
        } catch (IOException e) {
            Utils.logAndShow(activity, CalendarListActivity.TAG, e);
        }
        return false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.numAsyncTasks++;
        progressBar.setVisibility(View.VISIBLE);
    }
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected final void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (0 == --activity.numAsyncTasks) {
            progressBar.setVisibility(View.GONE);
        }
        if (success) {
            activity.refreshView();
        }
    }

}
