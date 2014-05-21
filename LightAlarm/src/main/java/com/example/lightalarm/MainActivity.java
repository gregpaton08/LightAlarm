package com.example.lightalarm;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends Activity {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private CheckBox checkbox_alarm;
    private TimePicker timePicker_alarm;

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

        checkbox_alarm = (CheckBox)findViewById(R.id.checkbox_alarm);

        timePicker_alarm = (TimePicker)findViewById(R.id.timepicker_alarmtime);
        timePicker_alarm.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                setAlarm(calendar);
            }
        });
    }

    public void onCheckboxAlarmClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        CheckBox cb = (CheckBox) view;

        if (checked) {
            cb.setText("On");
            setAlarmForCurrentTime();;
        }
        else {
            cb.setText("Off");
            cancelAlarm(getBaseContext());
        }
    }

    private PendingIntent createAlarmIntent(Context context) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private void setAlarm(Calendar calendar) {
        Context context = getBaseContext();

        // Cancel any current alarms
        cancelAlarm(context);

        if (checkbox_alarm.isChecked()) {
            alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarmIntent = createAlarmIntent(context);

            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    1000 * 60 * 60 * 24,
                    alarmIntent);
            String text = "Alarm set for " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
        else {
            String text = "Alarm NOT set for " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    private void setAlarmForCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, timePicker_alarm.getCurrentHour());
        calendar.set(Calendar.MINUTE, timePicker_alarm.getCurrentMinute());

        setAlarm(calendar);
    }

    private void cancelAlarm(Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = createAlarmIntent(context);

        alarmMgr.cancel(alarmIntent);
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
