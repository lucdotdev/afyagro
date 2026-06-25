package com.example.afyagro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afyagro.R;
import com.example.afyagro.models.Story;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Shows the circular story bubbles in the horizontal stories bar on the home
 * screen, just like the rings on Instagram or TikTok.
 */
public class StoryRecycleAdapter extends FirestoreRecyclerAdapter<Story, StoryRecycleAdapter.StoryViewHolder> {

    private final OnStoryClick onStoryClick;

    public StoryRecycleAdapter(@NonNull FirestoreRecyclerOptions<Story> options, OnStoryClick onStoryClick) {
        super(options);
        this.onStoryClick = onStoryClick;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_story_item, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull StoryViewHolder holder, int position, @NonNull Story model) {
        holder.publisher.setText(model.getPublisherName());

        String avatar = model.getPublisherPhoto();
        if (avatar == null || avatar.isEmpty()) {
            // Fall back to the story photo when the publisher has no avatar.
            avatar = model.getPhotoPath();
        }
        if (avatar != null && !avatar.isEmpty()) {
            Picasso.get()
                    .load(avatar)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.avatar);
        }
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final CircleImageView avatar;
        private final TextView publisher;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.avatar = itemView.findViewById(R.id.storyAvatar);
            this.publisher = itemView.findViewById(R.id.storyPublisher);
        }

        @Override
        public void onClick(View v) {
            onStoryClick.onStoryClick(getItem(getAdapterPosition()), getAdapterPosition());
        }
    }

    public interface OnStoryClick {
        void onStoryClick(Story story, int position);
    }
}
