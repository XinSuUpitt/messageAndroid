package mobi.messagecube.sdk.openAPI;

import android.app.Activity;
import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import mobi.messagecube.sdk.MessageItemParser;
import mobi.messagecube.sdk.android.MessageCube;

public class MessageCubeParams {

    // Required
    Context mContext;
    String q;

    // Generated
    String url;

    // Optional
    String senderNumber;
    String receiverNumber;

    long mDate;

    // TODO: we should update the API. Merge postalCode and location as one argument.
    public MessageCubeParams(Context context, String keyword, String postalCode, String location, String senderNumber, String receiverNumber, long date) {

        this.mContext = context;
        q = keyword;

        this.senderNumber = senderNumber;
        this.receiverNumber = receiverNumber;

        mDate = date;

        String awsUrl = "http://www.520carroll.com/api/v1/messages";
        q = q.trim();

        // TODO: This is also associated to the location like at/in/near.
        if (location != null) {
            if (location.length() > 0) {
                q = q + " near " + location;
            }
        }

        try {
            awsUrl += "?q="+ URLEncoder.encode(q, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            awsUrl += "?q=" + q;
        }

        if (postalCode != null && location == null) {
            postalCode = postalCode.trim();
            try {
                awsUrl += "&postal_code=" + URLEncoder.encode(postalCode, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                awsUrl += "&postal_code=" + postalCode;
            }
        }

        if (senderNumber != null) {
            senderNumber = senderNumber.trim();
            senderNumber = senderNumber.replaceAll("[^\\d]", "");
            awsUrl += "&sender=" + senderNumber;
        }

        if (receiverNumber != null) {
            receiverNumber = receiverNumber.trim();
            receiverNumber = receiverNumber.replaceAll("[^\\d]", "");
            awsUrl += "&receiver=" + receiverNumber;
        }


        // Application info, and package info and all other device info
        if (MessageCube.getKey1() != null) {
            try {
                awsUrl += "&key1=" + URLEncoder.encode(MessageCube.getKey1(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if (MessageCube.getKey2() != null) {
            try {
                awsUrl += "&key2=" + URLEncoder.encode(MessageCube.getKey2(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if (MessageCube.getApplicationId() != null) {
            try {
                awsUrl += "&application_id=" + URLEncoder.encode(MessageCube.getApplicationId(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        this.url = awsUrl;

    }

}
