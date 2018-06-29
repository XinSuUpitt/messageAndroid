package mobi.messagecube.sdk.database;

public class MessageItemModal {
    // Database schema
    public static final String TABLE_NAME = "message_items";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SENDER_NUMBER = "senderNumber";
    public static final String COLUMN_DATE_STRING = "dateString";
    public static final String COLUMN_RESULT = "result";

    public static String[] ALL_COLUMNS = {
            COLUMN_ID,
            COLUMN_SENDER_NUMBER,
            COLUMN_DATE_STRING,
            COLUMN_RESULT
    };

    // Database creation sql statement
    public static final String CREATE_TABLE = "create table "
            + TABLE_NAME + "( "
            + COLUMN_ID  + " integer primary key autoincrement, "
            + COLUMN_SENDER_NUMBER + " text not null, "
            + COLUMN_DATE_STRING + " text not null, "
            + COLUMN_RESULT + " text not null"
            + ");";

    private long id;
    private String senderNumber;
    private String dateString;
    private String result;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(String senderNumber) {
        this.senderNumber = senderNumber;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return result;
    }
}
