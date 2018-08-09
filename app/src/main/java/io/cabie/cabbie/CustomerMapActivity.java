package io.cabie.cabbie;

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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback ,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LatLng customerPickUpLocation;
    LocationRequest mLocationRequest;
    private final int MY_PERMISSION_REQUEST_CODE = 18888;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser mUser;
    String customerID="";
    DatabaseReference customerLocationReference;
    DatabaseReference driversAvailableReference;
    DatabaseReference driversInfoRef;
    DatabaseReference driversLocationRef;

    Marker mUserMarker;

    FancyButton callDriverButton;

    int radius=1;

    boolean driverFound=false;
    String driverFoundId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        callDriverButton = findViewById(R.id.buttonCallCab);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mUser=mAuth.getCurrentUser();
        customerID=mUser.getUid();
        customerLocationReference = FirebaseDatabase.getInstance().getReference().child("CustomerRequests");
        driversAvailableReference = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
        driversInfoRef = FirebaseDatabase.getInstance().getReference().child("Users/Drivers");
        driversLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversWorking");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(CustomerMapActivity.this, permissions, MY_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(CustomerMapActivity.this, permissions, MY_PERMISSION_REQUEST_CODE);
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

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


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

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void callCab(View view) {
        GeoFire geoFire=new GeoFire(customerLocationReference);
        geoFire.setLocation(customerID,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
        customerPickUpLocation=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        if (mUserMarker!=null && mUserMarker.isVisible())
        {
            mUserMarker.remove();
        }
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pick Me Up Here")
                .snippet("I am Here")
                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        getClosetDriver();
    }

    public void getClosetDriver() {
       GeoFire geoFire=new GeoFire(driversAvailableReference);
       GeoQuery geoQuery =geoFire.queryAtLocation(new GeoLocation(customerPickUpLocation.latitude,customerPickUpLocation.longitude),radius);
       geoQuery.removeAllListeners();
       geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
          @Override
          public void onKeyEntered(String key, GeoLocation location) {
              if (!driverFound){
                  driverFoundId =key;
                  driverFound=true;

                  HashMap map=new HashMap<>();
                  map.put("CustomerRideID", customerID);
                  driversInfoRef.child(driverFoundId).updateChildren(map);
                  callDriverButton.setText("Locating Driver ....");
                  getDriversLocation();

              }

          }

          @Override
          public void onKeyExited(String key) {

          }

          @Override
          public void onKeyMoved(String key, GeoLocation location) {

          }

          @Override
          public void onGeoQueryReady() {
           if (!driverFound){
               radius++;
               getClosetDriver();
           }
          }

          @Override
          public void onGeoQueryError(DatabaseError error) {

          }
      });

    }

    private void getDriversLocation() {
      driversLocationRef.child(driverFoundId).child("l").addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
              if (dataSnapshot.exists()){
                  List<Object> driverLocation = (List<Object>) dataSnapshot.getValue();
                  double lat=0;
                  double lon=0;
                  if (driverLocation.get(0)!=null) {
                      lat = Double.parseDouble(driverLocation.get(0).toString());
                      lon = Double.parseDouble(driverLocation.get(1).toString());
                  }
                  callDriverButton.setText("Driver Located");



              }
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });

    }
}


/*SELECT zip, primary_city,
       latitude, longitude, distance
  FROM (
 SELECT z.zip,
        z.primary_city,
        z.latitude, z.longitude,
        p.radius,
        p.distance_unit
                 * DEGREES(ACOS(COS(RADIANS(p.latpoint))
                 * COS(RADIANS(z.latitude))
                 * COS(RADIANS(p.longpoint - z.longitude))
                 + SIN(RADIANS(p.latpoint))
                 * SIN(RADIANS(z.latitude)))) AS distance
  FROM zip AS z
  JOIN (
        SELECT  42.81  AS latpoint,  -70.81 AS longpoint,
                50.0 AS radius,      111.045 AS distance_unit
                ) AS p ON 1=1
                WHERE z.latitude
                BETWEEN p.latpoint  - (p.radius / p.distance_unit)
                AND p.latpoint  + (p.radius / p.distance_unit)
                AND z.longitude
                BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))
                AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))
                ) AS d
                WHERE distance <= radius
                ORDER BY distance
                LIMIT 15*/