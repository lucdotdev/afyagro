package com.example.afyagro.ui.home.details;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.afyagro.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

public class FarmItemDetails extends AppCompatActivity {


    private Bundle extras;
    private FirebaseFirestore kStore;
    private String[] number = {""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_item_details);

        this.extras = getIntent().getExtras();
        this.kStore = FirebaseFirestore.getInstance();



        kStore.collection("users").document(Objects.requireNonNull(extras.getString("id"))).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
               number[0] = task.getResult().getString("phone");

            }
        });


        TextView itemName = findViewById(R.id.itemDetailsName);
        TextView itemPrice = findViewById(R.id.itemDetailsPrice);
        TextView itemDescr = findViewById(R.id.itemDetailsDescr);
        TextView itemVend = findViewById(R.id.itemDetailsVendor);
        ImageView itemImage = findViewById(R.id.itemDetailsImage);


        assert extras != null;
        Picasso.get()
                .load(extras.getString("image"))
                .fit().centerCrop()
                .into(itemImage);
        itemName.setText(extras.getString("name"));
        itemPrice.setText(extras.getString("price"));
        itemDescr.setText(extras.getString("desc"));
        itemVend.setText(extras.getString("vendor"));

    }

    public void onContactClick(View view) throws UnsupportedEncodingException {
        String message = "Bonjour *" + extras.getString("vendor") + "* je vous contact pour le produit  "+  extras.getString("name")+ " ...";
        String url= "https://api.whatsapp.com/send?phone=" + "+243" + number[0] +"&text=" + URLEncoder.encode(message, "UTF-8");


        try {
            PackageManager pm = this.getPackageManager();
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}