package com.example.afyagro.ui.home_;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.afyagro.R;
import com.example.afyagro.adapters.FarmerProductRecycleAdapter;
import com.example.afyagro.adapters.FindItemRecycleAdapter;
import com.example.afyagro.models.FarmItem;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class MyProduct extends AppCompatActivity implements FarmerProductRecycleAdapter.OnListItemClick , FarmerProductRecycleAdapter.OnEmptyList{





    private FirestoreRecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_product);



        RecyclerView  myProductRecycleView = findViewById(R.id.myProductRecycle);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();



        SharedPreferences prefs = this.getSharedPreferences("AUTH", MODE_PRIVATE);

        Query query = firebaseFirestore.collection("items")
                .whereEqualTo("publisherId", prefs.getString("auth_id", ""));
        FirestoreRecyclerOptions<FarmItem> options = new FirestoreRecyclerOptions.Builder<FarmItem>()
                .setQuery(query, new SnapshotParser<FarmItem>() {
                    @NonNull
                    @Override
                    public FarmItem parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        FarmItem item = snapshot.toObject(FarmItem.class);
                        assert item != null;
                        item.setUid(snapshot.getId());
                        return item;
                    }
                })
                .build();

        this.adapter = new FarmerProductRecycleAdapter(options, this, (Context) this, (FarmerProductRecycleAdapter.OnEmptyList) this);
        adapter.startListening();
        myProductRecycleView.setLayoutManager(new LinearLayoutManager(this));
        myProductRecycleView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(FarmItem item, int position) {

    }

    @Override
    public void onEmpty(boolean k) {
        TextView notItem = findViewById(R.id.emptyList);
        if(k){
            notItem.setVisibility(View.GONE);
        }else {
            notItem.setVisibility(View.VISIBLE);
        }
    }
}