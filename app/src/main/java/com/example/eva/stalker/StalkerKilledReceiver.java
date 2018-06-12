package com.example.eva.stalker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StalkerKilledReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, StalkerService.class));
    }
}
