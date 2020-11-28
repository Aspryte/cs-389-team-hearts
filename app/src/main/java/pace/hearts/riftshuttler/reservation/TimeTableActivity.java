package pace.hearts.riftshuttler.reservation;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pace.hearts.riftshuttler.R;
import pace.hearts.riftshuttler.reservation.timetable.TimeTable;

public class TimeTableActivity extends AppCompatActivity {
    private TimeTable timeTable;
    private String date;

    private Runnable tableInitializer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);
        setSupportActionBar(findViewById(R.id.timeTableToolbar));

        Bundle bundle = getIntent().getExtras();
        date = bundle.containsKey("date") ? bundle.getString("date") :
                new SimpleDateFormat("dd-MM-yy", Locale.US).format(new Date(0));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setElevation(1F);
            actionBar.setTitle(date);
        }

        timeTable = findViewById(R.id.timeTable);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Handler tableHandler = new Handler();
        tableInitializer = () -> {
            if (timeTable.isInitialized()) {
                timeTable.initTimeTable(date);
                tableHandler.removeCallbacks(tableInitializer);
            } else
                tableHandler.postDelayed(tableInitializer, 500);
        };
        tableHandler.post(tableInitializer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}