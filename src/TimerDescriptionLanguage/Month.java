package TimerDescriptionLanguage;

/**
 * Created by mcochrane on 17/12/16.
 */
public enum Month {
    JANUARY("january", "jan", 1),
    FEBRUARY("february", "feb", 2),
    MARCH("march", "mar", 3),
    APRIL("april", "apr", 4),
    MAY("may", "may", 5),
    JUNE("june", "june", 6),
    JULY("july", "july", 7),
    AUGUST("august", "aug", 8),
    SEPTEMBER("september", "sep", 9),
    OCTOBER("october", "oct", 10),
    NOVEMBER("november", "nov", 11),
    DECEMBER("december", "dev", 12);


    private final String longName;
    private final String shortName;
    private final int number;

    Month(final String longName, final String shortName, final int number) {
        this.longName = longName;
        this.shortName = shortName;
        this.number = number;
    }

    public static Month fromNumber(String number) {
        Month[] months = values();

        for (int i = 0; i < months.length; i++) {
            if (number.equals(months[i].number)) return months[i];
        }

        return null;
    }

    public static Month fromString(String str) {
        Month result = fromShortName(str);
        if (result == null) result = fromLongName(str);
        return result;
    }

    public static Month fromLongName(String longName) {
        Month[] months = values();

        for (int i = 0; i < months.length; i++) {
            if (longName.equals(months[i].longName)) return months[i];
        }

        return null;
    }

    public static Month fromShortName(String shortName) {
        Month[] months = values();

        for (int i = 0; i < months.length; i++) {
            if (shortName.equals(months[i].shortName)) return months[i];
        }

        return null;
    }

    @Override
    public String toString() {
        return longName;
    }

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }

    public int getNumber() {
        return number;
    }

    public static String[] getLongNames() {
        Month[] months = values();
        String[] longNames = new String[months.length];

        for (int i = 0; i < months.length; i++) {
            longNames[i] = months[i].longName;
        }

        return longNames;
    }

    public static String[] getShortNames() {
        Month[] months = values();
        String[] shortNames = new String[months.length];

        for (int i = 0; i < months.length; i++) {
            shortNames[i] = months[i].longName;
        }

        return shortNames;
    }
}
