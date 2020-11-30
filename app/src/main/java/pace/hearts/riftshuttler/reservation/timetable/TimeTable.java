package pace.hearts.riftshuttler.reservation.timetable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import pace.hearts.riftshuttler.R;

public class TimeTable extends LinearLayout {
    private List<String> columns;
    private int rows, rowHeight, startingHour;

    private LinearLayout timeTableLayout;

    private boolean isInitialized = false, isLoading = true;

    public TimeTable(@NonNull Context context) {
        this(context, null);
    }

    public TimeTable(@NonNull Context context, @Nullable AttributeSet attributes) {
        super(context, attributes);
        inflate(context, R.layout.layout_time_table, this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!isInitialized) {
            rowHeight = Math.round((bottom - top) / 15f);
            columns = new ArrayList<>();
            columns.add(getResources().getString(R.string.time_table_time_header));
            isInitialized = true;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        timeTableLayout = findViewById(R.id.timeTableLayout);
    }

    public void initTimeTable(String date) {
        timeTableLayout.removeAllViews();
        isLoading = true;

        LayoutParams titleRowParams = new LayoutParams(LayoutParams.MATCH_PARENT, rowHeight / 2);
        LayoutParams titleColParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 0.5f);
        LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, rowHeight);
        LayoutParams slotParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 0.5f);
        slotParams.setMargins(2, 2, 2, 2);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reservations = db.collection("reservations");
        DocumentReference document = reservations.document(date);
        document.get().addOnSuccessListener(result -> {
            if (result != null && result.exists() && result.getData() != null) {
                HashMap<String, ?> resultMap = (HashMap<String, ?>) result.getData();

                startingHour = (resultMap.containsKey("startingHour")) ?
                        Objects.requireNonNull(result.getLong("startingHour")).intValue() : 0;
                int finishingHour = resultMap.containsKey("finishingHour") ?
                        Objects.requireNonNull(result.getLong("finishingHour")).intValue() : 0;

                rows = (finishingHour - startingHour) * 4;

                for (Map.Entry<String, ?> entry : resultMap.entrySet())
                    if (!entry.getKey().equals("startingHour") && !entry.getKey().equals("finishingHour"))
                        columns.add(entry.getKey());

                // TODO: change -15 to 0 if switching to a table header
                for (int t = -15, i = 0; t <= rows * 15; t += 15, i++) {
                    LinearLayout tableRow = new LinearLayout(getContext());

                    tableRow.setOrientation(LinearLayout.HORIZONTAL);
                    tableRow.setLayoutParams(i == 0 ? titleRowParams : rowParams);
                    tableRow.setBackgroundResource(i % 2 != 0 ? R.color.cloud : R.color.white);
                    tableRow.setGravity(Gravity.CENTER);

                    for (int j = 0; j < columns.size(); j++) {
                        String time = new SimpleDateFormat("hh:mm a", Locale.US)
                                .format(getTimeAdded(t));
                        TimeSlot slot = new TimeSlot(getContext(), date, time, columns.get(j));
                        slot.setLayoutParams(j == 0 ? titleColParams : slotParams);

                        if (i > 0 && j == 0) {
                            slot.clearSlot();
                            slot.setText(time);
                            slot.setTextColor(ContextCompat.getColor(getContext(), R.color.pace_primary_dark));
                        }
                        tableRow.addView(slot);
                    }
                    timeTableLayout.addView(tableRow);
                }

                // TODO: make first row static while scrolling; separate table header?
                LinearLayout titleRow = (LinearLayout) timeTableLayout.getChildAt(0);
                for (int i = 0; i < columns.size(); i++) {
                    TimeSlot slot = (TimeSlot) titleRow.getChildAt(i);
                    if (slot != null) {
                        slot.clearSlot();
                        slot.setText(columns.get(i));
                        slot.setTextColor(ContextCompat.getColor(getContext(), R.color.pace_primary_dark));
                    }
                }
            } else {
                reservations.document(getResources().getString(R.string.time_table_weekday_template)).get().addOnSuccessListener(template -> {
                    if (template != null && template.exists() && template.getData() != null)
                        document.set(template.getData());
                    initTimeTable(date);
                }).addOnFailureListener(e -> {
                    // TODO: show empty schedule view
                    Toast.makeText(getContext(), "Empty schedule; template could not be found...", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            // TODO: show error view
            Toast.makeText(getContext(), "Error connecting to database...", Toast.LENGTH_SHORT).show();
        }).addOnCompleteListener(task -> isLoading = false);
    }

    private Date getTimeAdded(int minutes) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, startingHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isLoading() {
        return isLoading;
    }
}
