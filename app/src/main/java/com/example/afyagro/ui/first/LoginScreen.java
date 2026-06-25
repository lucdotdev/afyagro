package com.example.afyagro.ui.first;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.afyagro.ui.admin.AdminActivity;
import com.example.afyagro.ui.home.MainActivity;
import com.example.afyagro.ui.home_.MainPharmerActivity;
import com.example.afyagro.widgets.CustomLoadingDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginScreen extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

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
        Button googleSignInBtn = findViewById(R.id.googleSignInBtn);

        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();

        // default_web_client_id est généré par le plugin google-services à partir
        // du client web présent dans google-services.json. Il doit donc exister
        // pour que la connexion Google fonctionne (oubli fréquent à l'intégration).
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        this.googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(googleSignInClient.getSignInIntent(), RC_GOOGLE_SIGN_IN);
            }
        });


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
                                                } else if ((long) t.get("account_type") == 3) {
                                                    Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            customLoadingDialog.dismissLoading();
                                                            Intent intent = new Intent(LoginScreen.this, AdminActivity.class);
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

    public void onForgotPasswordClick(View view) {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Erreur Google: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        customLoadingDialog.startLoading();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            handleGoogleUser(account);
                        } else {
                            customLoadingDialog.dismissLoading();
                            Toast.makeText(LoginScreen.this,
                                    "Erreur: " + Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void handleGoogleUser(final GoogleSignInAccount account) {
        final String email = account.getEmail();
        firebaseFirestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null
                                && !task.getResult().getDocuments().isEmpty()) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            long type = doc.get("account_type") == null ? 2 : (long) doc.get("account_type");
                            saveToPrefs(type, doc.getId());
                            customLoadingDialog.dismissLoading();
                            routeByAccountType(type);
                        } else {
                            customLoadingDialog.dismissLoading();
                            promptAccountTypeAndCreate(account);
                        }
                    }
                });
    }

    private void promptAccountTypeAndCreate(final GoogleSignInAccount account) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Type de compte")
                .setMessage("Comment souhaitez-vous utiliser Afyagro ?")
                .setCancelable(false)
                .setPositiveButton("Vendeur", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        createGoogleUser(account, 1);
                    }
                })
                .setNegativeButton("Acheteur", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        createGoogleUser(account, 2);
                    }
                })
                .show();
    }

    private void createGoogleUser(GoogleSignInAccount account, final int accountType) {
        customLoadingDialog.startLoading();
        Map<String, Object> entry = new HashMap<>();
        entry.put("phone", "");
        entry.put("name", account.getDisplayName() == null ? "" : account.getDisplayName());
        entry.put("email", account.getEmail() == null ? "" : account.getEmail());
        entry.put("password", "");
        entry.put("account_type", accountType);
        entry.put("photoPath", account.getPhotoUrl() == null ? "" : account.getPhotoUrl().toString());

        firebaseFirestore.collection("users").add(entry)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        customLoadingDialog.dismissLoading();
                        if (task.isSuccessful() && task.getResult() != null) {
                            saveToPrefs(accountType, task.getResult().getId());
                            routeByAccountType(accountType);
                        } else {
                            Toast.makeText(LoginScreen.this,
                                    "Erreur lors de la création du compte", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void routeByAccountType(long type) {
        Intent intent;
        if (type == 1) {
            intent = new Intent(this, MainPharmerActivity.class);
        } else if (type == 3) {
            intent = new Intent(this, AdminActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }

}