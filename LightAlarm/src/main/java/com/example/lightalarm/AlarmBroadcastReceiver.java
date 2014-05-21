package com.example.lightalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Greg on 5/20/14.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("LightAlarn", "Broadcast Received");
        Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
    }

}
