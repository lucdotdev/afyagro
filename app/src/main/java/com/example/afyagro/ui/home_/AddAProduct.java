package com.example.afyagro.ui.home_;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.afyagro.R;
import com.example.afyagro.helpers.StringOperation;
import com.example.afyagro.ui.first.LoginScreen;
import com.example.afyagro.ui.home.MainActivity;
import com.example.afyagro.widgets.CustomLoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AddAProduct extends AppCompatActivity {

    private EditText myproductName;
    private EditText myproductPrice;
    private EditText myproductDesc;
    private String myProductType = "";
    private final String[] name = {""};
    private String photoPath = "";

    private static final StringOperation stringOperation = new StringOperation();
    private final CustomLoadingDialog customLoadingDialog = new CustomLoadingDialog(this);
    private  SharedPreferences prefs;
    private  FirebaseFirestore firebaseFirestore;



    private Button btnChoose, btnUpload;
    private ImageView imageView;

    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;

    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_product);


        this.prefs = this.getSharedPreferences("AUTH", MODE_PRIVATE);
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();



        firebaseFirestore.collection("users").document(Objects.requireNonNull(prefs.getString("auth_id", ""))).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                name[0] =  Objects.requireNonNull(task.getResult()).getString("name");
            }
        });
        this.myproductName = findViewById(R.id.myProductName);
        this.myproductPrice = findViewById(R.id.myProductPrice);
        this.myproductDesc = findViewById(R.id.myProductDescr);


        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        imageView = (ImageView) findViewById(R.id.imgView);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selectionner une image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    photoPath = uri.toString();
                                }
                            });
                            Toast.makeText(AddAProduct.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddAProduct.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
        else {
            Toast.makeText(AddAProduct.this, "Veuillez choisir une image", Toast.LENGTH_SHORT).show();
        }
    }


    public void onProductSubmit(View view) {

        if (stringOperation.isEmpty(myproductName) || stringOperation.isEmpty(myproductPrice) || stringOperation.isEmpty(myproductDesc) || myProductType == "") {
            Toast.makeText(AddAProduct.this, "Veuillez remplir tout les champs", Toast.LENGTH_LONG).show();
        } else {






            if(photoPath.equals("")) {

                Toast.makeText(AddAProduct.this, "Veuillez ajouter une image", Toast.LENGTH_LONG).show();

            } else {
                customLoadingDialog.startLoading();
                Map<String, Object> item = new HashMap<>();
                item.put("name", myproductName.getText().toString().trim());
                item.put("description", myproductDesc.getText().toString().trim());
                item.put("price", myproductPrice.getText().toString().trim());
                item.put("publisherName", name[0]);
                item.put("publisherId", prefs.getString("auth_id", ""));
                item.put("type", myProductType);
                item.put("photoPath", photoPath);


                firebaseFirestore.collection("items").add(item).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isComplete()){
                            customLoadingDialog.ok();
                            finish();
                        }
                        else {
                            customLoadingDialog.dismissLoading();
                            Toast.makeText(AddAProduct.this, "Echec", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

        }
    }
    public void onProductTypeSelection(View view){
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_animalr:
                if (checked) myProductType = "2";
                break;
            case R.id.radio_vegetables:
                if (checked)
                   myProductType = "1";
                break;
        }

    }

    public void onAddProductReturnButtonCLicked(View view) {
        finish();
    }
}
