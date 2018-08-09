package io.cabie.cabbie;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverMapActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private final int MY_PERMISSION_REQUEST_CODE = 18888;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser mUser;

    Boolean driverLogoutStatus=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        checkPermission();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mUser=mAuth.getCurrentUser();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(DriverMapActivity.this, permissions, MY_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    public void checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(DriverMapActivity.this, permissions, MY_PERMISSION_REQUEST_CODE);
            return;
        }
    }

    /**
     * Setup connection parameters
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(DriverMapActivity.this, permissions, MY_PERMISSION_REQUEST_CODE);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Get the current location and push the results to drivers available node in firebase database
     * @param location
     */

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));

        String userID= mUser.getUid();

        DatabaseReference driversAvailableRef = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");

        GeoFire geoFire=new GeoFire(driversAvailableRef);

        geoFire.setLocation(userID, new GeoLocation(latLng.latitude,latLng.longitude));


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (true) {

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permissions required", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    /**
     * Build Google API client
     */

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Ensures that drivers availability is removed when a driver navigates away from this activity
     */

    @Override
    protected void onStop() {
        super.onStop();
        if (!driverLogoutStatus){
            disconnectDriver();
        }
    }


    private void disconnectDriver() {
        String userID= mUser.getUid();
        DatabaseReference driversAvailableRef = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
        GeoFire geoFire=new GeoFire(driversAvailableRef);
        geoFire.removeLocation(userID);
    }

    public void settings(View view) {


    }

    public void signOut(View view) {
        driverLogoutStatus=true;
        disconnectDriver();
        mAuth.signOut();
        Intent x=new Intent(this, WelcomeActivity.class);
        x.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(x);
        finish();
    }
}
