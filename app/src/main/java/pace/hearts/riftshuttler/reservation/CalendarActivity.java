package pace.hearts.riftshuttler.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import pace.hearts.riftshuttler.R;
import pace.hearts.riftshuttler.auth.LoginPageActivity;

public class CalendarActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        setSupportActionBar(findViewById(R.id.calendarToolbar));

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null)
            startActivity(new Intent(this, LoginPageActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(1F);
            actionBar.setTitle(getResources().getString(R.string.app_name));
        }

        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            String date = (month + 1) + "-" + dayOfMonth + "-" + year;
            startActivity(new Intent(this, TimeTableActivity.class)
                    .putExtra("date", date));
        });

        // TODO: move to user page
        Button logout = findViewById(R.id.logoutBtn);
        logout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginPageActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
    }
}