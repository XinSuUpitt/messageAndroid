package mobi.messagecube.sdk.openAPI;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import mobi.messagecube.sdk.android.MessageCube;
import mobi.messagecube.sdk.database.Database;
import mobi.messagecube.sdk.database.MessageItemModal;

public class ReceiveProcess extends AsyncTask<MessageCubeParams, Void, String> implements ApiProcess {

    private final String USER_AGENT = "Mozilla/5.0";

    @Override
    protected String doInBackground(MessageCubeParams... params) {
        if (params.length == 0) {
            return "";
        }

        Database dataSource = null;

        MessageCubeParams messageCubeParams = params[0];

        String url = messageCubeParams.url;

        // Try to get the search result from cache
        String searchResultFromCache = null;

        // TODO: This causes a crash.
        if (messageCubeParams.mContext != null) {
            dataSource = new Database(messageCubeParams.mContext);
            dataSource.open();

            MessageItemModal foundMessageItemModal = dataSource.getMessageItem(messageCubeParams.receiverNumber, String.valueOf(messageCubeParams.mDate));
            if (foundMessageItemModal != null) {
                searchResultFromCache = foundMessageItemModal.getResult();
            }
        } else {
            System.out.println("mContext is null");
        }

        if (searchResultFromCache != null) {

            try {
                JSONObject responseJSONObjects = new JSONObject(searchResultFromCache);
                ArrayList<BaseClass> results = BaseClass.parseJSONObject(responseJSONObjects);
                onSuccess(responseJSONObjects.getString("textBody"), results);
                return "";

            } catch (JSONException e) {
                e.printStackTrace();

                // TODO: The system will emit a warning if we don't close the database.
                dataSource.close();

                // Continue, and send a request to the service.
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
                ArrayList<BaseClass> results = BaseClass.parseJSONObject(responseJSONObjects);
                onSuccess(responseJSONObjects.getString("textBody"), results);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            // Store it to the cache.
            if (dataSource != null) {
                MessageItemModal messageItemModal = dataSource.createMessageItem(messageCubeParams.receiverNumber, String.valueOf(messageCubeParams.mDate), response.toString());
                dataSource.close();
            }

            return "";

        } catch (Exception e) {
            dataSource.close();
            e.printStackTrace();
            onFailure();
            return "";
        }

    }

    @Override
    protected void onPostExecute(String message) {
        //process message
    }

    public void sendGet(MessageCubeParams messageCubeParams) {
        if (MessageCube.getHasInitialized() == false) {
            System.out.println("Please set the API key before sending any requests.");
        }

        if (messageCubeParams.q == null) {
            System.out.println("Invalid string");
            return;
        }
        execute(messageCubeParams);
    }

    @Override
    public void onSuccess(String response) {
        System.out.println("onSuccess");
    }

    @Override
    public void onSuccess(String textBody, ArrayList<BaseClass> baseClasses) {
        System.out.println("onSuccess");
    }

    @Override
    public void onFailure() {
        System.out.println("onFailure");

        // TODO: We should have an error reporting.
    }
}
