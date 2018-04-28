package com.example.eva.stalker;

import android.content.ComponentName;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageStats;
import android.os.Environment;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AsyncTask.execute(new Runnable() {

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
                return date + "." + getDeviceName() + ".log";
            }

            @Override
            public void run() {
                String folderName = Environment.getExternalStorageDirectory() + File.separator + "stalkerLog";

                String fName = getFileName();
//                File f = new File(getFilesDir(), fName);
//                f.delete();
                try {
                    FileOutputStream fOut = openFileOutput(fName, MODE_APPEND);
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
//                    outWriter.close();
//                    FileInputStream fIn = openFileInput(fName);
//                    InputStreamReader inputReader = new InputStreamReader(fIn);
//                    BufferedReader buffReader = new BufferedReader(inputReader);
//                    String line = "";
//                    int i = 0;
//                    while ((line = buffReader.readLine()) != null) {
//                        i++;
//                        Log.d("dbg", "LINE: " + line);
//                    }
//                    Log.d("dbg", String.valueOf(i));


                } catch (IOException e) {
                    Log.d("error", String.valueOf(e));
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
