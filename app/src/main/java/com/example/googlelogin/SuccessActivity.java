package com.example.googlelogin;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SuccessActivity extends AppCompatActivity {

    private CredentialManager credentialManager;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        credentialManager = CredentialManager.create(this);
        executor = Executors.newSingleThreadExecutor();

        Intent intent = getIntent();
        String idToken = intent.getStringExtra("idToken");
        String email = intent.getStringExtra("email");

        if (idToken != null) {
            Toast.makeText(this, idToken, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, email, Toast.LENGTH_SHORT).show();
        }

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> logOut());
    }

    private void logOut() {
        try {
            ClearCredentialStateRequest request = new ClearCredentialStateRequest();

            credentialManager.clearCredentialStateAsync(
                    request,
                    new CancellationSignal(),
                    executor,
                    new CredentialManagerCallback<Void, ClearCredentialException>() {
                        @Override
                        public void onResult(Void unused) {
                            runOnUiThread(() -> {
                                Intent intent = new Intent(SuccessActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        }

                        @Override
                        public void onError(@NonNull ClearCredentialException e) {
                            runOnUiThread(() ->
                                    Toast.makeText(SuccessActivity.this, "로그아웃 실패: "
                                            + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
            );
        } catch (Exception e) {
            Toast.makeText(this, "로그아웃 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}