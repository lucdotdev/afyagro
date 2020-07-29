package com.example.afyagro.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afyagro.R;
import com.example.afyagro.models.FarmItem;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class FarmerProductRecycleAdapter extends FirestoreRecyclerAdapter<FarmItem, FarmerProductRecycleAdapter.FarmItemViewHolder> {



    private OnListItemClick onListItemClick;
    private OnEmptyList onEmptyList;
    private Context i;



    public FarmerProductRecycleAdapter(@NonNull FirestoreRecyclerOptions<FarmItem> options, OnListItemClick onListItemClick, Context k, FarmerProductRecycleAdapter.OnEmptyList onEmptyList) {
        super(options);
        this.onListItemClick = onListItemClick;
        this.onEmptyList = onEmptyList;
        this.i = k;

    }

    @NonNull
    @Override
    public FarmItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_farmer_item, parent, false);
        return new FarmItemViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if(getItemCount()==0){
            onEmptyList.onEmpty(false);
        } else {
            onEmptyList.onEmpty(true);
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull FarmItemViewHolder holder, final int position, @NonNull final FarmItem model) {


        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              new AlertDialog.Builder(i)
                      .setTitle("Suppression")
                      .setMessage("Etes vous sur de supprimer l'elements ?")
                      .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              firebaseFirestore.collection("items").document(model.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                  @Override
                                  public void onSuccess(Void aVoid) {
                                      StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(model.getPhotoPath());

                                      storageReference.delete();
                                      notifyItemRemoved(position);
                                  }
                              }).addOnFailureListener(new OnFailureListener() {
                                  @Override
                                  public void onFailure(@NonNull Exception e) {

                                  }
                              });

                          }
                      })
                      .setNegativeButton(R.string.no, null)
                      .show();
            }
        });
        holder.itemName.setText(model.getName());
        if(!model.getPhotoPath().isEmpty()){
            Picasso.get()
                    .load(model.getPhotoPath())
                    .resize(400, 200)
                    .centerCrop()
                    .into(holder.itemImage);
        }

    }


    public  class FarmItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView itemName;
        private ImageView itemImage;
        private ImageView remove;
        public FarmItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.itemImage = itemView.findViewById(R.id.itemImage);
            this.itemName = itemView.findViewById(R.id.itemName);
            this.remove = itemView.findViewById(R.id.remove);

        }

        @Override
        public void onClick(View v) {
            onListItemClick.onItemClick(getItem(getAdapterPosition()), getAdapterPosition());
        }
    }

    public interface OnListItemClick{
        void onItemClick(FarmItem item, int position) ;
    }

    public interface OnEmptyList{
        void onEmpty(boolean k);
    }

}