package com.example.lightalarm;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.client.methods.HttpGet;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends Activity {

    public static String PREFS_NAME = "main_activity_prefs";
    public static String PREFS_ERROR = "prefs_error";
    public static String LIGHT_IP_KEY = "light_url_key";

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    int m_minute;
    int m_hour;

    // UI Components
    Button button_lightOn;
    Button button_lightOff;
    Button button_tvAlarmTime;
    TextView textview_alarmTime;
    ToggleButton toggle_alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove title banner
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Lock screen in portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        button_lightOn = (Button)findViewById(R.id.button_lightOn);
        button_lightOff = (Button)findViewById(R.id.button_lightOff);
        textview_alarmTime = (TextView)findViewById(R.id.textview_alarmTime);
        button_tvAlarmTime = (Button)findViewById(R.id.button_tvAlarmTime);
        toggle_alarm = (ToggleButton)findViewById(R.id.toggle_alarm);

        int btWidth = (getWindowManager().getDefaultDisplay().getWidth() / 2) - 40;
        button_lightOn.setWidth(btWidth);
        button_lightOff.setWidth(btWidth);

        button_tvAlarmTime.setBackgroundColor(Color.TRANSPARENT);
    }

    public void onClickButtonLightOn(View view) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
        String ip = preferences.getString(LIGHT_IP_KEY, PREFS_ERROR);
        if (ip.equals(PREFS_ERROR)) {
            AlertDialog.Builder urlAlert = new AlertDialog.Builder(this);
            urlAlert.setTitle("IP Address");
            urlAlert.setMessage("Set ip address and port (ip:port)");
            final EditText input = new EditText(this);
            urlAlert.setView(input);
            urlAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String inputText = input.getText().toString();

                    SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(LIGHT_IP_KEY, inputText);
                    editor.commit();

                    setLight(true);
                }
            });
            urlAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            urlAlert.show();
        }
        else {
            setLight(true);
        }
    }

    public void onClickButtonLightOff(View view) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
        String ip = preferences.getString(LIGHT_IP_KEY, PREFS_ERROR);
        if (ip.equals(PREFS_ERROR)) {
            AlertDialog.Builder urlAlert = new AlertDialog.Builder(this);
            urlAlert.setTitle("IP Address");
            urlAlert.setMessage("Set ip address and port (ip:port)");
            final EditText input = new EditText(this);
            urlAlert.setView(input);
            urlAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String inputText = input.getText().toString();

                    SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(LIGHT_IP_KEY, inputText);
                    editor.commit();

                    setLight(false);
                }
            });
            urlAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            urlAlert.show();
        }
        else {
            setLight(false);
        }
    }

    public void setLight(boolean on) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        String ip = preferences.getString(LIGHT_IP_KEY, PREFS_ERROR);
        if (ip.equals(PREFS_ERROR)) {
            return;
        }

        String url = "http://" + ip;
        if (on) {
            url += "/light_on";
        }
        else {
            url += "/light_off";
        }

        // Check if WIFI is on. If off then turn on
        String wifiStatus = "";
        WifiManager wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
        if (false == wifiManager.isWifiEnabled()) {
            wifiStatus = AlarmBroadcastReceiver.TURN_WIFI_OFF;
            wifiManager.setWifiEnabled(true);
        }

        AlarmBroadcastReceiver abr = new AlarmBroadcastReceiver();
        abr.new HTTPRequest(getBaseContext()).execute(url/*, wifiStatus*/);
    }

    public void onClickTextViewAlarmTime(View view) {

        String time = textview_alarmTime.getText().toString();
        if (time.length() == 0) {
            return;
        }

        int hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
        int minute = Integer.parseInt(time.substring(time.indexOf(":") + 1, time.indexOf(" ")));
        if (-1 != time.indexOf("PM")) {
            hour += 12;
        }

        setAlarmDialog(MainActivity.this, hour, minute);
    }

    public void onToggleAlarm(View view) {
        ToggleButton tb = (ToggleButton)view;

        if (tb.isChecked()) {
            setAlarmDialog(MainActivity.this);
        }
        else {
            cancelAlarm(getBaseContext());
        }
    }

    public void setAlarmDialog(Context context) {
        setAlarmDialog(context, -1, -1);
    }

    public void setAlarmDialog(Context context, int hour, int minute) {

        if (hour < 0 || hour > 24 || minute < 0 || minute > 59) {
            Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog tpd = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);

                setAlarm(calendar);
            }
        }, hour, minute, false);
        tpd.show();
    }

    private PendingIntent createAlarmIntent(Context context) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private Calendar setAlarm(Calendar calendar) {
        Context context = getBaseContext();

        // Cancel any current alarms
        cancelAlarm(context);

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = createAlarmIntent(context);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                1000 * 60 * 60 * 24,
                alarmIntent);

//        Toast.makeText(context,
//                       "Alarm set for " + calendar.get(Calendar.HOUR_OF_DAY) +
//                       ":" + calendar.get(Calendar.MINUTE),
//                       Toast.LENGTH_SHORT).show();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String time = "";
        time += hour % 12 + ":";
        if (minute < 10) {
            time += "0";
        }
        time += minute;
        if (hour > 12) {
            time += " PM";
        }
        else {
            time += " AM";
        }
        textview_alarmTime.setText(time);

        return calendar;
    }

    private void cancelAlarm(Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = createAlarmIntent(context);

        alarmMgr.cancel(alarmIntent);

        textview_alarmTime.setText("");
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
