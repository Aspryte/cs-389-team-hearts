package pace.hearts.riftshuttler.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import pace.hearts.riftshuttler.R;
import pace.hearts.riftshuttler.reservation.CalendarActivity;

public class LoginPageActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    private EditText emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null)
            startActivity(new Intent(this, CalendarActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

        emailField = findViewById(R.id.loginEmail);
        passwordField = findViewById(R.id.loginPassword);

        emailField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        TextView registerText = findViewById(R.id.registerText);
        registerText.setOnClickListener(v -> startActivity(new Intent(this, RegisterPageActivity.class)));

        Button loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim().toLowerCase();
            String password = passwordField.getText().toString().trim().toLowerCase();

            validate(email, password);
        });
    }

    private void validate(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.missing_field_msg), Toast.LENGTH_LONG).show();
        else if (!email.endsWith(getResources().getString(R.string.email_end)))
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_email_msg), Toast.LENGTH_LONG).show();
        else if (password.length() < 8)
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.short_password_msg), Toast.LENGTH_LONG).show();
        else
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, CalendarActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                } else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed_auth_msg), Toast.LENGTH_SHORT).show();
            });
    }
}