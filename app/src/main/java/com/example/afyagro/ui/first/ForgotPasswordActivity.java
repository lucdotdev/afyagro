package com.example.afyagro.ui.first;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.afyagro.R;
import com.example.afyagro.widgets.CustomLoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Récupération de mot de passe oublié par OTP (SMS).
 *
 * Déroulé :
 *  1. L'utilisateur saisit son numéro de téléphone. On vérifie qu'un compte
 *     existe dans Firestore puis on envoie un code par SMS ({@link PhoneAuthProvider}).
 *  2. L'utilisateur saisit le code reçu, qui est vérifié via Firebase Auth.
 *  3. L'utilisateur définit un nouveau mot de passe, enregistré dans le
 *     document "users" correspondant.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private final CustomLoadingDialog customLoadingDialog = new CustomLoadingDialog(this);

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private View phoneTab, codeTab, passwordTab;
    private EditText phoneInput, codeInput, newPasswordInput;

    private String verificationId = "";
    private String userDocId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("fr");
        firebaseFirestore = FirebaseFirestore.getInstance();

        phoneTab = findViewById(R.id.forgotPhoneTab);
        codeTab = findViewById(R.id.forgotCodeTab);
        passwordTab = findViewById(R.id.forgotPasswordTab);

        phoneInput = findViewById(R.id.forgotPhone);
        codeInput = findViewById(R.id.forgotCode);
        newPasswordInput = findViewById(R.id.forgotNewPassword);

        Button sendCodeBtn = findViewById(R.id.forgotSendCodeBtn);
        Button verifyBtn = findViewById(R.id.forgotVerifyBtn);
        Button savePasswordBtn = findViewById(R.id.forgotSavePasswordBtn);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // La vérification automatique reste prise en charge manuellement par l'utilisateur.
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                customLoadingDialog.dismissLoading();
                Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String id,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                verificationId = id;
                customLoadingDialog.dismissLoading();
                showStep(2);
                Toast.makeText(ForgotPasswordActivity.this,
                        R.string.forgot_code_sent, Toast.LENGTH_LONG).show();
            }
        };

        sendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserThenSendOtp();
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode();
            }
        });

        savePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewPassword();
            }
        });
    }

    private void checkUserThenSendOtp() {
        final String phone = phoneInput.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, R.string.forgot_phone_hint, Toast.LENGTH_LONG).show();
            return;
        }
        customLoadingDialog.startLoading();
        firebaseFirestore.collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null
                                && !task.getResult().getDocuments().isEmpty()) {
                            userDocId = task.getResult().getDocuments().get(0).getId();
                            sendOtp(phone);
                        } else {
                            customLoadingDialog.dismissLoading();
                            Toast.makeText(ForgotPasswordActivity.this,
                                    R.string.forgot_user_not_found, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendOtp(String phone) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+243" + phone,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }

    private void verifyCode() {
        String code = codeInput.getText().toString().trim();
        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(verificationId)) {
            Toast.makeText(this, R.string.forgot_code_hint, Toast.LENGTH_LONG).show();
            return;
        }
        customLoadingDialog.startLoading();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        customLoadingDialog.dismissLoading();
                        if (task.isSuccessful()) {
                            showStep(3);
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveNewPassword() {
        String newPassword = newPasswordInput.getText().toString().trim();
        if (newPassword.length() < 6) {
            Toast.makeText(this, R.string.forgot_password_too_short, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(userDocId)) {
            Toast.makeText(this, R.string.forgot_user_not_found, Toast.LENGTH_LONG).show();
            return;
        }
        customLoadingDialog.startLoading();
        firebaseFirestore.collection("users")
                .document(userDocId)
                .update("password", newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        customLoadingDialog.dismissLoading();
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    R.string.forgot_password_updated, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ForgotPasswordActivity.this, LoginScreen.class));
                            finish();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showStep(int step) {
        phoneTab.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        codeTab.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        passwordTab.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
    }
}
