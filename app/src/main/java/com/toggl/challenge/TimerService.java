package com.toggl.challenge;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by luiz on 4/20/16.
 */
public class TimerService extends AsyncTask<String, Void, String> {

    private final String URL = "https://www.toggl.com/api/v8/time_entries";

    public TimerService () {

    }

    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        JSONObject json = null;
        try {
//            byte[] bytes = Base64.encode("token:api_token".getBytes(), Base64.DEFAULT);
//            String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);

            URL url = new URL(this.URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Basic NDgxMDNlZDYzYjliYTg0N2FiMDJiZmY3ZTYxMDRhM2E6YXBpX3Rva2Vu");
            connection.setRequestProperty("Body", "{\"time_entry\": {\"created_with\": \"postman\"}}");

            OutputStream os = connection.getOutputStream();
            os.write("{\"time_entry\": {\"created_with\": \"group 1 app\"}}".getBytes("UTF-8"));
            os.close();

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
