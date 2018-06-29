package mobi.messagecube.sdk.openAPI;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.view.View;

import mobi.messagecube.sdk.com.ramotion.cardslider.CardSliderLayoutManager;
import mobi.messagecube.sdk.com.ramotion.cardslider.CardSnapHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import mobi.messagecube.sdk.R;
import mobi.messagecube.sdk.cards.SliderAdapter;

public class SearchCardSliderActivity extends Activity {

    // TODO: Register to receive notifications for FORWARD messages.
    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String forwardMessage = intent.getStringExtra("forwardMessage");
            onBackPressed();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("send-forward-message-to-compose-view"));

        ArrayList<String> data= getIntent().getStringArrayListExtra("jsonStrings");
        int initializedPosition = getIntent().getIntExtra("index", 0);

        setContentView(R.layout.dialog_activity);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        float density = getResources().getDisplayMetrics().density;
        int defaultCardWidth = (int)(380.0F * density);
        int defaultActiveCardLeft = (int)(10.0F * density);
        float defaultCardsGap = 12.0F * density;
        CardSliderLayoutManager cardSliderLayoutManager = new CardSliderLayoutManager(defaultActiveCardLeft, defaultCardWidth, defaultCardsGap);

        recyclerView.setLayoutManager(cardSliderLayoutManager);
        final CardSliderLayoutManager layoutManger = (CardSliderLayoutManager) recyclerView.getLayoutManager();


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onActiveCardChange();
                }
            }

            private void onActiveCardChange() {
                final int pos = layoutManger.getActiveCardPosition();
                if (pos == RecyclerView.NO_POSITION || pos == 0) {
                    return;
                }

                // onActiveCardChange(pos);
            }
        });

        String[] stockArr = new String[data.size()];
        stockArr = data.toArray(stockArr);

        Boolean isForward = getIntent().getBooleanExtra("isForward", false);
        String[] forwardItem = new String[1];
        if (isForward) {
            forwardItem[0] = stockArr[initializedPosition];
        }

        final String[] finalStockArr;
        if (isForward) {
            finalStockArr = forwardItem;
        } else {
            finalStockArr = stockArr;
        }

        final SliderAdapter sliderAdapter = new SliderAdapter(finalStockArr, finalStockArr.length, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("OnClickListener ....");
                final int pos = layoutManger.getActiveCardPosition();
                String baseClassString = finalStockArr[pos];

                try{
                    final JSONObject jsonObj = new JSONObject(baseClassString);
                    BaseClass baseClass = new BaseClass(jsonObj);

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(baseClass.url));
                    startActivity(browserIntent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        recyclerView.setAdapter(sliderAdapter);
        if (!isForward) {
            layoutManger.scrollToPosition(initializedPosition);
        }

        new CardSnapHelper().attachToRecyclerView(recyclerView);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

}
