package com.example.googlelogin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GoogleSignIn";
    private static final String OAUTH_ID = BuildConfig.OAUTH_ID;

    private CredentialManager credentialManager;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        credentialManager = CredentialManager.create(this);
        executor = Executors.newSingleThreadExecutor();

        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener( v -> googleSignIn());
    }

    @SuppressLint("CheckResult")
    private void googleSignIn() {
        GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(OAUTH_ID)
                .setAutoSelectEnabled(true)
//                .setNonce()
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(getGoogleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        handleSignIn(response);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Google Sign-In failed", e);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
        );

    }

    private void handleSignIn(GetCredentialResponse response) {
        Object credential = response.getCredential();

        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential;

            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {

                GoogleIdTokenCredential googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(customCredential.getData());

                String idToken = googleIdTokenCredential.getIdToken();
                Log.e(TAG, "Google ID Token " + idToken);

                String email = googleIdTokenCredential.getId();
                Log.e(TAG, "Google ID " + email);

                Intent intent = new Intent(this, SuccessActivity.class);
                intent.putExtra("idToken", idToken);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else {
                Log.e(TAG, "Unexpected credential type");
            }
        } else  {
            Log.e(TAG, "Unexpected credential type2");
        }
    }
}