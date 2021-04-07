package com.codedesign.locationbackgroundtryout;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyJobScheduler extends JobService {

    SimpleDateFormat simpleDateFormat;
    String timeStamp;
    DatabaseReference reference;
    private static final String TAG = "MyJobScheduler";
    private boolean jobCancelled = false;
    @Override
    public boolean onStartJob(JobParameters params) {

        Calendar calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        timeStamp = simpleDateFormat.format(calendar.getTime().getTime());

        reference = FirebaseDatabase.getInstance().getReference().child("timeList").child(timeStamp);

        Log.d(TAG, "Job started");
        doBackgroundWork(params);
        return true;
    }
    private void doBackgroundWork(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                reference.setValue("Test child");

                jobFinished(params, false);
            }
        }).start();
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }
}

