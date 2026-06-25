package com.example.afyagro.ui.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afyagro.R;
import com.example.afyagro.adapters.DeliveryRecycleAdapter;
import com.example.afyagro.models.Delivery;
import com.example.afyagro.ui.first.LoginScreen;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Écran réservé aux administrateurs (account_type == 3).
 *
 * Il liste l'ensemble des livraisons et permet de confirmer la date / l'heure
 * de livraison et de signaler un retard. Ces actions ne sont accessibles
 * qu'à l'administrateur.
 */
public class AdminActivity extends AppCompatActivity
        implements DeliveryRecycleAdapter.OnEmptyList {

    private DeliveryRecycleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        RecyclerView recyclerView = findViewById(R.id.adminDeliveryRecycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Query query = FirebaseFirestore.getInstance()
                .collection("deliveries")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Delivery> options =
                new FirestoreRecyclerOptions.Builder<Delivery>()
                        .setQuery(query, snapshot -> {
                            Delivery delivery = snapshot.toObject(Delivery.class);
                            if (delivery == null) {
                                delivery = new Delivery();
                            }
                            delivery.setUid(snapshot.getId());
                            return delivery;
                        })
                        .build();

        adapter = new DeliveryRecycleAdapter(options, this, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onEmpty(boolean hasItems) {
        TextView empty = findViewById(R.id.adminEmpty);
        empty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    public void logout(View view) {
        SharedPreferences.Editor editor = getSharedPreferences("AUTH", MODE_PRIVATE).edit();
        editor.remove("auth_id");
        editor.remove("account_type");
        editor.putBoolean("is_login", false);
        editor.apply();
        Intent intent = new Intent(this, LoginScreen.class);
        startActivity(intent);
        finish();
    }
}
