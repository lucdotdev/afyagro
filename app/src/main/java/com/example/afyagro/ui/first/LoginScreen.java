package com.example.afyagro.ui.first;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.afyagro.R;
import com.example.afyagro.helpers.StringOperation;
import com.example.afyagro.ui.home.MainActivity;
import com.example.afyagro.ui.home_.MainPharmerActivity;
import com.example.afyagro.widgets.CustomLoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LoginScreen extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;

    private EditText phoneNumber, password;

    private final CustomLoadingDialog customLoadingDialog = new CustomLoadingDialog(this);
    private static final StringOperation stringOperation = new StringOperation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        this.password = findViewById(R.id.loginPassword);
        this.phoneNumber = findViewById(R.id.loginPhone);
        Button loginBtn = findViewById(R.id.loginBtn);

        this.firebaseFirestore = FirebaseFirestore.getInstance();


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stringOperation.isEmpty(password) || stringOperation.isEmpty(phoneNumber)) {
                    Toast.makeText(LoginScreen.this, "Veuillez remplir tout les champs", Toast.LENGTH_LONG).show();
                } else {
                    customLoadingDialog.startLoading();
                    firebaseFirestore.collection("users").whereEqualTo("phone", phoneNumber.getText().toString().trim()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isComplete()) {
                                if( Objects.requireNonNull(task.getResult()).getDocuments().size()>0){
                                    firebaseFirestore.collection("users").document(Objects.requireNonNull(task.getResult()).getDocuments().get(0).getId().trim()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            DocumentSnapshot t = task.getResult();
                                            assert t != null;
                                            if (Objects.equals(t.getString("password"), password.getText().toString())) {
                                                saveToPrefs((long) t.get("account_type"), t.getId());
                                                if ((long) t.get("account_type") == 1) {
                                                    Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            customLoadingDialog.dismissLoading();
                                                            Intent intent = new Intent(LoginScreen.this, MainPharmerActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }, 2000);
                                                } else if ((long) t.get("account_type") == 2) {
                                                    Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            customLoadingDialog.dismissLoading();
                                                            Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }, 2000);
                                                }
                                            } else {
                                                customLoadingDialog.dismissLoading();
                                                Toast.makeText(LoginScreen.this, "mot de passe incorrecte", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                } else {
                                    customLoadingDialog.dismissLoading();
                                    Toast.makeText(LoginScreen.this, "utilisateur introuvable", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                customLoadingDialog.dismissLoading();
                                Toast.makeText(LoginScreen.this, "Erreur: " + task.getException(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            }
        });



    }


    public void saveToPrefs(long accountType,String id ){
        SharedPreferences.Editor editor = getSharedPreferences("AUTH", MODE_PRIVATE).edit();
        editor.putString("auth_id", id);
        editor.putInt("account_type", (int) accountType);
        editor.putBoolean("is_login", true);
        editor.apply();
    }


    public void onSignupClick(View view) {
        Intent signup = new Intent(this, SignupScreen.class);
        startActivity(signup);
        finish();
    }

}