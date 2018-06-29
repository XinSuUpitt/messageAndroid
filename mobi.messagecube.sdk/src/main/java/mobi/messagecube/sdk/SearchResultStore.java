package mobi.messagecube.sdk;

import org.json.JSONObject;

// The size is always 1
public class SearchResultStore {

    private static JSONObject response = null;
    private static boolean hasData = false;

    public static void push(JSONObject newResponse) {
        response = newResponse;
        hasData = true;
    }

    public static JSONObject pop() {
        if (hasData) {
            hasData = false;
            return response;
        } else {
            return null;
        }
    }

    public static boolean hasData() {
        return hasData;
    }
}