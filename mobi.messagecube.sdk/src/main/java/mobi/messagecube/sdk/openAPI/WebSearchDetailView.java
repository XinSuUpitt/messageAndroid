package mobi.messagecube.sdk.openAPI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import mobi.messagecube.sdk.android.MessageCube;
import mobi.messagecube.sdk.bingsearch.WebSearchView;

import mobi.messagecube.sdk.R;

public class WebSearchDetailView {



    public static void click(final ViewGroup pointer,
                             WebSearchView webSearchView,
                             final BaseClass webSearchResult,
                             final ArrayList<String> jsonStrings,
                             MessageType messageType) {

        clickAndShowCardslider(pointer, webSearchResult, jsonStrings, messageType);
        // clickWebSearchResult(pointer, webSearchView, webSearchResult);
    }

    private static void clickWebSearchResult(final ViewGroup pointer,
                                            WebSearchView webSearchView,
                                            final BaseClass webSearchResult,
                                             MessageType messageType) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(pointer.getContext());

        alertDialogBuilder.setNeutralButton("FORWARD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String forwardMessage = webSearchResult.forwardMessage;
                Intent myIntent = new Intent("send-forward-message-to-compose-view");
                myIntent.putExtra("forwardMessage", forwardMessage);
                LocalBroadcastManager.getInstance(pointer.getContext()).sendBroadcast(myIntent);
            }
        });

        alertDialogBuilder.setPositiveButton("Details", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Stays in the messagecube Android app
                // Intent myIntent = new Intent(mContext, WebViewActivity.class);
                // myIntent.putExtra("urlString", webSearchResult.url);
                // mContext.startActivity(myIntent);

                // Jump to Android's web browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webSearchResult.url));
                pointer.getContext().startActivity(browserIntent);
            }
        });

        LayoutInflater inflater = (LayoutInflater) pointer.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialoglayout = inflater.inflate(R.layout.search_item_card, null);


        // Image View
        ImageView imageView = (ImageView) dialoglayout.findViewById(R.id.imageView);
        ImageView profileView = (ImageView) dialoglayout.findViewById(R.id.profile_image);
        if (webSearchResult.imageUrl.length() > 0) {
            imageView.setImageDrawable(webSearchView.mImageView.getDrawable());
        }
        if (webSearchResult.optional.get("profile_image_url") != null) {
            profileView.setImageDrawable(webSearchView.mProfileImageView.getDrawable());
        } else {
            profileView.setVisibility(View.GONE);
        }

        // Text View
        TextView textView = (TextView) dialoglayout.findViewById(R.id.name);
        TextView displayView = (TextView) dialoglayout.findViewById(R.id.displayUrl);
        TextView snippetView = (TextView) dialoglayout.findViewById(R.id.snippet);

        textView.setText(webSearchResult.name);
        snippetView.setText(webSearchResult.snippet);
        displayView.setText(webSearchResult.displayUrl);


        // Setup Yelp UI
        RatingBar ratingBar = (RatingBar) dialoglayout.findViewById(R.id.ratingBar);
        TextView reviewCountView = (TextView) dialoglayout.findViewById(R.id.reviewCount);
        TextView yelpPriceTextView = (TextView) dialoglayout.findViewById(R.id.yelpPrice);
        TextView categoryTextView = (TextView) dialoglayout.findViewById(R.id.category);
        TextView reviewView = (TextView) dialoglayout.findViewById(R.id.reviews);

        //setup amazon UI
        TextView priceTextView = (TextView) dialoglayout.findViewById(R.id.price);
        TextView salePriceTextView = (TextView) dialoglayout.findViewById(R.id.salePrice);


        // Add click actions
        clickDetailActionToView(pointer, imageView, webSearchResult.url);
        clickDetailActionToView(pointer, profileView, webSearchResult.url);

        clickDetailActionToView(pointer, textView, webSearchResult.url);
        clickDetailActionToView(pointer, snippetView, webSearchResult.url);
        clickDetailActionToView(pointer, displayView, webSearchResult.url);

        clickDetailActionToView(pointer, ratingBar, webSearchResult.url);
        clickDetailActionToView(pointer, reviewCountView, webSearchResult.url);
        clickDetailActionToView(pointer, yelpPriceTextView, webSearchResult.url);
        clickDetailActionToView(pointer, categoryTextView, webSearchResult.url);
        clickDetailActionToView(pointer, reviewView, webSearchResult.url);

        clickDetailActionToView(pointer, priceTextView, webSearchResult.url);
        clickDetailActionToView(pointer, salePriceTextView, webSearchResult.url);



        if (webSearchResult.apiSource.equals("yelp")) {
            try {
                String ratingString = webSearchResult.optional.get("rating").toString();
                float rating = Float.valueOf(ratingString.trim());
                ratingBar.setRating(rating);
                ratingBar.setIsIndicator(true);
            } catch (Exception e) {
                System.out.println("Yelp rating NumberFormatException: " + e.getMessage());
                ratingBar.setVisibility(View.GONE);
            }

            try {
                String reviewCountString = webSearchResult.optional.get("reviewCount").toString();
                reviewCountView.setText(reviewCountString);
            } catch (Exception e) {
                System.out.println("Yelp reviewCount NumberFormatException: " + e.getMessage());
                reviewCountView.setVisibility(View.GONE);
            }

            try {
                String yelpPriceString = webSearchResult.optional.get("price").toString();
                yelpPriceTextView.setText(yelpPriceString);
            } catch (Exception e) {
                System.out.println("Price rating NumberFormatException: " + e.getMessage());
                yelpPriceTextView.setVisibility(View.GONE);
            }

            try {
                Object categoriesObject = webSearchResult.optional.get("categories");
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

        } else if (webSearchResult.apiSource.equals("amazon")){
            ratingBar.setVisibility(View.GONE);
            categoryTextView.setVisibility(View.GONE);
            reviewView.setVisibility(View.GONE);
            snippetView.setVisibility(View.GONE);
            yelpPriceTextView.setVisibility(View.GONE);
            reviewCountView.setVisibility(View.GONE);

            if (webSearchResult.optional.containsKey("price")) {
                String price = webSearchResult.optional.get("price").toString();
                if (price != null) {
                    priceTextView.setText(price);
                }
            }

            if (webSearchResult.optional.containsKey("salePrice")) {
                String salePrice = webSearchResult.optional.get("salePrice").toString();
                if (salePrice != null) {
                    salePriceTextView.setText(salePrice);
                    priceTextView.setPaintFlags(webSearchView.mProductPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }

        } else if (webSearchResult.apiSource.equals("youtube")){
            ratingBar.setVisibility(View.GONE);
            snippetView.setVisibility(View.GONE);
            reviewView.setVisibility(View.GONE);
            priceTextView.setVisibility(View.GONE);
            salePriceTextView.setVisibility(View.GONE);
            reviewCountView.setVisibility(View.GONE);
            yelpPriceTextView.setVisibility(View.GONE);
        } else if (webSearchResult.apiSource.equals("twitter")){
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


        TextView nativeAdTitle = (TextView) dialoglayout.findViewById(R.id.native_ad_title);
        nativeAdTitle.setVisibility(View.INVISIBLE);

        // final MessagecubeMediaView native_ad_media = (MessagecubeMediaView) dialoglayout.findViewById(R.id.native_ad_media);
        // native_ad_media.setVisibility(View.INVISIBLE);

        TextView nativeResponseTitle = (TextView) dialoglayout.findViewById(R.id.sponsored_label);
        nativeResponseTitle.setVisibility(View.INVISIBLE);

        final com.facebook.ads.NativeAd nativeAd;

        // TODO: Store this Facebook Ad id in the cloud.
        nativeAd = new com.facebook.ads.NativeAd(pointer.getContext(), MessageCube.getFacebookNativeAdId());
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(com.facebook.ads.Ad ad, com.facebook.ads.AdError error) {
                // NativeAd error callback
                // System.out.println("Native onError in NativeAd " + error.getErrorMessage());
                // holder.mNativeAdContainer.setVisibility(View.GONE);

                // TODO: I think this is unsafe.
                // NativeAd.this.onError(ad, error);

                TextView nativeAdTitle = (TextView) dialoglayout.findViewById(R.id.native_ad_title);
                nativeAdTitle.setVisibility(View.INVISIBLE);


                // MessagecubeMediaView native_ad_media = (MessagecubeMediaView) dialoglayout.findViewById(R.id.native_ad_media);
                // native_ad_media.setVisibility(View.INVISIBLE);

                TextView nativeResponseTitle = (TextView) dialoglayout.findViewById(R.id.sponsored_label);
                nativeResponseTitle.setVisibility(nativeResponseTitle.INVISIBLE);
            }

            @Override
            public void onAdLoaded(com.facebook.ads.Ad ad) {
                // NativeAd loaded callback
                // System.out.println("Native onAdLoaded in NativeAd ");

                if (nativeAd != null) {
                    nativeAd.unregisterView();
                }

                // Create native UI using the ad metadata.
                // ImageView nativeAdIcon = (ImageView) dialoglayout.findViewById(R.id.native_ad_icon);

                TextView nativeAdTitle = (TextView) dialoglayout.findViewById(R.id.native_ad_title);
                nativeAdTitle.setVisibility(View.VISIBLE);

                TextView nativeResponseTitle = (TextView) dialoglayout.findViewById(R.id.sponsored_label);
                nativeResponseTitle.setVisibility(nativeResponseTitle.VISIBLE);

                LinearLayout myLayout = (LinearLayout) dialoglayout.findViewById(R.id.native_media_placeholder);
                myLayout.setVisibility(View.VISIBLE);

                MessagecubeMediaView nativeAdMedia = new MessagecubeMediaView(pointer.getContext());
                nativeAdMedia.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

                myLayout.addView(nativeAdMedia);

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


        // TODO: Disable the alertDialogBuilder now.
        alertDialogBuilder.setView(dialoglayout);
        alertDialogBuilder.show();
    }

    private static void clickDetailActionToView(final ViewGroup pointer, View view, String url) {
        view.setOnClickListener(new View.OnClickListener() {
            String url;

            @Override
            public void onClick(View v) {
                // TODO: We have a potential bug here.
                // if url is twitter.com ... Some Android apps couldn't open it.
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                pointer.getContext().startActivity(browserIntent);
            }

            public View.OnClickListener setParams(String url) {
                this.url = url;
                return this;
            }
        }.setParams(url));
    }

    private static void clickAndShowCardslider(final ViewGroup pointer,
                                              final BaseClass webSearchResult,
                                              final ArrayList<String> jsonStrings,
                                               MessageType messageType) {

        Intent newIntent = new Intent(pointer.getContext(), SearchCardSliderActivity.class);
        newIntent.putExtra("jsonStrings", jsonStrings);
        // BaseClass is from 1 - 10
        newIntent.putExtra("index", webSearchResult.index-1);
        newIntent.putExtra("isForward", messageType == MessageType.FORWARD);

        pointer.getContext().startActivity(newIntent);
    }
}
