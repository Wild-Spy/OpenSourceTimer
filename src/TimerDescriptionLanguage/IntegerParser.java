package TimerDescriptionLanguage;

import java.util.StringTokenizer;

/**
 * Created by mcochrane on 17/11/16.
 * Parses numbers to string.  For example:
 * "1" = 1
 * "10" = 10
 * "one" = 1
 * "twenty seven" = 27
 * etc.
 *
 * Or for ordinals:
 * "first" = 1
 * "second" = 2
 * "third" = 3
 * "eighteenth" = 18
 * etc.
 *
 */
public class IntegerParser {

    public enum ResultType {
        Number,
        Ordinal,
        DefiniteOrdinal;
    }

    public static class Result {
        public Integer value;
        public ResultType type;
        public Result(Integer value, ResultType type) {
            this.value = value;
            this.type = type;
        }
    }

    public static Result parseAny(String str) throws InvalidSyntaxException {
        try {
            return parseNumber(str);
        } catch (InvalidSyntaxException ex) {
        }
        try {
            return parseOrdinal(str);
        } catch (InvalidSyntaxException ex) {
        }
        throw new InvalidSyntaxException(str, 0, "Could not parse as number or ordinal");
    }

    public static Result parseNumber(String str) throws InvalidSyntaxException {
        Integer retVal = parseDigits(str);
        if (retVal != null) return new Result(retVal, ResultType.Number);
        retVal = parseWords(str);
        if (retVal != null) return new Result(retVal, ResultType.Number);
        throw new InvalidSyntaxException(str, 0, "Could not parse number");
    }

    private static final String[] ORDINALS = {"first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth",
            "eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth", "twentieth",
            "twenty first", "twenty second", "twenty third", "twenty fourth", "twenty fifth", "twenty sixth", "twenty seventh", "twenty eighth",
            "twenty ninth", "thirtieth", "thirty first"};

    //TODO: More complex ordinals (ie twenty third) (using 'replaceNumbers()')
    public static Result parseOrdinal(String str) throws InvalidSyntaxException {
        Result result;
        boolean isDefinite = false;
        str = str.toLowerCase().trim();

        if (str.startsWith("the ")) {
            isDefinite = true;
            str = str.substring("the ".length());
        }

        if (Character.isDigit(str.charAt(0))) {
            result = parseDigitOrdinal(str);
        } else {
            result = parseWordOrdinal(str);
        }

        if (isDefinite) result.type = ResultType.DefiniteOrdinal;
        return result;
    }

    private static Result parseWordOrdinal(String str) throws InvalidSyntaxException {
        for (int i = 0; i < ORDINALS.length; i++) {
            if (str.equals(ORDINALS[i])) {
                return new Result(i + 1, ResultType.Ordinal);
            }
        }
        throw new InvalidSyntaxException(str, 0, "Could not parse ordinal");
    }

    private static Result parseDigitOrdinal(String str) throws InvalidSyntaxException {
        //Assumes str has already been trim() and toLowerCase().

        Integer result;

        String suffix = str.substring(str.length()-2);
        String digits = str.substring(0, str.length()-2);
        char lastDigit = digits.charAt(digits.length()-1);

        try {
            result = Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            throw new InvalidSyntaxException(str, 0, "Could not parse digit ordinal");
        }

        String expectedSuffix;

        switch (lastDigit) {
            case '1': expectedSuffix = "st"; break;
            case '2': expectedSuffix = "nd"; break;
            case '3': expectedSuffix = "rd"; break;
            default: expectedSuffix = "th"; break;
        }

        if (result >= 11 && result <= 13) expectedSuffix = "th"; //special cases

        if (!suffix.equals(expectedSuffix)) {
            throw new InvalidSyntaxException(str, 0, "Could not parse ordinal");
        }

        return new Result(result, ResultType.Ordinal);

    }

