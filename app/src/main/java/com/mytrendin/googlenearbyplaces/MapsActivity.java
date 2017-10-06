package com.mytrendin.googlenearbyplaces;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    double latitude;
    double longitude;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private double PROXIMITY_RADIUS = 45;

    public static final String DontWantTAG = "Dontshow";
    public static final String LatlngNearBY = "restlatlng";


    private ObjectAnimator waveOneAnimator;
    private ObjectAnimator waveTwoAnimator;
    private ObjectAnimator waveThreeAnimator;
    private ObjectAnimator waveFourAnimator;
    private ObjectAnimator waveFiveAnimator;


    private TextView hangoutTvOne;
    private TextView hangoutTvTwo;
    private TextView hangoutTvThree;
    private TextView hangoutTvfour;
    private TextView hangoutTvfive;

    private int screenWidth;
    Switch simpleSwitch;
    public static final String TotalAmount = "totalamount";


    Button RestList;

    ArrayList<String> list = new ArrayList<String>();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);




        simpleSwitch = (Switch) findViewById(R.id.switch1);
        simpleSwitch.setEnabled(false);



        RestList = (Button)findViewById(R.id.restbutton);

        RestList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent( MapsActivity.this, DataRestDisplay.class));
            }
        });


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            finish();
        } else {

        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //********* hangout ************
        hangoutTvOne = (TextView) findViewById(R.id.hangoutTvOne);
        hangoutTvTwo = (TextView) findViewById(R.id.hangoutTvTwo);
        hangoutTvThree = (TextView) findViewById(R.id.hangoutTvThree);
        hangoutTvfour = (TextView) findViewById(R.id.hangoutTvFour);
        hangoutTvfive = (TextView) findViewById(R.id.hangoutTvFive);



        hangoutTvOne.setVisibility(View.GONE);
        hangoutTvTwo.setVisibility(View.GONE);
        hangoutTvThree.setVisibility(View.GONE);
        hangoutTvfour.setVisibility(View.GONE);
        hangoutTvfive.setVisibility(View.GONE);



        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;


        hangoutTvOne.setVisibility(View.VISIBLE);
        hangoutTvTwo.setVisibility(View.VISIBLE);
        hangoutTvThree.setVisibility(View.VISIBLE);
        hangoutTvfour.setVisibility(View.VISIBLE);
        hangoutTvfive.setVisibility(View.VISIBLE);


        waveAnimation();


    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }

    public void nearbyrest()
    {
        String search = "restaurant";
        String url = getUrl(latitude, longitude, search);
        Object[] DataTransfer = new Object[2];
        DataTransfer[0] = mMap;
        DataTransfer[1] = url;
        GetNearbyBanksData getNearbyBanksData = new GetNearbyBanksData(MapsActivity.this);
        getNearbyBanksData.execute(DataTransfer);
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0");
        return (googlePlacesUrl.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        latitude = location.getLatitude();
        longitude = location.getLongitude();


        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
//        markerOptions.title("You are Here!");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//        mCurrLocationMarker = mMap.addMarker(markerOptions);

//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
      //  Toast.makeText(MapsActivity.this, "Your Current Location", Toast.LENGTH_LONG).show();

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 6s
                SharedPreferences prefs = getSharedPreferences(DontWantTAG, MODE_PRIVATE);
                String name = prefs.getString("Alertdisplay", "No name defined");//"No name defined" is the default value.

                SharedPreferences prefs1 = getSharedPreferences(LatlngNearBY, MODE_PRIVATE);
                String lalng_name1 = prefs1.getString("Latlnfg", "No name defined");//"No name defined" is the default value.


                if (name.equals("DONT"))
                {
                    String[] latlngArray = lalng_name1.split("-");

                    String latnear1= latlngArray[0];
                    String lngnear1 = latlngArray[1];

                    Location locationA = new Location("point A");
                    locationA.setLatitude(latitude);
                    locationA.setLongitude(longitude);

                    Location locationB = new Location("point B");
                    locationB.setLatitude(Double.parseDouble(latnear1));
                    locationB.setLongitude(Double.parseDouble(lngnear1));

                    float distance = locationA.distanceTo(locationB);
                    int distchange = (int) distance;

                    if (distchange>50)
                    {

                        hangoutTvOne.setVisibility(View.GONE);
                        hangoutTvTwo.setVisibility(View.GONE);
                        hangoutTvThree.setVisibility(View.GONE);
                        hangoutTvfour.setVisibility(View.GONE);
                        hangoutTvfive.setVisibility(View.GONE);
                        nearbyrest();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Do something after 8s
                                listdisplay();
                            }
                        }, 6000);
                    }
                    else return;

                }
                else
                {

                    hangoutTvOne.setVisibility(View.GONE);
                    hangoutTvTwo.setVisibility(View.GONE);
                    hangoutTvThree.setVisibility(View.GONE);
                    hangoutTvfour.setVisibility(View.GONE);
                    hangoutTvfive.setVisibility(View.GONE);
                    nearbyrest();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 8s
                            listdisplay();
                        }
                    }, 3000);
                }

            }
        }, 6000);



    }


    public void listdisplay()
    {

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonText = prefs.getString("key", null);
        String[] text = gson.fromJson(jsonText, String[].class);


        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MapsActivity.this);
        builderSingle.setIcon(R.drawable.fai_logo_gradients);
        builderSingle.setTitle("Have you been to this place today ? ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.select_dialog_singlechoice,text);

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setPositiveButton("Don't show", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                SharedPreferences.Editor editor = getSharedPreferences(DontWantTAG, MODE_PRIVATE).edit();
                editor.putString("Alertdisplay", "DONT");
                editor.apply();

                simpleSwitch.setChecked(false);

            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                avc(strName);
            }
        });
        builderSingle.show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void avc(final String res_name) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialogue, null);
        final EditText amount_txt = (EditText) alertLayout.findViewById(R.id.et_username);


        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(this);
        alert.setTitle("How much did you spend in "+ res_name+ " ? ");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String amount = amount_txt.getText().toString();
               // String pass = etPassword.getText().toString();

                Toast.makeText(getBaseContext(), "Amount: " + amount  , Toast.LENGTH_SHORT).show();
                AddData obj = new AddData(MapsActivity.this);
                obj.addartist (res_name, amount);

            }
        });

        android.support.v7.app.AlertDialog dialog = alert.create();
        dialog.show();


    }



    public void waveAnimation() {
        PropertyValuesHolder tvOne_Y = PropertyValuesHolder.ofFloat(hangoutTvOne.TRANSLATION_Y, -40.0f);
        PropertyValuesHolder tvOne_X = PropertyValuesHolder.ofFloat(hangoutTvOne.TRANSLATION_X, 0);
        waveOneAnimator = ObjectAnimator.ofPropertyValuesHolder(hangoutTvOne, tvOne_X, tvOne_Y);
        waveOneAnimator.setRepeatCount(-1);
        waveOneAnimator.setRepeatMode(ValueAnimator.REVERSE);
        waveOneAnimator.setDuration(400);
        waveOneAnimator.start();

        PropertyValuesHolder tvTwo_Y = PropertyValuesHolder.ofFloat(hangoutTvTwo.TRANSLATION_Y, -40.0f);
        PropertyValuesHolder tvTwo_X = PropertyValuesHolder.ofFloat(hangoutTvTwo.TRANSLATION_X, 0);
        waveTwoAnimator = ObjectAnimator.ofPropertyValuesHolder(hangoutTvTwo, tvTwo_X, tvTwo_Y);
        waveTwoAnimator.setRepeatCount(-1);
        waveTwoAnimator.setRepeatMode(ValueAnimator.REVERSE);
        waveTwoAnimator.setDuration(400);
        waveTwoAnimator.setStartDelay(300);
        waveTwoAnimator.start();

        PropertyValuesHolder tvThree_Y = PropertyValuesHolder.ofFloat(hangoutTvThree.TRANSLATION_Y, -40.0f);
        PropertyValuesHolder tvThree_X = PropertyValuesHolder.ofFloat(hangoutTvThree.TRANSLATION_X, 0);
        waveThreeAnimator = ObjectAnimator.ofPropertyValuesHolder(hangoutTvThree, tvThree_X, tvThree_Y);
        waveThreeAnimator.setRepeatCount(-1);
        waveThreeAnimator.setRepeatMode(ValueAnimator.REVERSE);
        waveThreeAnimator.setDuration(400);
        waveThreeAnimator.setStartDelay(400);
        waveThreeAnimator.start();

        PropertyValuesHolder tvFour_Y = PropertyValuesHolder.ofFloat(hangoutTvfour.TRANSLATION_Y, -40.0f);
        PropertyValuesHolder tvFour_X = PropertyValuesHolder.ofFloat(hangoutTvfour.TRANSLATION_X, 0);
        waveFourAnimator = ObjectAnimator.ofPropertyValuesHolder(hangoutTvfour, tvFour_Y, tvFour_X);
        waveFourAnimator.setRepeatCount(-1);
        waveFourAnimator.setRepeatMode(ValueAnimator.REVERSE);
        waveFourAnimator.setDuration(400);
        waveFourAnimator.setStartDelay(500);
        waveFourAnimator.start();

        PropertyValuesHolder tvFive_Y = PropertyValuesHolder.ofFloat(hangoutTvfive.TRANSLATION_Y, -40.0f);
        PropertyValuesHolder tvFive_X = PropertyValuesHolder.ofFloat(hangoutTvfive.TRANSLATION_X, 0);
        waveFiveAnimator = ObjectAnimator.ofPropertyValuesHolder(hangoutTvfive, tvFive_Y, tvFive_X);
        waveFiveAnimator.setRepeatCount(-1);
        waveFiveAnimator.setRepeatMode(ValueAnimator.REVERSE);
        waveFiveAnimator.setDuration(400);
        waveFiveAnimator.setStartDelay(600);
        waveFiveAnimator.start();


    }

}
