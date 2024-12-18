package com.example.laba6;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        Log.d(TAG, "Received alarm: Title=" + title + ", Text=" + text);

        NotificationManagerHelper.showNotification(context, title, text);
    }
}
