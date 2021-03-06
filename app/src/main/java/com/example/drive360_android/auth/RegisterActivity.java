package com.example.drive360_android.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.drive360_android.MainActivity;
import com.example.drive360_android.R;
import com.example.drive360_android.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static com.example.drive360_android.Config.appStatsRef;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseDatabase firebaseDB;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database, root and feedback references.
        firebaseDB = FirebaseDatabase.getInstance();
        rootRef = firebaseDB.getReference();
        userRef = rootRef.child("users");
    }

    private void submitUser(User user) {
        // Send data to users branch on Firebase.
        userRef.child(user.username).setValue(user);
        incrementUserCount();
    }

    private void incrementUserCount() {
        appStatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long num_users = (Long) dataSnapshot.child("num_users").getValue();
                    appStatsRef.child("num_users").setValue(num_users + 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Log the user in.
    public void register(View view) {
        // Get username and password.
        EditText usernameInput = findViewById(R.id.usernameInput);
        String username = usernameInput.getText().toString();
        EditText passwordInput = findViewById(R.id.passwordInput);
        String password = passwordInput.getText().toString();

        // Get user role.
        RadioGroup roleGroup = (RadioGroup) findViewById(R.id.userRole);
        // Get selected role from radioGroup.
        int selectedId = roleGroup.getCheckedRadioButtonId();
        RadioButton roleButton = (RadioButton) findViewById(selectedId);
        String role = roleButton.getText().toString();

        // Check for valid input.
        if (username != null && !username.equals("") && password != null && !password.equals("") && role != null && !role.equals("")) {
            // Check if login credentials matches.
            checkUniqueUsername(username, password, role);
        } else {
            Toast.makeText(RegisterActivity.this, "Invalid input. Please try again!", Toast.LENGTH_LONG).show();
        }
    }

    // Check if username already exists.
    public void checkUniqueUsername(String username, String password, String role) {
        userRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if username already exists.
                if (dataSnapshot.exists()) {
                    Toast.makeText(RegisterActivity.this, "Username already taken. Please try another one!", Toast.LENGTH_LONG).show();
                } else {
                    User user = new User(username, password, role);
                    submitUser(user);
                    goToMainScreen(user.username);
                    Toast.makeText(RegisterActivity.this, "Sign up successful!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Redirect the user to main screen.
    public void goToMainScreen(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.drive360_android", Context.MODE_PRIVATE);

        // Set isAuthenticated to true, isAdmin to false and pass in username for main screen.
        sharedPreferences.edit().putBoolean("isAuthenticated", true).apply();
        sharedPreferences.edit().putBoolean("isAdmin", false).apply();
        sharedPreferences.edit().putString("username", username).apply();

        // Redirect the user to main screen.
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Redirect the user to login screen.
    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
