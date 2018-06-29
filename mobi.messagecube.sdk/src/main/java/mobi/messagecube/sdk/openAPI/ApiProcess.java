package mobi.messagecube.sdk.openAPI;

import java.util.ArrayList;

public interface ApiProcess {
    void onSuccess(String response);
    void onSuccess(String textBody, ArrayList<BaseClass> baseClasses);
    void onFailure();
}
