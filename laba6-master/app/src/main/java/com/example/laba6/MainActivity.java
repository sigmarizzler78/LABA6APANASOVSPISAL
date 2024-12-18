package com.example.laba6;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ReminderDatabaseHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> reminderList;
    private ArrayList<Long> reminderIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new ReminderDatabaseHelper(this);
        reminderList = new ArrayList<>();
        reminderIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reminderList);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            long reminderId = reminderIds.get(position);
            dbHelper.deleteReminder(reminderId);
            loadReminders();
        });

        findViewById(R.id.addButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddReminderActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }

    private void loadReminders() {
        reminderList.clear();
        reminderIds.clear();

        Cursor cursor = dbHelper.getReminders();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                long dateTime = cursor.getLong(cursor.getColumnIndexOrThrow("date_time"));

                reminderList.add(title + " - " + android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", dateTime));
                reminderIds.add(id);
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }
}
