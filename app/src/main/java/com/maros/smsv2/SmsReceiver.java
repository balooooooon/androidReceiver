package com.maros.smsv2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class SmsReceiver extends BroadcastReceiver {
    private Bundle bundle;
    private SmsMessage currentSMS;
    private String message;

    final String DELIMITER = ",";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                if (pdu_Objects != null) {

                    for (Object aObject : pdu_Objects) {

                        currentSMS = getIncomingMessage(aObject, bundle);

                        String senderNo = currentSMS.getDisplayOriginatingAddress();

                        message = currentSMS.getDisplayMessageBody();


                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        //String URL = String.format(MainActivity.getUrlAddress(), 42);
                        String URL = MainActivity.getUrlAddress();
                        JSONObject jsonParsed = parseStringToJsonObject(message);


                        //jsonBody.put("senderNum", "senderNo");
                        final String mRequestBody = jsonParsed.toString();
                        Log.i(MainActivity.TAG, "Json body parsed into:" + mRequestBody);
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("VOLLEY", response);
                                ObservableObject.getInstance().updateValue(response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("VOLLEY", error.toString());
                            }
                        }) {
                            @Override
                            public String getBodyContentType() {
                                return "application/json; charset=utf-8";
                            }

                            @Override
                            public byte[] getBody() throws AuthFailureError {
                                try {
                                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                                } catch (UnsupportedEncodingException uee) {
                                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                                    return null;
                                }
                            }

                            @Override
                            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                String responseString = "";
                                if (response != null) {
                                    responseString = String.valueOf(response.statusCode);
                                    // can get more details such as response.headers
                                }
                                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                            }
                        };

                        requestQueue.add(stringRequest);

                    }
                    this.abortBroadcast();
                    // End of loop
                }
            }
        } // bundle null
    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }
        return currentSMS;
    }

    private JSONObject parseStringToJsonObject(String message) {
        //Get all tokens available in line
        String[] tokens = message.split(DELIMITER);
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("flightHash", "0cc175b9c0f1b6a831c399e269772661");
            JSONObject dataJson = new JSONObject();
            dataJson.put("time", tokens[0]);
            dataJson.put("timestamp", tokens[0]);
            JSONArray parametersJsonArray = new JSONArray();

            JSONObject positionJson = new JSONObject();
            positionJson.put("type", "position");
            JSONObject positionValuesJson = new JSONObject();
            positionValuesJson.put("lat", tokens[4]);
            positionValuesJson.put("lng", tokens[5]);
            positionValuesJson.put("alt", tokens[6]);
            positionJson.put("values", positionValuesJson);

            parametersJsonArray.put(positionJson);

            JSONObject temperatureJson = new JSONObject();
            temperatureJson.put("type", "temperature");
            JSONObject temperatureValuesJson = new JSONObject();
            temperatureValuesJson.put("in", tokens[2]);
            temperatureValuesJson.put("out", tokens[1]);
            temperatureJson.put("values", temperatureValuesJson);

            parametersJsonArray.put(temperatureJson);
            dataJson.put("parameters", parametersJsonArray);
            jsonObject.put("data", dataJson);
        } catch (JSONException | ArrayIndexOutOfBoundsException exception) {

            exception.printStackTrace();
        }

        return jsonObject;
    }
}
