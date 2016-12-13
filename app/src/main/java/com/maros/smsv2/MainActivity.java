package com.maros.smsv2;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;


public class MainActivity extends AppCompatActivity implements Observer {

    public static String TAG = "CustomLog";
    public static String DEFAULT_URL = "http://balooooooon.tk/balon/api/flight/42/telemetry";

    private static final int RECORD_REQUEST_CODE = 101;
    private EditText urlAdresssTextField;
    private Button urlChangeButton;

    public static String getUrlAddress() {
        return urlAddress;
    }

    public static String urlAddress = DEFAULT_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ObservableObject.getInstance().addObserver(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlAdresssTextField = (EditText) findViewById(R.id.urlEditText);
        urlChangeButton = (Button) findViewById(R.id.changeUrlButton);
        urlAdresssTextField.setText(urlAddress);

        this.urlChangeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TO DO url address validation
                urlAddress = urlAdresssTextField.getText().toString();
                Log.i(TAG, "Changed url to:" + urlAddress);
            }
        });

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to recieve sms denied");
            makeRequest();
        }
        Log.i(TAG, "Loading Receiver");
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        SmsReceiver receiver = new SmsReceiver();
        registerReceiver(receiver, filter);

        //BULLSHIT

    }
    //OBSERVER
    @Override
    public void update(Observable observable, Object data) {
        Toast.makeText(this, String.valueOf("activity observer " + data), Toast.LENGTH_SHORT).show();
        Log.i(TAG, "activity observer " + String.valueOf(data));
        //textView.setText(String.valueOf(data));
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.INTERNET},
                RECORD_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECORD_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");
                } else {
                    Log.i(TAG, "Permission has been granted by user");
                }
            }
        }
    }
}
