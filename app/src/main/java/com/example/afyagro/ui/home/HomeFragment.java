package com.example.afyagro.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.afyagro.R;
import com.example.afyagro.adapters.FarmItemRecycleAdapter;
import com.example.afyagro.models.FarmItem;
import com.example.afyagro.ui.home.details.FarmItemDetails;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class HomeFragment extends Fragment implements FarmItemRecycleAdapter.OnListItemClick {


    public HomeFragment() {

    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Query eggs_query = firebaseFirestore.collection("items").whereEqualTo("type", "1");
        Query cows_qurey = firebaseFirestore.collection("items").whereEqualTo("type", "2");

        RecyclerView home_eggs = view.findViewById(R.id.home_eggs);
        RecyclerView home_cows= view.findViewById(R.id.home_cows);



        FirestoreRecyclerOptions<FarmItem> optionsCows = new FirestoreRecyclerOptions.Builder<FarmItem>()
                .setQuery(cows_qurey, FarmItem.class)
                .build();
        FirestoreRecyclerAdapter adapterCows = new FarmItemRecycleAdapter(optionsCows, this);


        FirestoreRecyclerOptions<FarmItem> optionsEggs = new FirestoreRecyclerOptions.Builder<FarmItem>()
                .setQuery(eggs_query, FarmItem.class)
                .build();
        FirestoreRecyclerAdapter adapterEggs = new FarmItemRecycleAdapter(optionsEggs, this);

        adapterEggs.startListening();
        adapterCows.startListening();


        home_cows.setAdapter(adapterCows);
        home_cows.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));


        home_eggs.setAdapter(adapterEggs);
        home_eggs.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));



        return view ;
    }

    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onItemClick(FarmItem item, int position) {

        Intent itemDetails = new Intent(getActivity(), FarmItemDetails.class);

        itemDetails.putExtra("name", item.getName());
        itemDetails.putExtra("image", item.getPhotoPath());
        itemDetails.putExtra("vendor", item.getPublisherName());
        itemDetails.putExtra("desc", item.getDescription());
        itemDetails.putExtra("price", item.getPrice());
        itemDetails.putExtra("id", item.getPublisherId());

        startActivity(itemDetails);

    }
}