package dev.eder.oreogeofence.geofences.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import dev.eder.oreogeofence.R;

public class MyFirebaseInstantService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseInstantService.class.getSimpleName();

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e(TAG, "onNewToken: "+s );
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().isEmpty()){
            sendNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }else{
            sendNotification(remoteMessage.getData());
        }
    }

    private void sendNotification(Map<String, String> data) {
        String title = data.get("title").toString();
        String body = data.get("body").toString();

        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "dev.eder.oreogeofence.geofences.test";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Android Gdlj Channel");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.setVibrationPattern(new long[]{0,1000,500,1000});
            channel.enableLights(true);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this,NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("Info");
        manager.notify(new Random().nextInt(),notificationBuilder.build());
    }

    private void sendNotification(String title, String body) {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "dev.eder.oreogeofence.geofences.test";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Android Gdlj Channel");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.setVibrationPattern(new long[]{0,1000,500,1000});
            channel.enableLights(true);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this,NOTIFICATION_CHANNEL_ID);
                notificationBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentInfo("Info");
                manager.notify(new Random().nextInt(),notificationBuilder.build());

    }
}
