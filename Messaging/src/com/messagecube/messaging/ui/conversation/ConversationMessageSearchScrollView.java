package com.messagecube.messaging.ui.conversation;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.messagecube.messaging.R;

public class ConversationMessageSearchScrollView extends LinearLayout {

    public ConversationMessageSearchScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int horizontalSpace = MeasureSpec.getSize(widthMeasureSpec);

        ConversationMessageSearchScrollView mSearchScrollView =
                (ConversationMessageSearchScrollView) findViewById(R.id.search_scroll);

        mSearchScrollView.requestLayout();
    }
}
