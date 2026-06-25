package com.example.afyagro.ui.stories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.afyagro.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Lets a user publish a story: pick a photo, add an optional caption and share.
 * The photo is stored in Firebase Storage and a document is written to the
 * "stories" collection so it shows up in everyone's stories bar.
 */
public class AddStory extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 71;

    private ImageView imageView;
    private EditText captionInput;

    private Uri filePath;
    private final String[] publisherName = {""};
    private final String[] publisherPhoto = {""};

    private SharedPreferences prefs;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        this.prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.storageReference = FirebaseStorage.getInstance().getReference();

        imageView = findViewById(R.id.storyImgView);
        captionInput = findViewById(R.id.storyCaptionInput);
        Button btnChoose = findViewById(R.id.btnChooseStory);
        Button btnShare = findViewById(R.id.btnShareStory);

        firebaseFirestore.collection("users")
                .document(Objects.requireNonNull(prefs.getString("auth_id", "")))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot result = task.getResult();
                if (result != null) {
                    publisherName[0] = result.getString("name");
                    publisherPhoto[0] = result.getString("photoPath");
                }
            }
        });

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareStory();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.story_choose)), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void shareStory() {
        if (filePath == null) {
            Toast.makeText(this, R.string.story_pick_first, Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.story_uploading));
        progressDialog.show();

        final StorageReference ref = storageReference.child("stories/" + UUID.randomUUID().toString());
        ref.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                saveStory(uri.toString(), progressDialog);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddStory.this, R.string.story_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveStory(String photoPath, final ProgressDialog progressDialog) {
        Map<String, Object> story = new HashMap<>();
        story.put("publisherId", prefs.getString("auth_id", ""));
        story.put("publisherName", publisherName[0]);
        story.put("publisherPhoto", publisherPhoto[0]);
        story.put("photoPath", photoPath);
        story.put("caption", captionInput.getText().toString().trim());
        story.put("timestamp", System.currentTimeMillis());

        firebaseFirestore.collection("stories").add(story)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            finish();
                        } else {
                            Toast.makeText(AddStory.this, R.string.story_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void onAddStoryReturnButtonClicked(View view) {
        finish();
    }
}
