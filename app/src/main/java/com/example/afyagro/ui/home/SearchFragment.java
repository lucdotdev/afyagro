package com.example.afyagro.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.afyagro.R;
import com.example.afyagro.adapters.FarmItemRecycleAdapter;
import com.example.afyagro.adapters.FindItemRecycleAdapter;
import com.example.afyagro.models.FarmItem;
import com.example.afyagro.ui.home.details.FarmItemDetails;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class SearchFragment extends Fragment implements  FindItemRecycleAdapter.OnListItemClick, FindItemRecycleAdapter.OnEmptyList  {


    private RecyclerView findRecycleView;
    private FirestoreRecyclerAdapter adapter;

    public SearchFragment() {

    }
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        this.findRecycleView = view.findViewById(R.id.findRecycleView);
        SearchView searchView = view.findViewById(R.id.findSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });
        return view;
    }


    private void search(String txt){


        if(!txt.isEmpty()){

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            Query query = firebaseFirestore.collection("items").orderBy("searchName").startAt(txt.toLowerCase()).endAt(txt.toLowerCase()+"\uf8ff");
            FirestoreRecyclerOptions<FarmItem> options = new FirestoreRecyclerOptions.Builder<FarmItem>()
                    .setQuery(query, FarmItem.class)
                    .build();

            this.adapter = new FindItemRecycleAdapter(options, this, this);
            adapter.startListening();
            findRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
            findRecycleView.setAdapter(adapter);


        }else {


            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            Query query = firebaseFirestore.collection("items");
            FirestoreRecyclerOptions<FarmItem> options = new FirestoreRecyclerOptions.Builder<FarmItem>()
                    .setQuery(query, FarmItem.class)
                    .build();

            this.adapter = new FindItemRecycleAdapter(options, this, this);
            adapter.startListening();
            findRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
            findRecycleView.setAdapter(adapter);
        }
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

    @Override
    public void onEmpty(boolean k) {

    }
}