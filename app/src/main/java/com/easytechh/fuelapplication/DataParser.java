package com.easytechh.fuelapplication;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DataParser  {
GoogleMap mMap;

    public HashMap<String, String> getPlace(JSONObject object)
    {

   HashMap<String, String> googlePlaceMap = new HashMap<>();

        String placeName = "--NA--";
        String address= "--NA--";
        String formatted_phone="Not Available";
        String other_number="Not Available";
        JSONObject jsonObject=new JSONObject();
        String hours="Not Available";
        String latitude= "";
        String longitude="";
        String reference="";




        try {
            if(object.has("formatted_phone_number")){
                formatted_phone=object.getString("formatted_phone_number");
            }

            if(object.has("formatted_address")){
                address=object.getString("formatted_address");
            }

            if(object.has("name")){
                placeName=object.getString("name");

            }

            if(object.has("international_phone_number")){
                other_number=object.getString("international_phone_number");
            }
          if(object.has("opening_hours")){
        jsonObject=object.getJSONObject("opening_hours");
              hours=jsonObject.toString();

          }





            latitude = object.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = object.getJSONObject("geometry").getJSONObject("location").getString("lng");

            reference = object.getString("reference");

            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", address);
            googlePlaceMap.put("formatted_phone", formatted_phone);
            googlePlaceMap.put("other_number",other_number);
          //  googlePlaceMap.put("weekdays",weekdays);
          //  googlePlaceMap.put("weekdays",array.toString());
            googlePlaceMap.put("Hours",hours);


            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);


        }
        catch (JSONException e) {
            e.printStackTrace();
        }


   return googlePlaceMap;

    }
}
