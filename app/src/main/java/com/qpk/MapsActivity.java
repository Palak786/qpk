package com.qpk;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.qpk.adapter.AutoCompleteAdapter;
import com.qpk.model.PlacePredictions;
import com.qpk.utils.VolleyJSONRequest;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, Response.Listener<String>, Response.ErrorListener,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleMap mMap;
    private Button btnFindPath;
    private EditText etOrigin;
    private EditText etDestination;

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private GPSTracker gps;
    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    double latitude;
    double longitude;
    private ListView mAutoCompleteList;
    private String GETPLACESHIT = "places_hit";
    private PlacePredictions predictions;
    private Location mLastLocation;
    private AutoCompleteAdapter mAutoCompleteAdapter;
    private int CUSTOM_AUTOCOMPLETE_REQUEST_CODE = 20;
    private static final int MY_PERMISSIONS_REQUEST_LOC = 30;
    private FragmentManager fragmentManager;
    private String preFilledText;
    private Handler handler;
    private VolleyJSONRequest request;
    private GoogleApiClient mGoogleApiClient;
    private boolean isOrigen;
    private int PROXIMITY_RADIUS = 5000;
    private List<Route> finalroutes;
    private boolean isdrawRoute=false;
    private MyApplication controller;

    private String currentLanLong;
    private LatLng destinationLatLan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setTitle("Home");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        controller= (MyApplication) getApplication();

        gps=new GPSTracker(MapsActivity.this);
        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etDestination = (EditText) findViewById(R.id.etDestination);

        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String origin = etOrigin.getText().toString();
                String destination = etDestination.getText().toString();

                if (origin.isEmpty()) {
                    Toast.makeText(MapsActivity.this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (destination.isEmpty()) {
                    Toast.makeText(MapsActivity.this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(origin.equalsIgnoreCase("Current Location")){
                    origin=currentLanLong;
                }

                try {
                    new DirectionFinder(MapsActivity.this, origin, destination).execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
        manageSerching();
    }

    private void manageSerching() {
        fragmentManager = getSupportFragmentManager();
        mAutoCompleteList = (ListView) findViewById(R.id.searchResultLV);



        //get permission for Android M
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            fetchLocation();
        } else {

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOC);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            } else {
                fetchLocation();
            }
        }


        //Add a text change listener to implement autocomplete functionality
        etDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // optimised way is to start searching for laction after user has typed minimum 3 chars
                if (etDestination.getText().length() > 3) {

                    mAutoCompleteList.setVisibility(View.VISIBLE);
                 //   searchBtn.setVisibility(View.GONE);

                    Runnable run = new Runnable() {


                        @Override
                        public void run() {

                            // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                            MyApplication.volleyQueueInstance.cancelRequestInQueue(GETPLACESHIT);
                            isOrigen=false;
                            //build Get url of Place Autocomplete and hit the url to fetch result.
                            request = new VolleyJSONRequest(Request.Method.GET, getPlaceAutoCompleteUrl(etDestination.getText().toString()), null, null,MapsActivity.this, MapsActivity.this);

                            //Give a tag to your request so that you can use this tag to cancle request later.
                            request.setTag(GETPLACESHIT);

                            MyApplication.volleyQueueInstance.addToRequestQueue(request);

                        }

                    };

                    // only canceling the network calls will not help, you need to remove all callbacks as well
                    // otherwise the pending callbacks and messages will again invoke the handler and will send the request
                    if (handler != null) {
                        handler.removeCallbacksAndMessages(null);
                    } else {
                        handler = new Handler();
                    }
                    handler.postDelayed(run, 1000);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        etDestination.setText(preFilledText);
        etDestination.setSelection(etDestination.getText().length());



        //Add a text change listener to implement autocomplete functionality
        etOrigin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // optimised way is to start searching for laction after user has typed minimum 3 chars
                if (etOrigin.getText().length() > 3) {

                       mAutoCompleteList.setVisibility(View.VISIBLE);
                   // searchBtn.setVisibility(View.GONE);

                    Runnable run = new Runnable() {


                        @Override
                        public void run() {
                              isOrigen=true;
                            // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                            MyApplication.volleyQueueInstance.cancelRequestInQueue(GETPLACESHIT);

                            //build Get url of Place Autocomplete and hit the url to fetch result.
                            request = new VolleyJSONRequest(Request.Method.GET, getPlaceAutoCompleteUrl(etOrigin.getText().toString()), null, null,MapsActivity.this, MapsActivity.this);

                            //Give a tag to your request so that you can use this tag to cancle request later.
                            request.setTag(GETPLACESHIT);

                            MyApplication.volleyQueueInstance.addToRequestQueue(request);

                        }

                    };

                    // only canceling the network calls will not help, you need to remove all callbacks as well
                    // otherwise the pending callbacks and messages will again invoke the handler and will send the request
                    if (handler != null) {
                        handler.removeCallbacksAndMessages(null);
                    } else {
                        handler = new Handler();
                    }
                    handler.postDelayed(run, 1000);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        etOrigin.setText(preFilledText);
        etOrigin.setSelection(etDestination.getText().length());

/*        searchEdit.setOnEditorActionListener(new OnEditorActionListener()
        {

            public boolean onEditorAction(TextView arg0, int actionId,
                                          KeyEvent arg2)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO)
                {



                }

                return false;
            }
        });*/

        View view = this.getCurrentFocus();

        mAutoCompleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // pass the result to the calling activity
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if(isOrigen){
                    etOrigin.setText(""+predictions.getPlaces().get(position).getPlaceDesc());
                }else{
                    etDestination.setText(""+predictions.getPlaces().get(position).getPlaceDesc());
                }

                mAutoCompleteList.setVisibility(View.GONE);
                isdrawRoute=false;
                String origin = etOrigin.getText().toString();
                String destination = etDestination.getText().toString();

                if (origin.isEmpty()) {
                    Toast.makeText(MapsActivity.this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (destination.isEmpty()) {
                    Toast.makeText(MapsActivity.this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(origin.equalsIgnoreCase("Current Location")){
                    origin=currentLanLong;
                }

                try {
                    new DirectionFinder(MapsActivity.this, origin, destination).execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
               /* Intent intent = new Intent();
                intent.putExtra("Location Address", predictions.getPlaces().get(position).getPlaceDesc());
                setResult(CUSTOM_AUTOCOMPLETE_REQUEST_CODE, intent);
                finish();*/
            }
        });
    }

    public String getPlaceAutoCompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        urlString.append("?input=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&location=");
        urlString.append(latitude + "," + longitude); // append lat long of current location to show nearby results.
        urlString.append("&radius=500&language=en");
       // urlString.append("&key=" + "AIzaSyDnwLF2-WfK8cVZt9OoDYJ9Y8kspXhEHfI");
        urlString.append("&key=" + "AIzaSyBaIWBWZUTIo2zif0UQDpHlT2C7MJxrfCM");

        Log.d("FINAL URL:::   ", urlString.toString());
        return urlString.toString();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(MapsActivity.this,""+error,Toast.LENGTH_LONG).show();
       // searchBtn.setVisibility(View.VISIBLE);

    }

    @Override
    public void onResponse(String response) {

       // searchBtn.setVisibility(View.VISIBLE);
        Log.d("PLACES RESULT:::", response);
        Gson gson = new Gson();
        predictions = gson.fromJson(response, PlacePredictions.class);

        if (mAutoCompleteAdapter == null) {
            mAutoCompleteAdapter = new AutoCompleteAdapter(this, predictions.getPlaces(),MapsActivity.this);
            mAutoCompleteList.setAdapter(mAutoCompleteAdapter);
        } else {
            mAutoCompleteAdapter.clear();
            mAutoCompleteAdapter.addAll(predictions.getPlaces());
            mAutoCompleteAdapter.notifyDataSetChanged();
            mAutoCompleteList.invalidate();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();


                    LatLng hcmus = new LatLng(latitude,longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 14));
                    etOrigin.setText("Current Location");
                    currentLanLong=latitude + ","+longitude;
          /*  originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title("Đại học Kh")
                    .position(hcmus)));*/
                    // \n is for new line
                  //  Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

               // etOrigin.setText("new loc"+);
            }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
    public void showSearchSuccessDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Success !");

        // Setting Dialog Message
        alertDialog.setMessage("Parking available");

        // On pressing Settings button
        alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                    Intent gIntent;
                gIntent = new Intent(MapsActivity.this, Booking.class);
                startActivity(gIntent);
//                FragmentRoute fragmentRoute=new FragmentRoute();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frame_maps, fragmentRoute);
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.commit();

            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
    public void showSearchFailedDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Sorry !");

        // Setting Dialog Message
        alertDialog.setMessage("No Parking available");

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void fetchLocation(){
        //Build google API client to use fused location
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission granted!
                    fetchLocation();

                } else {
                    // permission denied!

                    Toast.makeText(this, "Please grant permission for using this app!", Toast.LENGTH_LONG).show();
                }
                return;
            }


        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
          mMap = googleMap;

         // gps = new GPSTracker(MapsActivity.this);

        // check if GPS enabled




        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        if (mMap != null) {


            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                @Override
                public void onMyLocationChange(Location arg0) {
                    // TODO Auto-generated method stub

                   // mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));

                    //load the traffic now
                    mMap.setTrafficEnabled(true);
                }
            });

        }
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {

         if(isdrawRoute){
             isdrawRoute=false;
             progressDialog.dismiss();
             polylinePaths = new ArrayList<>();
             originMarkers = new ArrayList<>();
             destinationMarkers = new ArrayList<>();

             for (Route route : finalroutes) {
                 ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
                 ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

                 originMarkers.add(mMap.addMarker(new MarkerOptions()
                         .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                         .title(route.startAddress)
                         .position(route.startLocation)));

                 destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                         .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                         .title(route.endAddress)
                         .position(route.endLocation)));

                 PolylineOptions polylineOptions = new PolylineOptions().
                         geodesic(true).
                         color(Color.BLUE).
                         width(10);

                 for (int i = 0; i < route.points.size(); i++)
                     polylineOptions.add(route.points.get(i));

                 polylinePaths.add(mMap.addPolyline(polylineOptions));


             }
         }else{
             findParking(routes);
         }


    }

    private void findParking(List<Route> routes) {
        for (Route route : routes) {

            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=" + route.endLocation.latitude+","+route.endLocation.longitude);
            googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
            googlePlacesUrl.append("&types=" +"Parking");
            googlePlacesUrl.append("&sensor=flase");
           // googlePlacesUrl.append("&key=" + "AIzaSyDnwLF2-WfK8cVZt9OoDYJ9Y8kspXhEHfI");
            googlePlacesUrl.append("&key=" + "AIzaSyBaIWBWZUTIo2zif0UQDpHlT2C7MJxrfCM");

            this.destinationLatLan=new LatLng(route.endLocation.latitude,route.endLocation.longitude);

          //  Toast.makeText(MapsActivity.this,""+destinationLatLan,Toast.LENGTH_LONG).show();

            GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
            Object[] toPass = new Object[2];
            toPass[0] =mMap;
            toPass[1] = googlePlacesUrl.toString();
            googlePlacesReadTask.execute(toPass);
        }
    }


    public class GooglePlacesReadTask extends AsyncTask<Object, Integer, String> {
        String googlePlacesData = null;
        GoogleMap googleMap;

        @Override
        protected String doInBackground(Object... inputObj) {
            try {
                googleMap = (GoogleMap) inputObj[0];
                String googlePlacesUrl = (String) inputObj[1];
                Http http = new Http();
                googlePlacesData = http.read(googlePlacesUrl);
            } catch (Exception e) {
                Log.d("Google Place Read Task", e.toString());
            }
            return googlePlacesData;
        }

        @Override
        protected void onPostExecute(String result) {
            PlacesDisplayTask placesDisplayTask = new PlacesDisplayTask();
            Object[] toPass = new Object[2];
            toPass[0] = googleMap;
            toPass[1] = result;
          //  Toast.makeText(MapsActivity.this,"result1 "+result,Toast.LENGTH_LONG).show();
            placesDisplayTask.execute(toPass);
        }
    }


    public class PlacesDisplayTask extends AsyncTask<Object, Integer, List<HashMap<String, String>>> implements GoogleMap.OnMarkerClickListener {

        JSONObject googlePlacesJson;
        GoogleMap googleMap;

        @Override
        protected List<HashMap<String, String>> doInBackground(Object... inputObj) {

            List<HashMap<String, String>> googlePlacesList = null;
            Places placeJsonParser = new Places();

            try {
                googleMap = (GoogleMap) inputObj[0];
                googlePlacesJson = new JSONObject((String) inputObj[1]);
              //  Toast.makeText(MapsActivity.this,"Exp 0 "+googlePlacesJson.toString(),Toast.LENGTH_LONG).show();
               // Log.d("ROUTE 1", ""+googlePlacesJson);
                googlePlacesList = placeJsonParser.parse(googlePlacesJson);
            } catch (Exception e) {
                Log.d("Exception", e.toString());
//                Toast.makeText(MapsActivity.this,"Exp 1 "+e.toString(),Toast.LENGTH_LONG).show();

            }
            return googlePlacesList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            googleMap.clear();
            progressDialog.dismiss();

            if(list!=null){
                for (int i = 0; i < list.size(); i++) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    HashMap<String, String> googlePlace = list.get(i);
                    double lat = Double.parseDouble(googlePlace.get("lat"));
                    double lng = Double.parseDouble(googlePlace.get("lng"));
                    String placeName = googlePlace.get("place_name");
                    String vicinity = googlePlace.get("vicinity");
                   // Toast.makeText(MapsActivity.this,"Exp 2 "+placeName,Toast.LENGTH_LONG).show();
                    LatLng latLng = new LatLng(lat, lng);
                    markerOptions.position(latLng);
                    markerOptions.title(placeName);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
                    googleMap.addMarker(markerOptions);
                    //Toast.makeText(getApplicationContext(),list.size(), Toast.LENGTH_SHORT).show();
                }
                MarkerOptions markerOptions1=new MarkerOptions();
                markerOptions1.position(destinationLatLan);
                markerOptions1.title(etDestination.getText().toString());
                googleMap.setOnMarkerClickListener(this);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLan, 12));
                googleMap.addMarker(markerOptions1);
                googleMap.setOnMarkerClickListener(this);
                //isdrawRoute=true;
                //etDestination.setText();
               /* destinationLatLan=list.get(list.size()-1).get("lat")+","+list.get(list.size()-1).get("lng");
                String origin = etOrigin.getText().toString();
                String destination = etDestination.getText().toString();
                if (origin.isEmpty()) {
                    Toast.makeText(MapsActivity.this, "Please enter origin address!", Toast.LENGTH_SHORT).show();

                }else
                if (destination.isEmpty()) {
                    Toast.makeText(MapsActivity.this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
                }else{
                    if(origin.equalsIgnoreCase("Current Location")){
                        origin=currentLanLong;
                    }
                    try {
                        new DirectionFinder(MapsActivity.this, origin,destinationLatLan).execute();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }*/
            }else {
                Toast.makeText(MapsActivity.this,"NO PARKING AREA",Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            controller.setTitle(marker.getTitle());
            controller.setSourceLocation(currentLanLong);
            controller.setDestinationLocation(marker.getPosition().latitude+","+marker.getPosition().longitude);


            final String urlSuffix = "?latitude=" + marker.getPosition().latitude + "&longitude=" + marker.getPosition().longitude;

            class Mapstuff extends AsyncTask<String, Void, String>
            {
                private ProgressDialog loading;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading = ProgressDialog.show(MapsActivity.this, "Searching...", null, true, true);
                }

                @Override
                protected void onPostExecute(String s) {
                    String string = s.trim();
                    loading.dismiss();
                    boolean isSuccess=true;
                    if(s.equalsIgnoreCase("Success")){
                        showSearchSuccessDialog();
                    }else{
                        showSearchFailedDialog();
                    }
////            if(string.equals("Login successful."))
//            {
//                startActivity(new Intent(Login.this, Home.class));
//            }
//            else if(string.equals("Invalid username or password."))
//            {   }
                }

                @Override
                protected String doInBackground(String... params) {
                    String map_url;
                    map_url="http://techiegirls6.esy.es/techiegirls/AreaInfo.php";
                    BufferedReader bufferReader;
                    try {
                        URL url=new URL(map_url+urlSuffix);
                        HttpURLConnection con=(HttpURLConnection)url.openConnection();
                        bufferReader=new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String result;
                        result=bufferReader.readLine();
                        return  result;

                    }catch (Exception e){
                        return null;
                    }
                }
            }
            Mapstuff ur = new Mapstuff();
            ur.execute(urlSuffix);




/*
            FragmentRoute fragmentRoute=new FragmentRoute();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_maps, fragmentRoute);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();*/

            // marker.getPosition()
            // etDestination.setText(marker.getTitle());
          /*  destinationLatLan=marker.getPosition().latitude+","+marker.getPosition().longitude;
            String origin = etOrigin.getText().toString();
            String destination = etDestination.getText().toString();
            if (origin.isEmpty()) {
                Toast.makeText(MapsActivity.this, "Please enter origin address!", Toast.LENGTH_SHORT).show();

            }else
            if (destination.isEmpty()) {
                Toast.makeText(MapsActivity.this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            }else{
                if(origin.equalsIgnoreCase("Current Location")){
                    origin=latitude+","+longitude;
                }
                isdrawRoute=true;
                try {
                    new DirectionFinder(MapsActivity.this, origin,destination).execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
*/


            return false;



           /* FragmentRoute fragmentRoute=new FragmentRoute();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_maps, fragmentRoute);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();*/

           // marker.getPosition()
           // etDestination.setText(marker.getTitle());
          /*  destinationLatLan=marker.getPosition().latitude+","+marker.getPosition().longitude;
            String origin = etOrigin.getText().toString();
            String destination = etDestination.getText().toString();
            if (origin.isEmpty()) {
                Toast.makeText(MapsActivity.this, "Please enter origin address!", Toast.LENGTH_SHORT).show();

            }else
            if (destination.isEmpty()) {
                Toast.makeText(MapsActivity.this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            }else{
                if(origin.equalsIgnoreCase("Current Location")){
                    origin=latitude+","+longitude;
                }
                isdrawRoute=true;
                try {
                    new DirectionFinder(MapsActivity.this, origin,destination).execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
*/



        }
    }

}
