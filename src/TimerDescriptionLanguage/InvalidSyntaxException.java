package TimerDescriptionLanguage;

/**
 * Created by mcochrane on 17/11/16.
 */
public class InvalidSyntaxException extends Exception {
    private String message;
    private String allCode;
    private int parseExceptionIndex;

    InvalidSyntaxException(String allCode, int parseExceptionIndex, String message) {
        this.message = message;
        this.allCode = allCode;
        this.parseExceptionIndex = parseExceptionIndex;
    }

    @Override
    public String toString() {
        return message + " \"" + allCode + "\".";
    }

    public String getMessage() { return message; }

    public String getCode() { return allCode; }

    public int getParseExceptionIndex() { return parseExceptionIndex; }

}
