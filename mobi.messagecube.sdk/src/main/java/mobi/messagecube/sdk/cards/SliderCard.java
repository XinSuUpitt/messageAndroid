package mobi.messagecube.sdk.cards;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import mobi.messagecube.sdk.R;
import mobi.messagecube.sdk.android.MessageCube;
import mobi.messagecube.sdk.openAPI.BaseClass;
import mobi.messagecube.sdk.openAPI.MessagecubeMediaView;

import org.json.*;

import com.bumptech.glide.Glide;
import com.facebook.ads.NativeAd;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.inmobi.sdk.InMobiSdk;
import com.inmobi.sdk.InMobiSdk.LogLevel;
import com.squareup.picasso.Picasso;

import com.mixpanel.android.mpmetrics.MixpanelAPI;


public class SliderCard extends RecyclerView.ViewHolder {

    private static int viewWidth = 0;
    private static int viewHeight = 0;

    private final View additionalView;

    private final LinearLayout message_cube_search_detail_linear_layout;

    private final ImageView imageView;
    private final ImageView profileView;

    private final TextView textView;
    private final TextView displayView;
    private final TextView snippetView;

    private final RatingBar ratingBar;
    private final TextView reviewCountView;
    private final TextView yelpPriceTextView;
    private final TextView categoryTextView;
    private final TextView reviewView;

    private final TextView priceTextView;
    private final TextView salePriceTextView;

    private final Button forwardButton;


    private final TextView nativeAdTitle;
    private final TextView nativeResponseTitle;

    // Facebook native ads
    private final LinearLayout facebookNativeMediaLayout;
    private NativeAd nativeAd;

    // Inmobi native ads
    private InMobiNative inMobiNativeAd;
    private ImageView inmobiAdMedia;

    private MixpanelAPI mixpanel;


    public SliderCard(View itemView) {
        super(itemView);

        additionalView = (View) itemView.findViewById(R.id.additional_view);

        message_cube_search_detail_linear_layout = (LinearLayout) itemView.findViewById(R.id.message_cube_search_detail_linear_layout);

        // Image View
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
        profileView = (ImageView) itemView.findViewById(R.id.profile_image);

        // Text View
        textView = (TextView) itemView.findViewById(R.id.name);
        displayView = (TextView) itemView.findViewById(R.id.displayUrl);
        snippetView = (TextView) itemView.findViewById(R.id.snippet);


        // Setup Yelp UI
        ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
        reviewCountView = (TextView) itemView.findViewById(R.id.reviewCount);
        yelpPriceTextView = (TextView) itemView.findViewById(R.id.yelpPrice);
        categoryTextView = (TextView) itemView.findViewById(R.id.category);
        reviewView = (TextView) itemView.findViewById(R.id.reviews);

        //setup amazon UI
        priceTextView = (TextView) itemView.findViewById(R.id.price);
        salePriceTextView = (TextView) itemView.findViewById(R.id.salePrice);

        forwardButton = (Button) itemView.findViewById(R.id.forward_button);

        nativeAdTitle = (TextView) itemView.findViewById(R.id.native_ad_title);
        nativeResponseTitle = (TextView) itemView.findViewById(R.id.sponsored_label);
        facebookNativeMediaLayout = (LinearLayout) itemView.findViewById(R.id.native_media_placeholder);

        inmobiAdMedia = (ImageView) itemView.findViewById(R.id.inmobi_ad_media);


        mixpanel = MixpanelAPI.getInstance(itemView.getContext(), "1f9b4107070551d59f285c8cf2195ccd");

    }

