package com.example.afyagro.ui.first;


import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.example.afyagro.R;
import com.example.afyagro.helpers.StringOperation;
import com.example.afyagro.ui.home.MainActivity;
import com.example.afyagro.ui.home_.MainPharmerActivity;
import com.example.afyagro.widgets.CustomLoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mukesh.OtpView;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SignupScreen extends AppCompatActivity {


    private static final StringOperation stringOperation = new StringOperation();
    private final CustomLoadingDialog customLoadingDialog = new CustomLoadingDialog(this);


    EditText numberContent, passwordContent, nameContent, emailContent;
    LinearLayout verification_tab, informationLinear;
    OtpView verificationInput;


    Button buttonNext;
    TextView titletxt, desctxt;
    ProgressBar signupProgressBar;


    int step = 1;
    int accountType = 0;


    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String verification = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_screen);


        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.mAuth.setLanguageCode("fr");


        ///Content
        ///

        nameContent = findViewById(R.id.nameContent);
        passwordContent = findViewById(R.id.passwordContent);
        emailContent = findViewById(R.id.emailContent);
        numberContent = findViewById(R.id.numberContent);

        ///VIEW
        informationLinear = findViewById(R.id.informationLinear);

        verification_tab = findViewById(R.id.verification_tab);

        informationLinear.setVisibility(View.VISIBLE);
        verification_tab.setVisibility(View.GONE);


        buttonNext = findViewById(R.id.btn_next);
        verificationInput = findViewById(R.id.verificationInput);
        signupProgressBar = findViewById(R.id.progressSignupBar);


        titletxt = findViewById(R.id.titletxt);
        desctxt = findViewById(R.id.desctxt);


        verificationInput = findViewById(R.id.verificationInput);
        Button verificationButton = findViewById(R.id.verificationButon);


        verificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stringOperation.isEmpty(verificationInput)) {
                    Toast.makeText(SignupScreen.this, "Code vide", Toast.LENGTH_LONG).show();
                } else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification, Objects.requireNonNull(verificationInput.getText()).toString().trim());
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                @SuppressLint("RtlHardcoded") Transition transition = new Slide(Gravity.RIGHT);

                transition.setDuration(600);


                if (step == 1) {
                    if (stringOperation.isEmpty(nameContent)) {
                        Toast.makeText(SignupScreen.this, "Veuillez entrer un nom ", Toast.LENGTH_LONG).show();
                    } else if (!stringOperation.isValidEmail(emailContent)) {

                        Toast.makeText(SignupScreen.this, "Veuillez entrez une adresse mail valide", Toast.LENGTH_LONG).show();
                    } else if (accountType == 0) {
                        Toast.makeText(SignupScreen.this, "Veuillez selectionnner un type de compte", Toast.LENGTH_LONG).show();

                    } else {
                        verifyIfExist();
                    }
                } else if (step == 2) {
                    setTheProgress(2);
                    step++;
                }

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(SignupScreen.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(SignupScreen.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d("Code send:", "onCodeSent:" + verificationId);

                verification = verificationId;
                // ...
            }
        };


    }

    private void verifyIfExist(){
        @SuppressLint("RtlHardcoded") final Transition transition = new Slide(Gravity.RIGHT);

        transition.setDuration(600);
        customLoadingDialog.startLoading();
        firebaseFirestore.collection("users").whereEqualTo("phone", numberContent.getText().toString().trim()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    if( Objects.requireNonNull(task.getResult()).getDocuments().size()==0){
                        setTheProgress(1);
                        customLoadingDialog.dismissLoading();
                        transition.addTarget(R.id.verification_tab);
                        sendOtp(numberContent.getText().toString().trim());
                        TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.layout), transition);
                        informationLinear.setVisibility(View.GONE);
                        titletxt.setText("Verification");
                        desctxt.setText("Nous vous avons envoyé un code par Sms veuillez l'incrire ici dessous");
                        verification_tab.setVisibility(View.VISIBLE);
                        step++;
                    } else {
                        customLoadingDialog.dismissLoading();
                        Toast.makeText(SignupScreen.this, "Utilisateur existant", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    customLoadingDialog.dismissLoading();
                    Toast.makeText(SignupScreen.this, "Erruer:" + task.getException(), Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {


        customLoadingDialog.startLoading();


        Map<String, Object> reg_entry = new HashMap<>();
        reg_entry.put("phone", numberContent.getText().toString().trim());
        reg_entry.put("name", nameContent.getText().toString().trim());
        reg_entry.put("email", emailContent.getText().toString().trim());
        reg_entry.put("password", passwordContent.getText().toString().trim());
        reg_entry.put("account_type", accountType);
        reg_entry.put("photoPath", "");

        firebaseFirestore.collection("users").add(reg_entry).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    SharedPreferences.Editor editor = getSharedPreferences("AUTH", MODE_PRIVATE).edit();
                    editor.putString("auth_id", Objects.requireNonNull(task.getResult()).getId().trim());
                    editor.putInt("account_type", accountType);
                    editor.putBoolean("is_login", true);
                    editor.apply();
                    customLoadingDialog.ok();
                    if (accountType == 1) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                customLoadingDialog.dismissLoading();
                                Intent intent = new Intent(SignupScreen.this, MainPharmerActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }, 2000);
                    } else if (accountType == 2) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                customLoadingDialog.dismissLoading();
                                Intent intent = new Intent(SignupScreen.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }, 2000);
                    }
                } else {
                    customLoadingDialog.dismissLoading();
                }
            }
        });
    }


    public void sendOtp(String number) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+243" + number,
                60,
                TimeUnit.SECONDS,
                SignupScreen.this,
                mCallbacks);
    }

    public void setTheProgress(int k) {
        ObjectAnimator u = ObjectAnimator.ofInt(signupProgressBar, "progress", k);
        u.setDuration(500);
        u.setInterpolator(new LinearInterpolator());
        u.start();
    }


    public void onLoginClick(View view) {
        Intent loginIntent = new Intent(this, LoginScreen.class);
        startActivity(loginIntent);
        finish();
    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_vendeur:
                if (checked)
                    accountType = 1;
                break;
            case R.id.radio_acheteur:
                if (checked)
                    accountType = 2;
                break;
        }

    }
}