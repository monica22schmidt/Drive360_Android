package com.example.drive360_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private FirebaseDatabase firebaseDB;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database, root and feedback references.
        firebaseDB = FirebaseDatabase.getInstance();
        rootRef = firebaseDB.getReference();
        userRef = rootRef.child("users");
    }

    // Log the user in.
    public void login(View view) {
        // Get username and password.
        EditText usernameInput = findViewById(R.id.usernameInput);
        String username = usernameInput.getText().toString();
        EditText passwordInput = findViewById(R.id.passwordInput);
        String password = passwordInput.getText().toString();

        // Check for valid input.
        if (username != null && !username.equals("") && password != null && !password.equals("")) {
            // Check if login credentials matches.
            if (checkCredentials(username, password)) {
                goToMainScreen(username);
            } else {
                Toast.makeText(LoginActivity.this, "Invalid credentials. Please try again!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(LoginActivity.this, "Invalid input. Please try again!", Toast.LENGTH_LONG).show();
        }
    }

    // Check if username and password match.
    public boolean checkCredentials(String username, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.drive360_android", Context.MODE_PRIVATE);

        // Get expectedPassword from given username, default to empty string.
        String expectedPassword = sharedPreferences.getString(username, "");

        // Check if password matches as expected.
        return password.equals(expectedPassword);
    }

    // Redirect the user to main screen.
    public void goToMainScreen(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.drive360_android", Context.MODE_PRIVATE);

        // Set isAuthenticated to true and pass in username for main screen.
        sharedPreferences.edit().putBoolean("isAuthenticated", true).apply();
        sharedPreferences.edit().putString("username", username).apply();

        // Check whether user is app admin.
        checkAdminAuthorization(username);

        // Redirect the user to main screen.
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Check whether user is app admin.
    private void checkAdminAuthorization(final String username) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences sharedPreferences = getSharedPreferences("com.example.drive360_android", Context.MODE_PRIVATE);

                // Set isAdmin to true for user with admin authorization.
                boolean isAdmin = (Boolean) dataSnapshot.child(username).getValue();
                sharedPreferences.edit().putBoolean("isAdmin", isAdmin).apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Redirect the user to register screen.
    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
