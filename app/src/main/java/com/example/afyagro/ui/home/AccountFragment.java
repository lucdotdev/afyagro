package com.example.afyagro.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.afyagro.R;
import com.example.afyagro.ui.first.LoginScreen;
import com.example.afyagro.ui.first.SignupScreen;
import com.example.afyagro.ui.home_.MainPharmerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


public class AccountFragment extends Fragment {



    public AccountFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_account, container, false);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        final TextView name = view.findViewById(R.id.accountName);

        Button logout = view.findViewById(R.id.logout_btn);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = Objects.requireNonNull(getActivity()).getSharedPreferences("AUTH", MODE_PRIVATE).edit();
                editor.remove("auth_id");
                editor.remove("account_type");
                editor.putBoolean("is_login", false);
                editor.apply();
                Intent intent = new Intent(getContext(), LoginScreen.class);
                startActivity(intent);
                 getActivity().finish();

            }
        });


        SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences("AUTH", MODE_PRIVATE);
        firebaseFirestore.collection("users").document(Objects.requireNonNull(prefs.getString("auth_id", ""))).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                name.setText("Nom: " +Objects.requireNonNull(task.getResult()).getString("name"));

            }
        });
        return view;
    }

    @Override
    public void onStart() {

        super.onStart();
    }
}