package mobi.messagecube.sdk;

import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

public class MessageItemParser {
    public static int getSearchTypeFromText(String body, int boxId, boolean isSms) {
        if (isSms) {
            if (boxId == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX || boxId == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_ALL) {
                // Incoming
                if (body == null) {
                    return MessageItemSearchType.IN_NORMAL;
                }

                if (body.startsWith(SearchKeyword.image)) {
                    return MessageItemSearchType.IN_IMAGE;

                } else if (isSimpleWeb(body)) {
                    return MessageItemSearchType.IN_WEB;

                } else if (body.startsWith(SearchKeyword.restaurant)) {
                    return MessageItemSearchType.IN_RESTAURANT;

                } else if (body.startsWith(SearchKeyword.location)) {
                    return MessageItemSearchType.IN_LOCATION;

                } else if (isComplicatedWeb(body)) {
                    return MessageItemSearchType.IN_COMPLICATED_WEB;

                } else if (isForwardMessage(body)) {
                    return MessageItemSearchType.IN_FORWARD_MESSAGE;

                } else {
                    return MessageItemSearchType.IN_NORMAL;
                }
            } else {
                // Outcoming
                // In and out are different.
                if (body == null) {
                    return MessageItemSearchType.OUT_NORMAL;
                }

                if (body.startsWith(SearchKeyword.image)) {
                    return MessageItemSearchType.OUT_IMAGE;

                } else if (isSimpleWeb(body)) {
                    return MessageItemSearchType.OUT_WEB;

                } else if (body.startsWith(SearchKeyword.restaurant)) {
                    return MessageItemSearchType.OUT_RESTAURANT;

                } else if (body.startsWith(SearchKeyword.location)) {
                    return MessageItemSearchType.OUT_LOCATION;

                } else if (isComplicatedWeb(body)) {
                    return MessageItemSearchType.OUT_COMPLICATED_WEB;

                } else if (isForwardMessage(body)) {
                    return MessageItemSearchType.OUT_FORWARD_MESSAGE;

                } else {
                    return MessageItemSearchType.OUT_NORMAL;
                }
            }
        } else {
            if (boxId == Telephony.Mms.MESSAGE_BOX_ALL || boxId == Telephony.Mms.MESSAGE_BOX_INBOX) {
                return MessageItemSearchType.IN_NORMAL;
            } else {
                return MessageItemSearchType.OUT_NORMAL;
            }
        }
    }

