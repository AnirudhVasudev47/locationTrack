package com.codedesign.locationbackgroundtryout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    SharedPreferences sharedPreferences;
    DatabaseReference reference, chartReference;
    TextView locationUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationUpdate = findViewById(R.id.LocationUpdate);

        reference = FirebaseDatabase.getInstance().getReference().child("LocationTrack");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locationUpdate.setText(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sharedPreferences = getApplicationContext().getSharedPreferences("com.codedesign.locationbackgroundtryout", Context.MODE_PRIVATE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        sharedPreferences.edit().putInt("lastDay", (int) calendar.getTimeInMillis()).apply();

        SwipeButton swipeBtn = findViewById(R.id.swipeBtn);
        swipeBtn.setOnStateChangeListener(new OnStateChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onStateChange(boolean active) {
                Toast.makeText(MainActivity.this, "button: " + active, Toast.LENGTH_SHORT).show();
                if (active){
                    ComponentName componentName = new ComponentName(getApplicationContext(), MyJobScheduler.class);
                    JobInfo info = new JobInfo.Builder(123, componentName)
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .setPersisted(true)
                            .setPeriodic(15 * 60 * 1000)
                            .build();
                    JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                    int resultCode = scheduler.schedule(info);
                    if (resultCode == JobScheduler.RESULT_SUCCESS) {
                        Log.d(TAG, "Job scheduled");
                    } else {
                        Log.d(TAG, "Job scheduling failed");
                    }

                    LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
                    Context context = MainActivity.this;
                    boolean gps_enabled = false;
                    boolean network_enabled = false;
                    try {
                        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch(Exception ex) {}

                    try {
                        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch(Exception ex) {}

                    if(!gps_enabled && !network_enabled) {
                        // notify user
                        new AlertDialog.Builder(context)
                                .setMessage("Please turn on Location")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }

                    Intent serviceIntent = new Intent(getApplicationContext(), LocationService.class);
                    ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);

                } else {
                    JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                    scheduler.cancel(123);
                    Log.d(TAG, "Job cancelled");

                    Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
                    stopService(serviceIntent);
                }
            }
        });

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {

            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();

        plotGraph();


    }

    private void plotGraph() {

    }


}