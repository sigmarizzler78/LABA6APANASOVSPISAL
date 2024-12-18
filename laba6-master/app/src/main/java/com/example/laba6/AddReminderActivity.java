package com.example.laba6;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Date;

public class AddReminderActivity extends AppCompatActivity {
    private static final String TAG = "AddReminderActivity";
    private EditText titleInput, textInput;
    private Button dateButton, timeButton, saveButton;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        titleInput = findViewById(R.id.titleInput);
        textInput = findViewById(R.id.textInput);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        saveButton = findViewById(R.id.saveButton);

        calendar = Calendar.getInstance();

        dateButton.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                Log.d(TAG, "Date selected: " + year + "/" + (month + 1) + "/" + dayOfMonth);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        timeButton.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                Log.d(TAG, "Time selected: " + hourOfDay + ":" + minute);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePicker.show();
        });

        saveButton.setOnClickListener(v -> {
            if (isExactAlarmPermissionGranted()) {
                saveReminder();
            } else {
                requestExactAlarmPermission();
            }
        });
    }

    private boolean isExactAlarmPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            boolean canSchedule = alarmManager != null && alarmManager.canScheduleExactAlarms();
            Log.d(TAG, "Exact Alarm Permission Granted: " + canSchedule);
            return canSchedule;
        }
        return true;
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(TAG, "Requesting Exact Alarm Permission");
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }

    private void saveReminder() {
        String title = titleInput.getText().toString();
        String text = textInput.getText().toString();
        long dateTime = calendar.getTimeInMillis();

        Log.d(TAG, "Saving reminder: Title=" + title + ", Text=" + text + ", DateTime=" + new Date(dateTime));

        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(this);
        long id = dbHelper.addReminder(title, text, dateTime);

        Log.d(TAG, "Reminder saved with ID: " + id);

        scheduleNotification(id, title, text, dateTime);
        finish();
    }

    private void scheduleNotification(long id, String title, String text, long dateTime) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("text", text);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, dateTime, pendingIntent);
            Log.d(TAG, "Scheduled notification for: " + new Date(dateTime));
        } else {
            Log.e(TAG, "Failed to get AlarmManager service");
        }
    }
}
