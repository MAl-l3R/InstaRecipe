package com.example.instarecipe.ui.startupPage;

 import android.app.ProgressDialog;
import android.content.Intent;
 import android.os.Bundle;
 import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

 import com.example.instarecipe.R;
 import com.example.instarecipe.ui.profilePage.ProfileSetupActivity;
 import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CreateAccountActivity extends AppCompatActivity {

    TextView loginToAccount;
    EditText emailEditText, pwEditText, confirmPwEditText;
    String email, password, confirmPassword;
    MaterialButton createAccountBtn;
    ProgressDialog progressDialog;
    FirebaseAuth fbAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        emailEditText = findViewById(R.id.email_address);
        pwEditText = findViewById(R.id.password);
        confirmPwEditText = findViewById(R.id.forgot_password);
        createAccountBtn = findViewById(R.id.create_account_button);
        loginToAccount = findViewById(R.id.LoginToAccount);
        progressDialog = new ProgressDialog(this);
        fbAuth = FirebaseAuth.getInstance();

        // Create Account Button
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = emailEditText.getText().toString();
                password = pwEditText.getText().toString();
                confirmPassword = confirmPwEditText.getText().toString();

                // Validate the data
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Please enter a valid email address");
                } else if (password.isEmpty() || password.length() < 6){
                    pwEditText.setError("Password needs to be at least 6 characters long");
                } else if (!password.equals(confirmPassword)) {
                    confirmPwEditText.setError("Password does not match");
                } else {
                    // Start Loading screen
                    progressDialog.setMessage("Creating Account...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    // Create account with email and password using firebase database
                    fbAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                toggleLoginState("Create", CreateAccountActivity.this, ProfileSetupActivity.class);
                                Toast.makeText(CreateAccountActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(CreateAccountActivity.this, "This email is already in use", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        // Log in to registered account
        loginToAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
            }
        });
    }

    private void toggleLoginState(String signInMethod, android.content.Context from, Class to) {
        // Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
        Intent intent = new Intent(from, to);
        if (!signInMethod.equals("Sign Out")) {
            intent.putExtra("SignInMethod", signInMethod);
        }
        if (signInMethod.equals("Create")) {
            intent.putExtra("email", email);
        }
        // Make sure user can't come back to this screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

