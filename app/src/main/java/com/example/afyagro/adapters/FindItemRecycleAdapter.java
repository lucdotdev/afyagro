package com.example.afyagro.adapters;


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

public class FindItemRecycleAdapter extends FirestoreRecyclerAdapter<FarmItem, FindItemRecycleAdapter.ItemViewHolder> {



    private OnListItemClick onListItemClick;

    private OnEmptyList onEmptyList;

    public FindItemRecycleAdapter(@NonNull FirestoreRecyclerOptions<FarmItem> options, OnListItemClick onListItemClick, OnEmptyList onEmptyList) {
        super(options);
        this.onListItemClick = onListItemClick;
        this.onEmptyList = onEmptyList;

    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_search_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull FarmItem model) {

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

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if(getItemCount()==0){
            onEmptyList.onEmpty(false);
        } else {
            onEmptyList.onEmpty(true);
        }
    }

    public  class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private TextView itemName;
        private TextView itemPrice;
        private ImageView itemImage;

        public ItemViewHolder(@NonNull View itemView) {
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
        void onItemClick(FarmItem drug, int position) ;
    }
    public interface OnEmptyList{
        void onEmpty(boolean k);
    }
}