    void setContent(final String baseClass) {
        try{
            final JSONObject jsonObj = new JSONObject(baseClass);
            if (viewWidth == 0) {
                itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        viewWidth = itemView.getWidth();
                        viewHeight = itemView.getHeight();

                        // loadBitmap(resId);
                        setUI(new BaseClass(jsonObj));
                    }
                });
            } else {
                // loadBitmap(resId);
                setUI(new BaseClass(jsonObj));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void clearContent() {

    }

    private void setUI(final BaseClass baseClass) {
        additionalView.setVisibility(View.INVISIBLE);

        if (baseClass.imageUrl.length() > 0) {
            // imageView.setImageDrawable(baseClass.mImageView.getDrawable());
            if (imageView.getContext() != null) {
                Glide.with(imageView.getContext()).load(baseClass.imageUrl).into(imageView);
            }
            imageView.setVisibility(View.VISIBLE);
        }
        if (baseClass.optional.get("profile_image_url") != null) {
            // profileView.setImageDrawable(webSearchView.mProfileImageView.getDrawable());
            if (profileView.getContext() != null) {
                Glide.with(profileView.getContext()).load(baseClass.optional.get("profile_image_url")).into(profileView);
            }
        } else {
            profileView.setVisibility(View.GONE);
        }

        textView.setText(baseClass.name);
        snippetView.setText(baseClass.snippet);
        displayView.setText(baseClass.displayUrl);

        if (baseClass.apiSource.equals("yelp")) {
            try {
                String ratingString = baseClass.optional.get("rating").toString();
                float rating = Float.valueOf(ratingString.trim());
                ratingBar.setRating(rating);
                ratingBar.setIsIndicator(true);
            } catch (Exception e) {
                System.out.println("Yelp rating NumberFormatException: " + e.getMessage());
                ratingBar.setVisibility(View.GONE);
            }

            try {
                String reviewCountString = baseClass.optional.get("reviewCount").toString();
                reviewCountView.setText(reviewCountString);
            } catch (Exception e) {
                System.out.println("Yelp reviewCount NumberFormatException: " + e.getMessage());
                reviewCountView.setVisibility(View.GONE);
            }

            try {
                String yelpPriceString = baseClass.optional.get("price").toString();
                yelpPriceTextView.setText(yelpPriceString);
            } catch (Exception e) {
                System.out.println("Price rating NumberFormatException: " + e.getMessage());
                yelpPriceTextView.setVisibility(View.GONE);
            }

            try {
                Object categoriesObject = baseClass.optional.get("categories");
                JSONArray categories = (JSONArray) categoriesObject;

                if (categories.length() > 0) {
                    categoryTextView.setText(categories.getString(0));
                } else {
                    categoryTextView.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                System.out.println("Yelp categories NumberFormatException: " + e.getMessage());
                categoryTextView.setVisibility(View.GONE);
            }

        } else if (baseClass.apiSource.equals("amazon")){
            ratingBar.setVisibility(View.GONE);
            categoryTextView.setVisibility(View.GONE);
            reviewView.setVisibility(View.GONE);
            snippetView.setVisibility(View.GONE);
            yelpPriceTextView.setVisibility(View.GONE);
            reviewCountView.setVisibility(View.GONE);

            if (baseClass.optional.containsKey("price")) {
                String price = baseClass.optional.get("price").toString();
                if (price != null) {
                    priceTextView.setText(price);
                }
            }

            if (baseClass.optional.containsKey("salePrice")) {
                String salePrice = baseClass.optional.get("salePrice").toString();
                if (salePrice != null) {
                    salePriceTextView.setText(salePrice);
                    priceTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }

        } else if (baseClass.apiSource.equals("youtube")){
            ratingBar.setVisibility(View.GONE);
            snippetView.setVisibility(View.GONE);
            reviewView.setVisibility(View.GONE);
            priceTextView.setVisibility(View.GONE);
            salePriceTextView.setVisibility(View.GONE);
            reviewCountView.setVisibility(View.GONE);
            yelpPriceTextView.setVisibility(View.GONE);
        } else if (baseClass.apiSource.equals("twitter")){
            ratingBar.setVisibility(View.GONE);

            reviewView.setVisibility(View.GONE);
            priceTextView.setVisibility(View.GONE);
            salePriceTextView.setVisibility(View.GONE);
            reviewCountView.setVisibility(View.GONE);
            yelpPriceTextView.setVisibility(View.GONE);
        }

        else {
            ratingBar.setVisibility(View.GONE);
            priceTextView.setVisibility(View.GONE);
            categoryTextView.setVisibility(View.GONE);
            reviewView.setVisibility(View.GONE);
            yelpPriceTextView.setVisibility(View.GONE);
        }


        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String forwardMessage = baseClass.forwardMessage;
                Intent myIntent = new Intent("send-forward-message-to-compose-view");
                myIntent.putExtra("forwardMessage", forwardMessage);
                LocalBroadcastManager.getInstance(imageView.getContext()).sendBroadcast(myIntent);
            }
        });

        // TODO: we need a method to choose which Ad to load.
        loadFacebookNativeAd();
    }

    private void loadFacebookNativeAd() {
        inmobiAdMedia.setVisibility(View.GONE);

        facebookNativeMediaLayout.setVisibility(View.INVISIBLE);

        nativeAdTitle.setVisibility(View.INVISIBLE);
        nativeResponseTitle.setVisibility(View.INVISIBLE);


        // AdSettings.addTestDevice("ea1550a55709f811b56604c4fcbe9cd9");


        // TODO: Store this Facebook Ad id in the cloud.
        nativeAd = new com.facebook.ads.NativeAd(imageView.getContext(), MessageCube.getFacebookNativeAdId());
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(com.facebook.ads.Ad ad, com.facebook.ads.AdError error) {
                // NativeAd error callback
                System.out.println("Facebook Native onError in NativeAd " + error.getErrorMessage());
                sendAdFailedToMixpanel("Facebook", error.getErrorMessage());


                // holder.mNativeAdContainer.setVisibility(View.GONE);

                // TODO: I think this is unsafe.
                // NativeAd.this.onError(ad, error);

                // MessagecubeMediaView native_ad_media = (MessagecubeMediaView) dialoglayout.findViewById(R.id.native_ad_media);
                // native_ad_media.setVisibility(View.INVISIBLE);

                loadInmobiNativeAd();
            }

            @Override
            public void onAdLoaded(com.facebook.ads.Ad ad) {
                // NativeAd loaded callback
                System.out.println("Facebook Native onAdLoaded in NativeAd ");

                sendAdSucceededToMixpanel("Facebook");

                if (nativeAd != null) {
                    nativeAd.unregisterView();
                }

                // Create native UI using the ad metadata.
                // ImageView nativeAdIcon = (ImageView) dialoglayout.findViewById(R.id.native_ad_icon);

                nativeAdTitle.setVisibility(View.VISIBLE);
                nativeResponseTitle.setVisibility(nativeResponseTitle.VISIBLE);
                facebookNativeMediaLayout.setVisibility(View.VISIBLE);

                MessagecubeMediaView nativeAdMedia = new MessagecubeMediaView(imageView.getContext());
                nativeAdMedia.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

                facebookNativeMediaLayout.addView(nativeAdMedia);

                // MessagecubeMediaView nativeAdMedia = (MessagecubeMediaView) dialoglayout.findViewById(R.id.native_ad_media);
                // nativeAdMedia.setVisibility(View.VISIBLE);

                // TextView nativeAdSocialContext = (TextView) dialoglayout.findViewById(R.id.native_ad_social_context);
                // TextView nativeAdBody = (TextView) dialoglayout.findViewById(R.id.native_ad_body);
                // Button nativeAdCallToAction = (Button) dialoglayout.findViewById(R.id.native_ad_call_to_action);

                // Set the text
                nativeAdTitle.setText(nativeAd.getAdTitle());
                nativeResponseTitle.setText("Sponsored");
                // nativeAdSocialContext.setText(NativeAd.nativeAd.getAdSocialContext());
                // nativeAdBody.setText(NativeAd.nativeAd.getAdBody());
                // nativeAdCallToAction.setText(NativeAd.nativeAd.getAdCallToAction());

                // Download and display the ad icon.
                // com.facebook.ads.NativeAd.Image adIcon = this.nativeAd.getAdIcon();
                // NativeAd.downloadAndDisplayImage(adIcon, NativeAd.nativeAd);

                // Download and display the cover image.
                nativeAdMedia.setNativeAd(nativeAd);

                // Add the AdChoices icon
                // LinearLayout adChoicesContainer = (LinearLayout) dialoglayout.findViewById(R.id.ad_choices_container);
                // AdChoicesView adChoicesView = new AdChoicesView(mContext, nativeAd, true);
                // adChoicesContainer.addView(adChoicesView);

                // Register the Title and CTA button to listen for clicks.
                // LinearLayout nativeAdContainer = (LinearLayout) dialoglayout.findViewById(R.id.native_ad_container);

                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(nativeAdTitle);
                clickableViews.add(nativeAdMedia);
                // clickableViews.add(nativeAdCallToAction);
                // nativeAd.registerViewForInteraction(nativeAdContainer,clickableViews);

                nativeAd.registerViewForInteraction(nativeAdMedia, clickableViews);
                nativeAd.registerViewForInteraction(nativeAdTitle, clickableViews);

            }

            @Override
            public void onAdClicked(com.facebook.ads.Ad ad) {
                // NativeAd clicked callback
                // System.out.println("Native onAdClicked in NativeAd .......");

                // NativeAd.this.onAdClicked(ad);
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

        nativeAd.loadAd();
    }


    private void loadInmobiNativeAd() {
        facebookNativeMediaLayout.setVisibility(View.GONE);

        inmobiAdMedia.setVisibility(View.INVISIBLE);

        nativeAdTitle.setVisibility(View.INVISIBLE);
        nativeResponseTitle.setVisibility(View.INVISIBLE);


        // https://support.inmobi.com/monetize/android-guidelines/
        // Use InMobi Account ID
        InMobiSdk.init(imageView.getContext(), "01ddf0beaa5d41599525f937d829263b");
        InMobiSdk.setLogLevel(LogLevel.DEBUG);

        InMobiNative.NativeAdListener adListener = new InMobiNative.NativeAdListener() {
            @Override
            public void onAdLoadSucceeded(final InMobiNative inMobiNative) {
                System.out.println("Inmobi onAdLoadSucceeded .... ");

                sendAdSucceededToMixpanel("InMobi");

                inmobiAdMedia.setVisibility(View.VISIBLE);

                nativeAdTitle.setVisibility(View.VISIBLE);
                nativeResponseTitle.setVisibility(View.VISIBLE);


                try {
                    JSONObject content = inMobiNative.getCustomAdContent();
                    String title = content.getString("title");
                    String landingURL = content.getString("landingURL");
                    String sreenshotUrl = content.getJSONObject("screenshots").getString("url");
                    System.out.println(sreenshotUrl);

                    nativeAdTitle.setText(title);
                    nativeAdTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            System.out.println("Click Inmobi ads.");
                            inMobiNative.reportAdClickAndOpenLandingPage();
                        }
                    });

                    Picasso.with(imageView.getContext()).load(sreenshotUrl).into(inmobiAdMedia);

                    inmobiAdMedia.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            System.out.println("Click Inmobi ads.");
                            inMobiNative.reportAdClickAndOpenLandingPage();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
                System.out.println("Inmobi onAdLoadFailed .... " + inMobiAdRequestStatus.getMessage());
                sendAdFailedToMixpanel("InMobi", inMobiAdRequestStatus.getMessage());
            }

            @Override
            public void onAdFullScreenDismissed(InMobiNative inMobiNative) {
                System.out.println("Inmobi onAdFullScreenDismissed .... ");
            }

            @Override
            public void onAdFullScreenWillDisplay(InMobiNative inMobiNative) {
                System.out.println("Inmobi onAdFullScreenWillDisplay .... ");
            }

            @Override
            public void onAdFullScreenDisplayed(InMobiNative inMobiNative) {
                System.out.println("Inmobi onAdFullScreenDisplayed .... ");
            }

            @Override
            public void onUserWillLeaveApplication(InMobiNative inMobiNative) {
                System.out.println("Inmobi onUserWillLeaveApplication .... ");
            }

            @Override
            public void onAdImpressed(@NonNull InMobiNative inMobiNative) {
                System.out.println("Inmobi onAdImpressed .... ");
            }

            @Override
            public void onAdClicked(@NonNull InMobiNative inMobiNative) {
                System.out.println("Inmobi onAdClicked .... ");
            }

            @Override
            public void onMediaPlaybackComplete(@NonNull InMobiNative inMobiNative) {
                System.out.println("Inmobi onMediaPlaybackComplete .... ");
            }

            @Override
            public void onAdStatusChanged(@NonNull InMobiNative inMobiNative) {
                System.out.println("Inmobi onAdStatusChanged .... ");
            }
        };

        inMobiNativeAd = new InMobiNative(imageView.getContext(), 1508949713852L, adListener);
        inMobiNativeAd.load();
    }


    private void sendAdSucceededToMixpanel(String adProvider) {
        try {
            JSONObject props = new JSONObject();
            props.put("Ad Provider", adProvider);
            mixpanel.track("Native Ad Succeeded", props);

        } catch (JSONException e) {
            Log.e("Mixpanel", "Unable to add properties to JSONObject", e);
        }
    }

    private void sendAdFailedToMixpanel(String adProvider, String errorMessage) {
        try {
            JSONObject props = new JSONObject();
            props.put("Ad Provider", adProvider);
            props.put("Error Message", errorMessage);
            mixpanel.track("Native Ad Failed", props);

        } catch (JSONException e) {
            Log.e("Mixpanel", "Unable to add properties to JSONObject", e);
        }
    }

}