    public static boolean isSimpleWeb(String body) {
        if (body == null) {
            return false;
        }

        if (!body.toLowerCase().startsWith(SearchKeyword.web.toLowerCase())) {
            return false;
        }

        String tmp = body.trim().toLowerCase();
        tmp = tmp.replaceFirst(SearchKeyword.web.toLowerCase(), "");

        if (tmp.trim().length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isComplicatedWeb(String body) {
        if (body == null) {
            return false;
        }

        if (body.startsWith(SearchKeyword.webMagicWord) && body.endsWith(" More at: goo.gl/m5s14B")) {
            return true;
        }
        return false;
    }

    public static String getSearchKeywordFromText(String body) {
        if (body == null) {
            return "";
        }

        // Example:
        // @hello world 1. www.google.com More at: goo.gl/m5s14B
        // @hello world : 94086 1. www.google.com More at: goo.gl/m5s14B

        if (isComplicatedWeb(body)) {
            String tmp = body;

            tmp = tmp.replaceFirst(SearchKeyword.webMagicWord, "");
            tmp = tmp.replaceAll(" More at: goo.gl/m5s14B", "");

            String reg = "\\s1\\.\\s";
            String[] arr = tmp.split(reg);

            // At least two lines
            if (arr.length < 2) {
                return "";
            }

            String token = arr[0];
            if (token == null || token.length() == 0) {
                return "";
            }

            // Try to get the postal code
            String[] locations = token.split("\\s*:\\s");
            if (locations.length == 2) {

                // String postalCode = locations[1];
                // if (TextUtils.isDigitsOnly(postalCode)) {
                //     return locations[0];
                // }

                return locations[0];
            }

            return token;

        } else if (isSimpleWeb(body)) {

            // Example
            // search: sunnyvale or search:sunnyvale
            //
            // All chars are transfered to lower case.

            String tmp = body.trim().toLowerCase();
            tmp = tmp.replaceFirst(SearchKeyword.web.toLowerCase(), "");

            if (tmp.trim().length() == 0) {
                return "";
            }
            return tmp.trim();

        } else {
            return "";
        }
    }

    // TODO: I don't remember why we don't use this functino anymore.
    public static String getSearchPostalCodeFromText(String body) {
        if (body == null) {
            return "";
        }

        // Example:
        // @hello world : 94086 1. hello world www.google.com More at: goo.gl/m5s14B

        if (isComplicatedWeb(body)) {
            String tmp = body;

            tmp = tmp.replaceFirst(SearchKeyword.webMagicWord, "");
            tmp = tmp.replaceAll(" More at: goo.gl/m5s14B", "");

            String reg = "\\s1\\.\\s";
            String[] arr = tmp.split(reg);

            // At least two lines
            if (arr.length < 2) {
                return "";
            }

            String token = arr[0];
            if (token == null || token.length() == 0) {
                return "";
            }

            // Try to get the postal code
            String[] locations = token.split("\\s*:\\s");
            if (locations.length == 2) {
                String postalCode = locations[1];
                if (TextUtils.isDigitsOnly(postalCode)) {

                    return postalCode;
                }
            }

            return "";

        } else {
            return null;
        }
    }

    // TODO: Forward message
    public static boolean isForwardMessage(String body) {
        if (body == null) {
            return false;
        }

        if (body.startsWith(SearchKeyword.webMagicWord)) {

            // The regular expression pattern is
            // 1. 2. 3. 4.
            // split the search query

            String reg = "\\s[0-9]+\\.\\s";
            String[] arr = body.split(reg);
            if (arr.length == 2) {
                return true;
            }
        }
        return false;
    }

    public static String getSearchKeywordFromForwardMessage(String body) {
        if (body == null) {
            return "";
        }

        // Example:
        // @hello world : 94086 1. hello world www.google.com

        if (isForwardMessage(body)) {
            String tmp = body;

            tmp = tmp.replaceFirst(SearchKeyword.webMagicWord, "");

            String reg = "\\s[0-9]+\\.\\s";
            String[] arr = tmp.split(reg);

            // At least two lines
            if (arr.length != 2) {
                return "";
            }

            String token = arr[0];
            if (token == null || token.length() == 0) {
                return "";
            }

            // Try to get the postal code
            String[] locations = token.split("\\s*:\\s");
            if (locations.length == 2) {

                // String postalCode = locations[1];
                // if (TextUtils.isDigitsOnly(postalCode)) {
                //     return locations[0];
                // }

                return locations[0];
            }

            return token;
        } else {
            return "";
        }
    }

    public static String getSearchPostalCodeFromForwardMessage(String body) {
        if (body == null) {
            return "";
        }

        // Example:
        // @hello world: 94086 1. hello world www.google.com
        // @sushi : sunnyvale 1. Tanto Japanese Restaurant: 1063 E El Camino Real, Sunnyvale, CA 94087

        if (isForwardMessage(body)) {
            String tmp = body;

            tmp = tmp.replaceFirst(SearchKeyword.webMagicWord, "");

            String reg = "\\s[0-9]+\\.\\s";
            String[] arr = tmp.split(reg);

            // At least two lines
            if (arr.length != 2) {
                return "";
            }

            String token = arr[0];
            if (token == null || token.length() == 0) {
                return "";
            }

            // Try to get the postal code
            String[] locations = token.split("\\s*:\\s");
            if (locations.length == 2) {
                String postalCode = locations[1];

                // We don't need to check the digits anymore...
                // if (TextUtils.isDigitsOnly(postalCode)) {
                //     return postalCode;
                // }

                return postalCode;
            }

            return "";

        } else {
            return null;
        }

    }

    public static int getForwardIndex(String body) {
        if (body == null) {
            return 0;
        }

        if (isForwardMessage(body)) {
            try {
                String tmp = body.toLowerCase();

                tmp = tmp.replaceFirst(SearchKeyword.webMagicWord, "");
                String reg = "\\s[0-9]+\\.\\s";
                String[] arr = tmp.split(reg);

                // At least two lines
                if (arr.length != 2) {
                    return 0;
                }

                String token = body.toLowerCase().replaceFirst(SearchKeyword.webMagicWord, "");
                token = token.replaceFirst(arr[0], "");
                token = token.replaceAll(arr[1], "");
                token = token.replaceAll("\\.", "");
                token = token.trim();

                // It's from 1 - 10
                if (TextUtils.isDigitsOnly(token)) {
                    int digitToken = Integer.parseInt(token);
                    return digitToken;
                }

                String[] items = token.split(" ");
                if (items.length > 1) {
                    String firstItem = items[0].trim();
                    if (TextUtils.isDigitsOnly(firstItem)) {
                        return Integer.parseInt(firstItem);
                    }
                }

                return 0;
            } catch (Exception e) {
                Log.e("MessageItemParser", "Failed to parse getForwardIndex");
                return 0;
            }
        } else {
            return 0;
        }
    }

}
