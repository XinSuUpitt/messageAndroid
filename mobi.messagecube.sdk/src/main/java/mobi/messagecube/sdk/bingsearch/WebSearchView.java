package mobi.messagecube.sdk.bingsearch;

import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class WebSearchView {

    public TextView[] textViews = new TextView[2];

    public TextView nameTextView;
    public TextView snippetTextView;

    public ImageView mImageView;
    public ImageView mProfileImageView;

    public RatingBar mRatingBar;
    public TextView mProductPrice;
    public TextView mSaleProductPrice;
    public TextView mYelpCategory;

    public WebSearchView() {

    }

    // TODO: Why do we need this function?
    public void add() {
        textViews[0] = nameTextView;
        textViews[1] = snippetTextView;
    }

    public void clean() {
        if (nameTextView != null) {
            nameTextView.setText("");
        }

        if (snippetTextView != null) {
            snippetTextView.setText("");
        }

        if (mImageView != null) {
            mImageView.setVisibility(View.INVISIBLE);
        }

        if (mProfileImageView != null) {
            mProfileImageView.setVisibility(View.INVISIBLE);
        }

        if (mRatingBar != null) {
            mRatingBar.setVisibility(View.INVISIBLE);
        }

        if (mProductPrice != null) {
            mProductPrice.setVisibility(View.INVISIBLE);
        }

        if (mSaleProductPrice != null) {
            mSaleProductPrice.setVisibility(View.INVISIBLE);
        }

        if (mYelpCategory != null) {
            mYelpCategory.setVisibility(View.INVISIBLE);
        }
    }

}
