package mobi.messagecube.sdk.database;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import java.io.ByteArrayOutputStream;

public class ImageModal {
    // Database schema
    public static final String TABLE_NAME = "images";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_IMAGE_DATA = "image_data";

    public static String[] ALL_COLUMNS = {
            COLUMN_ID,
            COLUMN_IMAGE_URL,
            COLUMN_IMAGE_DATA
    };

    // Database creation sql statement
    public static final String CREATE_TABLE = "create table "
            + TABLE_NAME + "( "
            + COLUMN_ID  + " integer primary key autoincrement, "
            + COLUMN_IMAGE_URL + " text not null, "
            + COLUMN_IMAGE_DATA + " blob"
            + ");";

    private long id;
    private String imageUrl;
    private Bitmap image;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }
}
