package com.messagecube.messaging.ui.mediapicker;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.InflateException;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.messagecube.messaging.R;
import com.messagecube.messaging.ui.contact.ContactPickerFragment;

import java.util.Random;

/**
 * Created by suxin on 10/12/17.
 */

public class VcardMediaChooser extends MediaChooser {
    /**
     * Initializes a new instance of the Chooser class
     *
     * @param mediaPicker The media picker that the chooser is hosted in
     */
    private View view;
    private TextView pickContact;
    private TextView vcard_quote_tv;
    private TextView whyweuse_tv;
    private ImageView icon_under_slogan;
    private RelativeLayout pick_contact_layout;


    VcardMediaChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
    }

    @Override
    protected View createView(ViewGroup container) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        try {
            view = (View) getLayoutInflater().inflate(R.layout.mediapicker_vcard_chooser, container, false);
            pickContact = (TextView) view.findViewById(R.id.pick_contact);
            vcard_quote_tv = (TextView) view.findViewById(R.id.vcard_quote_tv);
            whyweuse_tv = (TextView) view.findViewById(R.id.whyweuse_tv);
            icon_under_slogan = (ImageView) view.findViewById(R.id.icon_under_slogan);
            pick_contact_layout = (RelativeLayout) view.findViewById(R.id.pick_contact_layout);

            Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "blackjack.otf");
            vcard_quote_tv.setTypeface(typeface);
            whyweuse_tv.setTypeface(typeface);
            String[] strings = new String[]{getContext().getResources().getString(R.string.vcard_slogan_1),
                    getContext().getResources().getString(R.string.vcard_slogan_2),
                    getContext().getResources().getString(R.string.vcard_slogan_3),
                    getContext().getResources().getString(R.string.vcard_slogan_4),
                    getContext().getResources().getString(R.string.vcard_slogan_5),
                    getContext().getResources().getString(R.string.vcard_slogan_6),
                    getContext().getResources().getString(R.string.vcard_slogan_7),
                    getContext().getResources().getString(R.string.vcard_slogan_8),
                    getContext().getResources().getString(R.string.vcard_slogan_9),
                    getContext().getResources().getString(R.string.vcard_slogan_10),
            };
            Random random = new Random();
            vcard_quote_tv.setText(strings[random.nextInt(10)]);

            pick_contact_layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            pick_contact_layout.setBackgroundResource(R.drawable.pick_vcard_btn_d);
                            break;
                        case MotionEvent.ACTION_UP:
                            pick_contact_layout.setBackgroundResource(R.drawable.pick_vcard_btn);
                            Intent intent = new Intent(getContext(), VcardActivity.class);
                            getContext().startActivity(intent);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });

            icon_under_slogan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setIcon(R.drawable.ic_launcher)
                            .setMessage("Would you like to give a five star for Message Cube?")
                            .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    openPlayStore();
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
            });

        } catch (InflateException e) {

        }
        return view;
    }

    private void openPlayStore() {
        Uri uri = Uri.parse("market://details?id=" + getContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            getContext().startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            getContext().startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
        }
    }

    @Override
    public int getSupportedMediaTypes() {
        return MediaPicker.MEDIA_TYPE_VCARD;
    }

    @Override
    int getIconResource() {
        return R.drawable.ic_perm_contact_calendar_white_24dp;
    }

    @Override
    int getIconDescriptionResource() {
        return R.string.mediapicker_vcardDescription;
    }

    @Override
    int getActionBarTitleResId() {
        return 0;
    }
}
