package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action
                = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            startService(context);

        }
    }

    private void startService(Context context) {
        Intent appIntent = new Intent(context, BootService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(appIntent);
        } else {
            context.startService(appIntent);
        }
    }
}