    private static Integer parseDigits(String digits) {
        digits = digits.trim();
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer parseWords(String words) {
        words = words.toLowerCase().trim();
        try {
            return replaceNumbers(words);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static final String[] DIGITS = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
    private static final String[] TENS = {null, "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
    private static final String[] TEENS = {"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
    private static final String[] MAGNITUDES = {"hundred", "thousand", "million", "point"};
    private static final String[] ZERO = {"zero", "oh"};

    private static Integer replaceNumbers (String input) {
        input = input.replace(" and ", " ");
        String result = "";
        String[] decimal = input.split(MAGNITUDES[3]);
        String[] millions = decimal[0].split(MAGNITUDES[2]);
        boolean valid = false;

        for (int i = 0; i < millions.length; i++) {
            String[] thousands = millions[i].split(MAGNITUDES[1]);

            for (int j = 0; j < thousands.length; j++) {
                int[] triplet = {0, 0, 0};
                StringTokenizer set = new StringTokenizer(thousands[j]);

                if (set.countTokens() == 1) { //If there is only one token given in triplet
                    String uno = set.nextToken();
                    triplet[0] = 0;
                    for (int k = 0; k < DIGITS.length; k++) {
                        if (uno.equals(DIGITS[k])) {
                            triplet[1] = 0;
                            triplet[2] = k + 1;
                            valid = true;
                        }
                        if (uno.equals(TENS[k])) {
                            triplet[1] = k + 1;
                            triplet[2] = 0;
                            valid = true;
                        }
                    }
                    for (int k = 0; k < TEENS.length; k++) {
                        if (uno.equals(TEENS[k])) {
                            triplet[1] = 1;
                            triplet[2] = k;
                            valid = true;
                        }
                    }
                    if (uno.equals(ZERO[0])) {
                        triplet[2] = 0;
                        valid = true;
                    }
                } else if (set.countTokens() == 2) {  //If there are two tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    if (dos.equals(MAGNITUDES[0])) {  //If one of the two tokens is "hundred"
                        for (int k = 0; k < DIGITS.length; k++) {
                            if (uno.equals(DIGITS[k])) {
                                triplet[0] = k + 1;
                                triplet[1] = 0;
                                triplet[2] = 0;
                                valid = true;
                            }
                        }
                    }
                    else {
                        triplet[0] = 0;
                        for (int k = 0; k < DIGITS.length; k++) {
                            if (uno.equals(TENS[k])) {
                                triplet[1] = k + 1;
                                valid = true;
                            }
                            if (dos.equals(DIGITS[k])) {
                                triplet[2] = k + 1;
                                valid = true;
                            }
                        }
                    }
                } else if (set.countTokens() == 3) {  //If there are three tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    String tres = set.nextToken();
                    for (int k = 0; k < DIGITS.length; k++) {
                        if (uno.equals(DIGITS[k])) {
                            triplet[0] = k + 1;
                            valid = true;
                        }
                        if (tres.equals(DIGITS[k])) {
                            triplet[1] = 0;
                            triplet[2] = k + 1;
                            valid = true;
                        }
                        if (tres.equals(TENS[k])) {
                            triplet[1] = k + 1;
                            triplet[2] = 0;
                            valid = true;
                        }
                    }
                } else if (set.countTokens() == 4) {  //If there are four tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    String tres = set.nextToken();
                    String cuatro = set.nextToken();
                    for (int k = 0; k < DIGITS.length; k++) {
                        if (uno.equals(DIGITS[k])) {
                            triplet[0] = k + 1;
                            valid = true;
                        }
                        if (cuatro.equals(DIGITS[k])) {
                            triplet[2] = k + 1;
                            valid = true;
                        }
                        if (tres.equals(TENS[k])) {
                            triplet[1] = k + 1;
                            valid = true;
                        }
                    }
                } else {
                    triplet[0] = 0;
                    triplet[1] = 0;
                    triplet[2] = 0;
                }

                result = result + Integer.toString(triplet[0]) + Integer.toString(triplet[1]) + Integer.toString(triplet[2]);
            }
        }

        if (decimal.length > 1) {  //The number is a decimal
            StringTokenizer decimalDigits = new StringTokenizer(decimal[1]);
            result = result + ".";
            System.out.println(decimalDigits.countTokens() + " decimal digits");
            while (decimalDigits.hasMoreTokens()) {
                String w = decimalDigits.nextToken();
                System.out.println(w);

                if (w.equals(ZERO[0]) || w.equals(ZERO[1])) {
                    result = result + "0";
                    valid = true;
                }
                for (int j = 0; j < DIGITS.length; j++) {
                    if (w.equals(DIGITS[j])) {
                        result = result + Integer.toString(j + 1);
                        valid = true;
                    }
                }

            }
        }

        if (valid)
            return Integer.parseInt(result);
        else
            return null;
    }

}
