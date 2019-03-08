package com.easytechh.fuelapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
    int PROXIMITY_RADIUS = 10000;
    double latitude,longitude;
    PayPalConfiguration m_congiguration;
    String m_paypalClientId;
    Intent m_service;
    int m_paypalRequestCode = 1;
    private TextView total;

    Button submit;

    //Paypal code

    //Paypal intent request code to track onActivityResult method
    public static final int PAYPAL_REQUEST_CODE = 123;
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    //The views
    private Button buttonPay;
    private EditText editTextAmount;

    //Payment Amount
    private String paymentAmount;
    //code over

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = new Intent(this, PayPalService.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        startService(intent);
        //code over


new Thread(new Runnable() {
    @Override
    public void run() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();

        }
    }
});

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }



    private void getPayment() {
        //Getting the amount from editText
        paymentAmount = total.getText().toString();

        //Creating a paypalpayment
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(paymentAmount)), "USD", "Simplified Coding Fee",
                PayPalPayment.PAYMENT_INTENT_SALE);

        //Creating Paypal Payment activity intent
        Intent intent = new Intent(this, PaymentActivity.class);

        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        //Puting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        //Starting the intent activity for result
        //the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the result is from paypal
        if (requestCode == PAYPAL_REQUEST_CODE) {

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                //if confirmation is not null
                if (confirm != null) {
                    try {
                        //Getting the payment details
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        Log.i("paymentExample", paymentDetails);

                        //Starting a new activity for the payment details and also putting the payment details with intent
                        startActivity(new Intent(this, ConfirmationActivity.class)
                                .putExtra("PaymentDetails", paymentDetails)
                                .putExtra("PaymentAmount", paymentAmount));

                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }


//code over




    public void customDetails(String s) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View v = LayoutInflater.from(this).inflate(R.layout.alert, null);
        TextView add = v.findViewById(R.id.address);
        final Spinner qua0n = v.findViewById(R.id.quantity);
        final Spinner type = v.findViewById(R.id.type);
        final TextView unit = v.findViewById(R.id.unitprice);
        submit=v.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getPayment();
            }
        });
        total = v.findViewById(R.id.totalprice);



        add.setText(s);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!type.getSelectedItem().toString().equalsIgnoreCase("select fuel type")) {
                    if (type.getSelectedItem().toString().equalsIgnoreCase("petrol")) {
//set petrol unit price
                        unit.setText(String.valueOf(80));

                        total.setText(String.valueOf(Double.parseDouble(unit.getText().toString())*Double.parseDouble(qua0n.getSelectedItem().toString())));
                    } else if (type.getSelectedItem().toString().equalsIgnoreCase("diesel")) {
                        //set diesel unit price
                        unit.setText(String.valueOf(70));
                        total.setText(String.valueOf(Double.parseDouble(unit.getText().toString())*Double.parseDouble(qua0n.getSelectedItem().toString())));
                    } else {
                        //set cng unit price
                        unit.setText(String.valueOf(40));
                        total.setText(String.valueOf(Double.parseDouble(unit.getText().toString())*Double.parseDouble(qua0n.getSelectedItem().toString())));
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Select Fuel Type", Toast.LENGTH_SHORT).show();
                    unit.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                Toast.makeText(MapsActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();

            }
        });


        alert.setView(v);

        alert.show();
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


        if(currentLocationmMarker != null)
        {
            currentLocationmMarker.remove();

        }
        Log.d("lat = ",""+latitude);
        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
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
                                JSONObject object=response.getJSONObject("result");


                           objectPlace=parser.getPlace(object);

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

            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaces;

            final String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            String contact = googlePlace.get("formatted_phone");
            double lat = Double.parseDouble( googlePlace.get("lat"));
            double lng = Double.parseDouble( googlePlace.get("lng"));

         //  Log.d("place details",placeName+" : "+vicinity+" : "+contact+" : "+String.valueOf(lat));

            LatLng latLng = new LatLng( lat, lng);
            markerOptions.position(latLng);
           markerOptions.title(placeName + " : "+ vicinity+" : "+"PH."+" : "+ contact);

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        mMap.setOnMarkerClickListener((new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String st=marker.getTitle();
                customDetails(st);
                //Toast.makeText(MapsActivity.this, st, Toast.LENGTH_SHORT).show();
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
                EditText tf_location =  findViewById(R.id.TF_location);
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

       // Log.d("MapsActivityforPhoneNO", "url = "+googlePlaceUrl.toString());

        ArrayList list=new ArrayList();
        list.add(googlePlaceUrl.toString());

        /*for(int i=0;i<list.size();i++){
           // Log.d("DetailsUrl",list.get(i).toString());
        }*/
            gettingDetails(list);
        return googlePlaceUrl.toString();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }


    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED )
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            return false;

        }
        else
            return true;
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}