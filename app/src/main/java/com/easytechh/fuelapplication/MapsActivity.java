package com.easytechh.fuelapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{


    GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastlocation;
    private Marker currentLocationmMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 1000;
    double latitude,longitude;
    double end_latitude,end_longitude;

    private TextView total;


    Button submit;


    private Button buttonPay;
    EditText tf_location;

    //Payment Amount
    private String paymentAmount;
    //code over

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tf_location =  findViewById(R.id.TF_location);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();

        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            bulidGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }





    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();

    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastlocation = location;
        String address=null;
try {
    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
    address=addresses.get(0).getAddressLine(0);
}catch(Exception e){

}

        if(currentLocationmMarker != null)
        {
            currentLocationmMarker.remove();

        }
        Log.d("lat = ",""+latitude);
        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(address);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationmMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(client != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }
    }

    public void placeId(final String url){

      Runnable r=new Runnable() {
          @Override
          public void run() {
              JsonObjectRequest objectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                  @Override
                  public void onResponse(JSONObject response) {
                      try {
                          JSONArray array=response.getJSONArray("results");
                          for(int i=0;i<array.length();i++) {
                              JSONObject object = array.getJSONObject(i);
                              String placeId=object.getString("place_id");
                              //Log.d("Place Id : ",placeId);
                              getUrll(placeId);
                          }
                      } catch (JSONException e) {
                          e.printStackTrace();
                      }
                  }
              }, new Response.ErrorListener() {
                  @Override
                  public void onErrorResponse(VolleyError error) {

                  }
              });
              RequestQueue requestQueue=Volley.newRequestQueue(getApplicationContext());
              requestQueue.add(objectRequest);

          }
      };
     Thread t=new Thread(r);
     t.start();


            }




    void gettingDetails(final ArrayList list){

        Runnable r=new Runnable() {
            DataParser parser=  new DataParser();


            @Override
            public void run() {
                for(int i=0;i<list.size();i++){
                    String url=list.get(i).toString();


                    //Log.d("Urls : ",url);
                    JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //parser.getData(response.toString());
                            try {
                                HashMap<String,String> objectPlace;
                              //  HashMap<String,ArrayList<String>> workingHours;
                                ArrayList<String> workingHours=new ArrayList<>();


                                JSONObject object=response.getJSONObject("result");
                           /* JSONObject jsonObjectRequest1=object.getJSONObject("opening_hours");
                               JSONArray array=jsonObjectRequest1.getJSONArray("weekday_text");
*/
                       /*for(int i=0;i<array.length();i++){
                                 String day=array.getString(i);
                                object.put("day",day);
                             }*/


                           objectPlace=parser.getPlace(object);
                          // workingHours=parser.getPlace(array);


                         showNearbyPlaces(objectPlace);


                            } catch (JSONException e) {

                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
                    RequestQueue requestQueue=Volley.newRequestQueue(getApplicationContext());
                    requestQueue.add(jsonObjectRequest);
                }



            }

        };
         Thread t=new Thread(r);
         t.start();


    }



   public void showNearbyPlaces(HashMap<String, String> nearbyPlaces)
    {

            final MarkerOptions markerOptions = new MarkerOptions();



            final String placeName = nearbyPlaces.get("place_name");
            final String vicinity = nearbyPlaces.get("vicinity");
            final String contact = nearbyPlaces.get("formatted_phone");
           final String contact2=nearbyPlaces.get("other_number");
                final String timeDetails=nearbyPlaces.get("Hours");



        double lat = Double.parseDouble(nearbyPlaces.get("lat"));
            double lng = Double.parseDouble( nearbyPlaces.get("lng"));


            LatLng latLng = new LatLng( lat, lng);
            markerOptions.position(latLng);

            if(!contact.equalsIgnoreCase("Not Available")) {
                markerOptions.title(placeName + "  " + vicinity + "  " + contact+"  "+timeDetails);
            }else{
                markerOptions.title(placeName + "  " + vicinity + "  " + contact2+"  "+timeDetails);
            }

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        mMap.setOnMarkerClickListener((new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String st=marker.getTitle();


                Intent intent=new Intent(MapsActivity.this,Information.class);
                intent.putExtra("Details",st);


                end_latitude=marker.getPosition().latitude;
                end_longitude=marker.getPosition().longitude;
                float results[]=new float[10];
                Location.distanceBetween(latitude,longitude,end_latitude,end_longitude,results);

                tf_location.setText("Distance : "+results[0]+" meter");
              intent.putExtra("Distance",String.valueOf(results[0]));
                startActivity(intent);
                return true;
            }
        }));


    }






    public void onClick(View v)
    {
        Object dataTransfer[] = new Object[2];

        switch(v.getId())
        {
            case R.id.B_search:

                String location = tf_location.getText().toString();
                List<Address> addressList;


                if(!location.equals(""))
                {
                    Geocoder geocoder = new Geocoder(this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 5);

                        if(addressList != null)
                        {
                            for(int i = 0;i<addressList.size();i++)
                            {
                                LatLng latLng = new LatLng(addressList.get(i).getLatitude() , addressList.get(i).getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(location);
                                mMap.addMarker(markerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.B_gasStation:
                mMap.clear();
                String hospital = "gas_station";
                String url = getUrl(latitude, longitude, hospital);

                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                Toast.makeText(MapsActivity.this, "Showing Nearby Gas_Stations", Toast.LENGTH_SHORT).show();
                break;



            case R.id.B_to:
        }
    }




    public String getUrl(double latitude , double longitude , String nearbyPlace)
    {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyDqIPTdo_6Hry3xknSuPpWDR58G9QT83Rk");

        Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());
                     placeId(googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    public String getUrll(String placeid)
    {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        googlePlaceUrl.append("placeid="+placeid);
        googlePlaceUrl.append("&key="+"AIzaSyA0sipSO46iA57bZsoXOgizb3b651loCtA");

       Log.d("MapsActivityforPhoneNO", "url = "+googlePlaceUrl.toString());

        ArrayList list=new ArrayList();
        list.add(googlePlaceUrl.toString());

       /* for(int i=0;i<list.size();i++){
            Log.d("DetailsUrl",list.get(i).toString());
        }*/
            gettingDetails(list);
        return googlePlaceUrl.toString();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;

        } else
            return true;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}