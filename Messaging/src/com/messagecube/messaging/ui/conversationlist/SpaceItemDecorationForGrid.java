package com.messagecube.messaging.ui.conversationlist;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by suxin on 10/1/17.
 */

public class SpaceItemDecorationForGrid extends RecyclerView.ItemDecoration {
    private int space;

    public SpaceItemDecorationForGrid(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1 || parent.getChildAdapterPosition(view) == 2) {
            outRect.top = space;
        }

        if (parent.getChildAdapterPosition(view) % 3 == 1) {
            outRect.left = space / 2;
            outRect.right = space / 2;
        }
    }
}
