package pace.hearts.riftshuttler.reservation.timetable;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import pace.hearts.riftshuttler.R;

public class TimeSlot extends AppCompatTextView {
    // TODO: connectedTo field
    private int seatsTaken, totalSeats;

    // private DocumentReference routes;

    public TimeSlot(@NonNull Context context) {
        this(context, "01-01-1970", "00:00 AM", "-");
    }

    public TimeSlot(@NonNull Context context, @NonNull String date, @NonNull String time, @NonNull String route) {
        super(context);

        setTextColor(ContextCompat.getColor(context, R.color.white));
        setGravity(Gravity.CENTER);
        setPadding(2, 0, 2, 0);
        setTextSize(12);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reservations = db.collection("reservations");
        DocumentReference document = reservations.document(date);
        document.addSnapshotListener((snapshot, e) -> {
            if (e != null)
                return; // TODO: log or show error toast

            if (snapshot != null && snapshot.exists() && snapshot.getData() != null && snapshot.getData().containsKey(route)) {
                HashMap<?, ?> routeMap = (HashMap<?, ?>) snapshot.getData().get(route);

                if (routeMap != null && routeMap.containsKey(time)) {
                    HashMap<?, ?> timeMap = (HashMap<?, ?>) routeMap.get(time);

                    totalSeats = (timeMap != null && timeMap.containsKey("totalSeats")) ?
                            ((Long) Objects.requireNonNull(timeMap.get("totalSeats"))).intValue() : 0;

                    ArrayList<?> reservedBy = timeMap != null && timeMap.containsKey("reservedBy") ?
                            (ArrayList<?>) timeMap.get("reservedBy") : new ArrayList<>();
                    seatsTaken = reservedBy != null ? reservedBy.size() : 0;

                    setText(String.format(Locale.US, "%d/%d%nocc.", seatsTaken, totalSeats));

                    setClickable(true);
                    setLongClickable(true);

                    setOnClickListener(v -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String email = user != null ? user.getEmail() : "";
                        boolean reserved = reservedBy != null && reservedBy.contains(email);

                        // TODO: clean this up a bit; separate into multiple lines
                        new AlertDialog.Builder(getContext()).setTitle(route + ", " + time)
                                .setNegativeButton("Back", (dialog, id) -> dialog.cancel())
                                .setNeutralButton("View GPS", (dialog, id) -> {
                                    // TODO: GPS
                                })
                                .setPositiveButton(reserved ? "Cancel" : "Reserve",
                                        (dialog, id) -> {
                                            if (!isFull()) {
                                                document.update(route + "." + time + ".reservedBy", !reserved
                                                        ? FieldValue.arrayUnion(email) : FieldValue.arrayRemove(email))
                                                        .addOnCompleteListener(task -> {
                                                            if (task.isSuccessful())
                                                                Toast.makeText(getContext(), !reserved ?
                                                                        "Seat reserved!" : "Reservation canceled...", Toast.LENGTH_SHORT).show();
                                                        });
                                            } else
                                                setBackgroundColor(ContextCompat.getColor(getContext(), R.color.pace_accent_red));
                                        })
                                .show();
                    });
                    setBackgroundColor(ContextCompat.getColor(getContext(), isFull() ?
                            R.color.pace_accent_red : R.color.pace_accent_green));
                }
            }
        });
    }

    @Override
    public void setBackgroundColor(int color) {
        StateListDrawable background = new StateListDrawable();
        background.addState(new int[]{android.R.attr.state_pressed},
                new ColorDrawable(ContextCompat.getColor(getContext(), R.color.pace_primary_dark)));
        background.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(color));
        setBackgroundDrawable(background);
    }

    public void clearSlot() {
        setText(null);
        setClickable(false);
        setLongClickable(false);
        setOnClickListener(null);
        super.setBackgroundColor(Color.TRANSPARENT);
    }

    public boolean isFull() {
        return seatsTaken >= totalSeats;
    }
}
