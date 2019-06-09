package com.easytechh.fuelapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class Information extends AppCompatActivity  implements TextToSpeech.OnInitListener {

    private static final int REQUEST_CALL = 1;
    TextView title,address,phone,distance,textView;
    ImageView imageCall;
    Spinner spinner;
    private TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        title = findViewById(R.id.name);
        address = findViewById(R.id.address);
        phone = findViewById(R.id.phone);
        distance = findViewById(R.id.distance);
        spinner=findViewById(R.id.spinner1);

        imageCall = findViewById(R.id.call);
        tts = new TextToSpeech(this, this);

       new Thread(new Runnable() {
           @Override
           public void run() {
               imageCall.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       makePhoneCall();
                   }
               });
           }
       });


        String details = getIntent().getStringExtra("Details");



        String dist = getIntent().getStringExtra("Distance");
        String[] single = details.split("  ");
        String name = single[0];
        String add = single[1];
        String contact = single[2];
        String workDetails=single[3];

        ArrayList<String> workList=new ArrayList<>();
        try {
            JSONObject jsonObject=new JSONObject(workDetails);
            JSONArray array=jsonObject.getJSONArray("weekday_text");
            for(int i=0;i<array.length();i++){
                workList.add(array.get(i).toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }



        title.setText(name);
        address.setText(add);
        distance.setText(dist + " meter");
        phone.setText(contact);

        new Thread(new Runnable() {
            @Override
            public void run() {
                speakOut();
            }
        });



        if(!(workList.size()>0)) {
            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,new String[]{"No Working Details Available"});
            spinner.setAdapter(arrayAdapter);


        }else{
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, workList);
            spinner.setAdapter(adapter);
        }

    }


    @Override
    public void onDestroy() {
// Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {

                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut() {

        String text = phone.getText().toString();
        if(text.equalsIgnoreCase("Not Available")) {




            tts.setSpeechRate(.7f);
            tts.speak("Sorryyy Phone number is not available , you can send a message to get help", TextToSpeech.QUEUE_FLUSH, null);
        }else{

            tts.setSpeechRate(.7f);
            tts.speak("you can make a call to get help", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    
    private void makePhoneCall() {
            String number = phone.getText().toString();
            if (number.equalsIgnoreCase("not available")) {
                Toast.makeText(this, "you don't have any number to call", Toast.LENGTH_SHORT).show();
            } else {
                if (number.trim().length() > 0) {

                    if (ContextCompat.checkSelfPermission(Information.this,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Information.this,
                                new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                    } else {
                        String dial = "tel:" + number;
                        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
                    }

                } else {
                    Toast.makeText(Information.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == REQUEST_CALL) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall();
                } else {
                    Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
                }
            }
        }


}