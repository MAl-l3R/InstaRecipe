package com.example.instarecipe.ui.startupPage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.instarecipe.MainActivity;
import com.example.instarecipe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    TextView createAccount, forgotPassword;
    EditText emailEditText, pwEditText;
    MaterialButton loginBtn;
    Button googleBtn;
    ProgressDialog progressDialog;
    FirebaseAuth fbAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email_address);
        pwEditText = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login_button);
        forgotPassword = findViewById(R.id.forgot_password);
        createAccount = findViewById(R.id.CreateNewAccount);
        googleBtn = findViewById(R.id.signinwithgoogle);
        progressDialog = new ProgressDialog(this);
        fbAuth = FirebaseAuth.getInstance();

        // Login Button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = pwEditText.getText().toString();

                // Validate the data
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Please enter a valid email address");
                } else if (password.isEmpty()) {
                    pwEditText.setError("Please enter password");
                } else {
                    // Start Loading screen
                    progressDialog.setMessage("Logging in...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    // Log in with email and password using firebase database
                    fbAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                toggleLoginState("Login", LoginActivity.this, MainActivity.class);
                                Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Email or Password is incorrect", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        // Forgot password?
        Context context = this;
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword(context);
            }
        });

        // Create a new account
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });

        // Sign in with Google button
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity((new Intent(LoginActivity.this, GoogleSignInActivity.class)));
            }
        });
    }

    private void toggleLoginState(String signInMethod, android.content.Context from, Class to) {
        // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        Intent intent = new Intent(from, to);
        if (!signInMethod.equals("Sign Out")) {
            intent.putExtra("SignInMethod", signInMethod);
        }
        // Make sure user can't come back to this screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void resetPassword(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
        builder.setTitle("Enter Registered Email Address");

        // Set up the layout for the dialog
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 0, 40, 0);

        // Create an EditText for the email address input
        final EditText emailEditText = new EditText(context);
        emailEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEditText.setHint("Email Address");
        emailEditText.setTextColor(Color.parseColor("#F4F4F4"));
        emailEditText.setHintTextColor(Color.parseColor("#F4F4F4"));
        layout.addView(emailEditText);

        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String emailAddress = emailEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(emailAddress) && Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                    // User clicked "OK", reset password
                    fbAuth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Please check your email to reset password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(context, "Please retry with a valid email address", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Cancel", dismiss the dialog
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
