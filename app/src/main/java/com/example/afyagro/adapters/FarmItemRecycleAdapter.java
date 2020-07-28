package com.example.afyagro.adapters;

import android.annotation.SuppressLint;
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
import com.squareup.picasso.Picasso;

public class FarmItemRecycleAdapter extends FirestoreRecyclerAdapter<FarmItem, FarmItemRecycleAdapter.FarmItemViewHolder> {



    private OnListItemClick onListItemClick;



    public FarmItemRecycleAdapter(@NonNull FirestoreRecyclerOptions<FarmItem> options, OnListItemClick onListItemClick) {
        super(options);
        this.onListItemClick = onListItemClick;

    }

    @NonNull
    @Override
    public FarmItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.farm_home_item, parent, false);
        return new FarmItemViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull FarmItemViewHolder holder, int position, @NonNull FarmItem model) {


        holder.itemName.setText(model.getName());
        holder.itemPrice.setText(model.getPrice());

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
        private TextView itemPrice;
        private ImageView itemImage;
        public FarmItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.itemImage = itemView.findViewById(R.id.itemImage);
            this.itemName = itemView.findViewById(R.id.itemName);
            this.itemPrice = itemView.findViewById(R.id.itemPrice);

        }

        @Override
        public void onClick(View v) {
            onListItemClick.onItemClick(getItem(getAdapterPosition()), getAdapterPosition());
        }
    }

    public interface OnListItemClick{
        void onItemClick(FarmItem item, int position) ;
    }

}
