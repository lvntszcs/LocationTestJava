package com.example.locationtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.locationtest.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkPermissionAndRun(
                Manifest.permission.ACCESS_FINE_LOCATION,
                new LocationRunnable(this, fusedLocationClient),
                1000
        );

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {


                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Couldn't retrieve token.", Toast.LENGTH_LONG).show();

                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.wtf("WTF", token);

                        // Log and toast
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_LONG).show();
                    }
                });
    }


    @SuppressLint("NewApi")
    private void sendNotification(String messageBody) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // For Android Oreo and above, you need to create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new Notification.Builder(this, "default_channel")
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .build();

        notificationManager.notify(0, notification);
    }

    public class LocationRunnable implements Runnable {
        private final FusedLocationProviderClient fusedLocationClient;
        private final Context context;
        public LocationRunnable(Context context, FusedLocationProviderClient fusedLocationClient){
            this.fusedLocationClient = fusedLocationClient;
            this.context = context;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        setContentView(R.layout.activity_main);
                        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                            return insets;
                        });

                        TextView coordinateInfo = findViewById(R.id.coordinates);

                        coordinateInfo.setText(String.format(Locale.ENGLISH, "%f, %f", latitude, longitude));
                        sendNotification("test");
                    } else {
                        Toast.makeText(
                                context,
                                "Location not found.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            });
        }
    }

    public void onRefreshClick(View view){
        checkPermissionAndRun(
                Manifest.permission.ACCESS_FINE_LOCATION,
                new LocationRunnable(this, fusedLocationClient),
                1000
        );
        Toast.makeText(this, "Refresh initiated!", Toast.LENGTH_SHORT).show();
    }

    private void checkPermissionAndRun(String requestedPermission, Runnable program,  int requestCode){
        if (ContextCompat.checkSelfPermission(
                this, requestedPermission) ==
                PackageManager.PERMISSION_GRANTED) {
            program.run();
        }
        /*
        else if(ActivityCompat.shouldShowRequestPermissionRationale(
                this, requestedPermission)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected, and what
            // features are disabled if it's declined. In this UI, include a
            // "cancel" or "no thanks" button that lets the user continue
            // using your app without granting the permission.
        }
        */
        else {
            // You can directly ask for the permission.
            requestPermissions(
                    new String[] { requestedPermission },
                    requestCode
            );
        }
    }

    /*
    @SuppressLint("ObsoleteSdkInt")
    public static void requestPermission(Activity activity, String permission, int requestCode) {
        // Check if the permission is already granted
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            // If not, request the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Show rationale if needed (optional, depending on your use case)
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    // You could show a dialog here explaining why the permission is needed
                    // and then request the permission again.
                }
                // Request the permission
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
        }
    }
    */

}