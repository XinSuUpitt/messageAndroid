package mobi.messagecube.sdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Database {

    // Database fields
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public Database(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public MessageItemModal createMessageItem(String senderNumber, String dateString, String result) {
        // Check senderNumber, dateString, and result
        if (senderNumber == null || dateString == null || result == null) {
            return null;
        }

        ContentValues values = new ContentValues();
        values.put(MessageItemModal.COLUMN_SENDER_NUMBER, senderNumber);
        values.put(MessageItemModal.COLUMN_DATE_STRING, dateString);
        values.put(MessageItemModal.COLUMN_RESULT, result);
        long insertId = database.insert(MessageItemModal.TABLE_NAME, null, values);

        // Why we need to query it?
        Cursor cursor = database.query(MessageItemModal.TABLE_NAME,
                MessageItemModal.ALL_COLUMNS, MessageItemModal.COLUMN_ID + " = " + insertId,
                null, null, null, null);
        cursor.moveToFirst();
        MessageItemModal newMessageItemModal = cursorToMessageItem(cursor);
        cursor.close();
        return newMessageItemModal;
    }

    public MessageItemModal getMessageItem(String senderNumber, String dateString) {
        // Check senderNumber and dataString
        if (senderNumber == null || dateString == null) {
            return null;
        }

        String whereClause = MessageItemModal.COLUMN_SENDER_NUMBER +" =? AND " + MessageItemModal.COLUMN_DATE_STRING + "=?";
        String[] whereArgs = new String[]{senderNumber, dateString};
        Cursor cursor = database.query(MessageItemModal.TABLE_NAME, MessageItemModal.ALL_COLUMNS, whereClause, whereArgs,
                null, null, null);

        if (cursor.getCount() == 0) {
            System.out.println("get cursor is 0 called");
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        MessageItemModal newMessageItemModal = cursorToMessageItem(cursor);
        cursor.close();
        return newMessageItemModal;
    }

    private MessageItemModal cursorToMessageItem(Cursor cursor) {
        MessageItemModal messageItemModal = new MessageItemModal();
        messageItemModal.setId(cursor.getLong(0));
        messageItemModal.setSenderNumber(cursor.getString(1));
        messageItemModal.setDateString(cursor.getString(2));
        messageItemModal.setResult(cursor.getString(3));
        return messageItemModal;
    }

    public void createImage(String imageUrl, Bitmap img) {
        // Check imageUrl and image
        if (imageUrl == null || img == null) {
            return;
        }

        // We did the compress in the cloud.
        // Compress the image
        /*
        int maxWidth = 100;
        int maxHeight = 100;

        int width = img.getWidth();
        int height = img.getHeight();
        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > 1) {
            finalWidth = (int) ((float)maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float)maxWidth / ratioBitmap);
        }
        Bitmap image = Bitmap.createScaledBitmap(img, finalWidth, finalHeight, true);
        */

        byte[] data = ImageModal.getBitmapAsByteArray(img);

        ContentValues values = new ContentValues();
        values.put(ImageModal.COLUMN_IMAGE_URL, imageUrl);
        values.put(ImageModal.COLUMN_IMAGE_DATA, data);
        long insertId = database.insert(ImageModal.TABLE_NAME, null, values);

        System.out.println("Create a image successfully. " + insertId);
    }

    public ImageModal getImage(String imageUrl) {
        // Check imageUrl
        if (imageUrl == null) {
            return null;
        }

        Cursor cursor = database.query(
              ImageModal.TABLE_NAME,
              ImageModal.ALL_COLUMNS,
              ImageModal.COLUMN_IMAGE_URL + " = " + "\""+ imageUrl + "\"",
              null, null, null, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        ImageModal newImageModal = cursorToImage(cursor);
        cursor.close();
        return newImageModal;
    }

    private ImageModal cursorToImage(Cursor cursor) {
        ImageModal imageModal = new ImageModal();
        imageModal.setId(cursor.getLong(0));
        imageModal.setImageUrl(cursor.getString(1));
        byte[] imgByte = cursor.getBlob(2);
        try {
            Bitmap image = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            imageModal.setImage(image);
        } catch (Exception e) {
            System.out.println("Failed to decodeByteArray");
            return null;
        }

        return imageModal;
    }
}
