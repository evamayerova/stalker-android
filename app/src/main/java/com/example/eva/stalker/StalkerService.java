package com.example.eva.stalker;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class StalkerService extends Service {


    private String deviceName;
    private String folderName;
    private String fileName;

    public StalkerService(Context appContext) {
        deviceName = getDeviceName();
        folderName = Environment.getExternalStorageDirectory() + File.separator + "stalkerLog";
        fileName = getFileName();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("dbg", "onStartCommand");
        try {
            FileOutputStream fOut = openFileOutput(fileName, MODE_APPEND);
            OutputStreamWriter outWriter = new OutputStreamWriter(fOut);
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep((long) 0.5);
                    String currentApp = printForegroundTask();
                    Log.d("dbg", "current app: " + currentApp);
                    outWriter.append("timestamp: " + String.valueOf(System.currentTimeMillis()) + ", proc: " + currentApp + "\n");
                } catch (InterruptedException e) {
                    Log.d("error", String.valueOf(e));
                }
            }
        } catch (java.io.FileNotFoundException e) {
        } catch (java.io.IOException e) {
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("com.example.stalker.RestartStalker");
        sendBroadcast(broadcastIntent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private String printForegroundTask() {
        String currentApp = "NULL";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        return currentApp;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + '-' + model.replace(" ", "_");
    }

    public String getFileName() {
        Calendar now = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yy-MM-dd");
        String date = df.format(Calendar.getInstance().getTime());
        return date + "." + deviceName + ".log";
    }
}
