package mobi.messagecube.sdk.openAPI;


import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import mobi.messagecube.sdk.android.MessageCube;


public class FacebookNativeAdProcess extends AsyncTask<String, Void, String> implements FacebookNativeAdInterface {

    private final String USER_AGENT = "Mozilla/5.0";

    @Override
    protected String doInBackground(String... params) {

        String url = "http://www.520carroll.com/api/v1/facebook_native_ad";

        // Application info, and package info and all other device info
        if (MessageCube.getKey1() != null) {
            try {
                url += "?key1=" + URLEncoder.encode(MessageCube.getKey1(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if (MessageCube.getKey2() != null) {
            try {
                url += "&key2=" + URLEncoder.encode(MessageCube.getKey2(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if (MessageCube.getApplicationId() != null) {
            try {
                url += "&application_id=" + URLEncoder.encode(MessageCube.getApplicationId(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            onSuccess(response.toString());

            // a list of BaseClass ...
            try {
                JSONObject responseJSONObjects = new JSONObject(response.toString());
                String nativeAdId = responseJSONObjects.getString("nativeAdId");
                onSuccess(nativeAdId);
            } catch (Throwable t) {

            }

            return "";
        } catch (Exception e) {
            e.printStackTrace();
            onFailure();
            return "";
        }
    }

    @Override
    protected void onPostExecute(String message) {
        //process message
    }

    public void sendGet() {
        execute();
    }

    @Override
    public void onSuccess(String nativeAdId) {
        System.out.println("onSuccess");
    }

    @Override
    public void onFailure() {
        System.out.println("onFailure");

        // TODO: We should have an error reporting.
    }
}
