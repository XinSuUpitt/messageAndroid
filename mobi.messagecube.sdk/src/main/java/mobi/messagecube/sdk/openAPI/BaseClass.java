package mobi.messagecube.sdk.openAPI;

import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BaseClass {

    // TODO: This must be an enum.
    public String apiSource = "";

    public String id = "";
    public String name = "";
    public String url = "";
    public String displayUrl = "";
    public String snippet = "";

    private boolean isValid = false;

    public String imageUrl = "";

    public HashMap<String, Object> optional = new HashMap<String, Object>();

    // Used in the forward text body.
    public int index = -1;  // Default value is -1 that means "unset".
    public String forwardMessage = "";

    public String jsonString = "";

    public BaseClass() {

    }

    public BaseClass(JSONObject object) {

        try {
            jsonString = object.toString();

            apiSource = object.getString("apiSource");

            id = object.getString("id");
            name = object.getString("name");
            url = object.getString("url");
            displayUrl = object.getString("displayUrl");
            snippet = object.getString("snippet");
            imageUrl = object.getString("imageUrl");

            String optionalString = object.getString("optional");
            optional = jsonToMap(optionalString);

            index = object.getInt("index");
            forwardMessage = object.getString("forwardMessage");

            isValid = true;

        } catch (JSONException e) {
            isValid = false;
        }
    }

    public static ArrayList<BaseClass> parseJSONObject(JSONObject response) {
        ArrayList<BaseClass> results = new ArrayList<BaseClass>();
        try {
            JSONArray jsonArray = response.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                BaseClass result = new BaseClass(object);
                results.add(result);
            }
            return results;

        } catch (JSONException e) {
            e.printStackTrace();

            // Return what we have parsed successfully.
            return results;
        }
    }

    public static HashMap<String, Object> jsonToMap(String t) throws JSONException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        JSONObject jObject = new JSONObject(t);
        Iterator<String> keys = jObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jObject.get(key);
            map.put(key, value);
        }

        return map;
    }
}
