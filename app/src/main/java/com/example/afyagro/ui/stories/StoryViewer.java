package com.example.afyagro.ui.stories;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.afyagro.R;
import com.example.afyagro.models.Story;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Plays a publisher's stories full screen with auto-advancing segmented
 * progress bars, exactly like Instagram and TikTok. Tap the right side to go
 * to the next story and the left side to go back.
 */
public class StoryViewer extends AppCompatActivity {

    /** How long each story stays on screen before auto-advancing. */
    private static final long STORY_DURATION_MS = 5000L;

    private final List<Story> stories = new ArrayList<>();
    private final List<ProgressBar> segments = new ArrayList<>();

    private ImageView storyImage;
    private TextView caption;
    private TextView viewerName;
    private CircleImageView viewerAvatar;
    private LinearLayout progressContainer;

    private int currentIndex = 0;
    private ValueAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_viewer);

        storyImage = findViewById(R.id.storyImage);
        caption = findViewById(R.id.storyCaption);
        viewerName = findViewById(R.id.storyViewerName);
        viewerAvatar = findViewById(R.id.storyViewerAvatar);
        progressContainer = findViewById(R.id.storyProgressContainer);

        findViewById(R.id.storyClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.storyNextZone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });
        findViewById(R.id.storyPrevZone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previous();
            }
        });

        String publisherId = getIntent().getStringExtra("publisherId");
        if (publisherId == null || publisherId.isEmpty()) {
            finish();
            return;
        }
        loadStories(publisherId);
    }

    private void loadStories(String publisherId) {
        FirebaseFirestore.getInstance().collection("stories")
                .whereEqualTo("publisherId", publisherId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        Story story = doc.toObject(Story.class);
                        // Only show stories that have not expired yet.
                        if (story != null && story.isActive()) {
                            stories.add(story);
                        }
                    }
                }
                // Oldest first, like Instagram/TikTok. Sorted on the client so
                // no Firestore composite index is required.
                Collections.sort(stories, new Comparator<Story>() {
                    @Override
                    public int compare(Story a, Story b) {
                        return Long.compare(a.getTimestamp(), b.getTimestamp());
                    }
                });
                if (stories.isEmpty()) {
                    Toast.makeText(StoryViewer.this, R.string.story_no_stories, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                buildSegments();
                showStory(0);
            }
        });
    }

    private void buildSegments() {
        progressContainer.removeAllViews();
        segments.clear();
        for (int i = 0; i < stories.size(); i++) {
            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(100);
            bar.setProgress(0);
            bar.setProgressDrawable(ResourcesCompat.getDrawable(
                    getResources(), R.drawable.story_progress_segment, getTheme()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, dp(3), 1f);
            params.setMargins(dp(2), 0, dp(2), 0);
            bar.setLayoutParams(params);
            progressContainer.addView(bar);
            segments.add(bar);
        }
    }

    private void showStory(int index) {
        if (index < 0 || index >= stories.size()) {
            finish();
            return;
        }
        currentIndex = index;
        Story story = stories.get(index);

        if (story.getPhotoPath() != null && !story.getPhotoPath().isEmpty()) {
            Picasso.get().load(story.getPhotoPath()).fit().centerInside().into(storyImage);
        }

        viewerName.setText(story.getPublisherName());
        if (story.getCaption() == null || story.getCaption().isEmpty()) {
            caption.setVisibility(View.GONE);
        } else {
            caption.setVisibility(View.VISIBLE);
            caption.setText(story.getCaption());
        }

        String avatar = story.getPublisherPhoto();
        if (avatar != null && !avatar.isEmpty()) {
            Picasso.get().load(avatar).placeholder(R.drawable.ic_profile).into(viewerAvatar);
        } else {
            viewerAvatar.setImageResource(R.drawable.ic_profile);
        }

        // Fill earlier segments, reset later ones.
        for (int i = 0; i < segments.size(); i++) {
            segments.get(i).setProgress(i < index ? 100 : 0);
        }

        startSegmentAnimation();
    }

    private void startSegmentAnimation() {
        cancelAnimation();
        final ProgressBar bar = segments.get(currentIndex);
        animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(STORY_DURATION_MS);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                bar.setProgress((int) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Guard against the cancel() call which also triggers onAnimationEnd.
                if (bar.getProgress() >= 100) {
                    next();
                }
            }
        });
        animator.start();
    }

    private void next() {
        cancelAnimation();
        if (currentIndex + 1 < stories.size()) {
            showStory(currentIndex + 1);
        } else {
            finish();
        }
    }

    private void previous() {
        cancelAnimation();
        if (currentIndex - 1 >= 0) {
            showStory(currentIndex - 1);
        } else {
            showStory(currentIndex);
        }
    }

    private void cancelAnimation() {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelAnimation();
    }

    @Override
    protected void onDestroy() {
        cancelAnimation();
        ViewGroup container = progressContainer;
        if (container != null) {
            container.removeAllViews();
        }
        super.onDestroy();
    }
}
