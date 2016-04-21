package com.toggl.challenge;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by luiz on 4/21/16.
 */
public class StopTimerTask extends AsyncTask<String, Void, String> {

    private final String URL = "https://www.toggl.com/api/v8/time_entries/";

    public StopTimerTask() {

    }

    protected String doInBackground(String... id) {
        if (id.length == 0) {
            return null;
        }

        HttpURLConnection connection = null;
        JSONObject json = null;
        try {
//            byte[] bytes = Base64.encode("token:api_token".getBytes(), Base64.DEFAULT);
//            String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);

            java.net.URL url = new URL(this.URL + id[0] + "/stop");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Basic NDgxMDNlZDYzYjliYTg0N2FiMDJiZmY3ZTYxMDRhM2E6YXBpX3Rva2Vu");

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            json = new JSONObject(sb.toString());
            Log.d("HTTP", json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

        return json != null ? json.toString() : "";
    }
}
