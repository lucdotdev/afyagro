package com.example.afyagro.ui.home.details;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.afyagro.R;
import com.example.afyagro.models.Delivery;
import com.example.afyagro.ui.map.DeliveryMapActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
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

    public void onTrackDeliveryClick(View view) {
        Intent intent = new Intent(this, DeliveryMapActivity.class);
        intent.putExtra("dest_label", extras.getString("vendor"));
        startActivity(intent);
    }

    public void onOrderClick(View view) {
        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        final String buyerId = prefs.getString("auth_id", "");
        if (buyerId.isEmpty()) {
            Toast.makeText(this, R.string.order_failed, Toast.LENGTH_LONG).show();
            return;
        }
        // Récupère le nom de l'acheteur puis enregistre la commande.
        kStore.collection("users").document(buyerId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String buyerName = "";
                        if (task.isSuccessful() && task.getResult() != null) {
                            buyerName = task.getResult().getString("name");
                        }
                        createOrder(buyerId, buyerName == null ? "" : buyerName);
                    }
                });
    }

    private void createOrder(String buyerId, String buyerName) {
        Map<String, Object> order = new HashMap<>();
        order.put("itemId", extras.getString("item_id", ""));
        order.put("itemName", extras.getString("name", ""));
        order.put("buyerId", buyerId);
        order.put("buyerName", buyerName);
        order.put("sellerId", extras.getString("id", ""));
        order.put("sellerName", extras.getString("vendor", ""));
        order.put("status", Delivery.STATUS_PENDING);
        order.put("deliveryDate", "");
        order.put("deliveryTime", "");
        order.put("delayNote", "");
        order.put("createdAt", System.currentTimeMillis());

        kStore.collection("deliveries").add(order)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(FarmItemDetails.this, R.string.order_placed, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(FarmItemDetails.this, R.string.order_failed, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}