package com.example.afyagro.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.afyagro.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * Carte de suivi de livraison basée sur OpenStreetMap (osmdroid).
 * Aucune clé API n'est requise.
 *
 * Extras optionnels :
 *  - "dest_lat" / "dest_lng" (double) : position de la destination (personne à livrer).
 *  - "dest_label" (String) : libellé du marqueur de destination.
 *
 * La position de l'appareil (le livreur ou la personne à livrer) n'est affichée
 * que pendant la session de livraison, après accord de la permission de localisation.
 */
public class DeliveryMapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 101;

    // Centre par défaut : Kinshasa (RDC), cohérent avec l'indicatif +243 utilisé dans l'app.
    private static final double DEFAULT_LAT = -4.4419;
    private static final double DEFAULT_LNG = 15.2663;
    private static final double DEFAULT_ZOOM_LEVEL = 15.0;

    private MapView map;
    private MyLocationNewOverlay myLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // osmdroid doit être configuré avant l'inflation de la vue carte.
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_delivery_map);

        map = findViewById(R.id.deliveryMap);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(DEFAULT_ZOOM_LEVEL);

        GeoPoint center = new GeoPoint(DEFAULT_LAT, DEFAULT_LNG);

        // Marqueur de destination (personne à livrer), si fourni.
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("dest_lat") && extras.containsKey("dest_lng")) {
            GeoPoint destination = new GeoPoint(
                    extras.getDouble("dest_lat"),
                    extras.getDouble("dest_lng"));
            Marker destinationMarker = new Marker(map);
            destinationMarker.setPosition(destination);
            destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            destinationMarker.setTitle(
                    extras.getString("dest_label", getString(R.string.map_destination)));
            map.getOverlays().add(destinationMarker);
            center = destination;
        }

        map.getController().setCenter(center);

        enableLocationIfPermitted();
    }

    private void enableLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            showMyLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void showMyLocation() {
        if (myLocationOverlay == null) {
            myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
            map.getOverlays().add(myLocationOverlay);
        }
        myLocationOverlay.enableMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMyLocation();
            } else {
                Toast.makeText(this, R.string.map_permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
        // Arrête le suivi GPS dès que la carte n'est plus visible : la localisation
        // n'est active que pendant la consultation de la livraison.
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }

    public void onMapReturnButtonClicked(View view) {
        finish();
    }
}
