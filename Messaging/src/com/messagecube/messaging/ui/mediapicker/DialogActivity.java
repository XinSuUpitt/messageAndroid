package com.messagecube.messaging.ui.mediapicker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.messagecube.messaging.R;

/**
 * Created by suxin on 10/10/17.
 */

public class DialogActivity extends Activity implements View.OnClickListener {

    Button ok_btn, cancel_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        ok_btn = (Button) findViewById(R.id.ok_btn_id);
        cancel_btn = (Button) findViewById(R.id.cancel_btn_id);

        ok_btn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);
        finish();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ok_btn_id:

                this.finish();

                break;

            case R.id.cancel_btn_id:

                this.finish();

                break;
        }

    }


}
