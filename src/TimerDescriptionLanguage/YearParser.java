package TimerDescriptionLanguage;

import java.time.chrono.Era;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mcochrane on 17/12/16.
 */
public class YearParser {

    private static final String[] BCEras = {"bc", "b.c.", "bce", "b.c.e.", "before christ", "before common era"};
    private static final String[] ADEras = {"ad", "a.d.", "ce", "c.e.", "common era", "anno domini"};

    private static enum WordType {
        one,
        teen,
        ten,
        hundred,
        thousand;
    }

    private static class ValAndType {
        public Integer val;
        public WordType type;
        ValAndType(Integer val, WordType type) {
            this.val = val;
            this.type = type;
        }
    }

    private static final String[] ones = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
    private static final String[] teens = {null, "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
    private static final String[] tens = {"zero", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
    private static final String hundred = "hundred";
    private static final String thousand = "thousand";

    private YearParser() {}

    public static Integer parse(String text) throws InvalidSyntaxException {
        text = text.toLowerCase();

        Integer year = parseYearDigitsOnly(text);
        if (year != null) return year;

        year = parseYearDigitsAndEra(text);
        if (year != null) return year;

        year = parseYearWordsAD(text);
        if (year != null) return year;

        throw new InvalidSyntaxException(text, 0, "");
    }

    private static Integer parseYearDigitsOnly(String text) {
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer parseYearDigitsAndEra(String text) {
        boolean bcEra = false;

        if (ParseHelper.endsWithAnyOf(text, BCEras)) {
            text = ParseHelper.removeAnyFromEnd(text, BCEras);
            text = text.trim();
            bcEra = true;
        } else if (ParseHelper.endsWithAnyOf(text, ADEras)) {
            text = ParseHelper.removeAnyFromEnd(text, ADEras);
            text = text.trim();
            bcEra = false;
        }

        Integer i = parseYearDigitsOnly(text);
        if (i == null) return null;

        if (bcEra) {
            return -i;
        }
        else {
            return i;
        }
    }

    private static Integer parseYearWordsAD(String text) {
        text = text.replace(" and ", " ");
        List<String> wordList = new ArrayList<>(Arrays.asList(text.split(" ")));

        if (wordList.size() == 1) {
            return parseYearWordsLengthOne(wordList.get(0));
        } else if (wordList.size() == 2) {
            return parseYearWordsLengthTwo(wordList);
        } else if (wordList.size() == 3) {
            return parseYearWordsLengthThree(wordList);
        } else if (wordList.size() == 4) {
            return parseYearWordsLengthFour(wordList);
        }
        return null;
    }

    private static Integer parseYearWordsLengthFour(List<String> wordList) {
        ValAndType firstPart = getYearWordValAndType(wordList.get(0));
        if (firstPart == null) return null;
        ValAndType secondPart = getYearWordValAndType(wordList.get(1));
        if (secondPart == null) return null;
        ValAndType thirdPart = getYearWordValAndType(wordList.get(2));
        if (thirdPart == null) return null;
        ValAndType fourthPart = getYearWordValAndType(wordList.get(3));
        if (fourthPart == null) return null;

        Integer firstTwo = parseYearWordsLengthTwo(wordList.subList(0, 2));
        if (firstTwo == null) return null;
        Integer lastTwo = parseYearWordsLengthTwo(wordList.subList(2, 4));
        if (lastTwo == null) return null;

        if (secondPart.type == WordType.hundred && fourthPart.type == WordType.hundred) {
            return parseYearDigitsOnly(firstTwo.toString() + lastTwo.toString());
        } else if (secondPart.type == WordType.hundred || secondPart.type == WordType.thousand) {
            return firstTwo + lastTwo;
        } else {
            return parseYearDigitsOnly(firstTwo.toString() + lastTwo.toString());
        }
    }

    private static Integer parseYearWordsLengthThree(List<String> wordList) {
        ValAndType firstPart = getYearWordValAndType(wordList.get(0));
        if (firstPart == null) return null;
        ValAndType secondPart = getYearWordValAndType(wordList.get(1));
        if (secondPart == null) return null;
        ValAndType thirdPart = getYearWordValAndType(wordList.get(2));
        if (thirdPart == null) return null;

        if ((firstPart.type == WordType.ten || firstPart.type == WordType.teen) &&
                (secondPart.type == WordType.ten && thirdPart.type == WordType.one)) {
            Integer lastTwo = parseYearWordsLengthTwo(wordList.subList(1, 3));
            if (lastTwo == null) return null;
            return parseYearDigitsOnly(firstPart.val.toString() + lastTwo.toString());
        } else if (firstPart.type == WordType.ten && secondPart.type == WordType.one) {
            Integer firstTwo = parseYearWordsLengthTwo(wordList.subList(0, 2));
            if (firstTwo == null) return null;
            return parseYearDigitsOnly(firstTwo.toString() + thirdPart.val.toString());
        } else if (secondPart.type == WordType.hundred &&
                (firstPart.type == WordType.one || firstPart.type == WordType.teen)) {
            if (thirdPart.type == WordType.hundred || thirdPart.type == WordType.thousand) return null;
            return firstPart.val*100 + thirdPart.val;
        } else if (secondPart.type == WordType.thousand &&
                (firstPart.type == WordType.one
                        || firstPart.type == WordType.teen
                        || firstPart.type == WordType.ten)) {
            if (thirdPart.type == WordType.hundred || thirdPart.type == WordType.thousand) return null;
            return firstPart.val*1000 + thirdPart.val;
        }

        return null;
    }

    private static Integer parseYearWordsLengthTwo(List<String> wordList) {
        ValAndType firstPart = getYearWordValAndType(wordList.get(0));
        if (firstPart == null) return null;
        ValAndType secondPart = getYearWordValAndType(wordList.get(1));
        if (secondPart == null) return null;

        if (firstPart.type == WordType.one) {
            if (secondPart.type == WordType.hundred) {
                return firstPart.val*100;
            } else if (secondPart.type == WordType.thousand) {
                return firstPart.val*1000;
            } else {
                return null;
            }
        } else if (firstPart.type == WordType.ten) {
            if (secondPart.type == WordType.one) {
                return firstPart.val + secondPart.val;
            } else if (secondPart.type == WordType.teen ||
                    secondPart.type == WordType.ten) {
                return parseYearDigitsOnly(firstPart.val.toString() + secondPart.val.toString());
            } else if (secondPart.type == WordType.hundred) {
                return null; //ie ten hundred, fifty hundred, etc. doesn't make sense.
            } else if (secondPart.type == WordType.thousand) {
                return firstPart.val*1000;
            }
        } else if (firstPart.type == WordType.teen) {
            if (secondPart.type == WordType.one) {
                return null;
            } else if (secondPart.type == WordType.teen ||
                    secondPart.type == WordType.ten) {
                return parseYearDigitsOnly(firstPart.val.toString() + secondPart.val.toString());
            } else if (secondPart.type == WordType.hundred) {
                return firstPart.val*100;
            } else if (secondPart.type == WordType.thousand) {
                return firstPart.val*1000;
            }
        }

        return null;
    }

    private static Integer parseYearWordsLengthOne(String word) {
        ValAndType vat = getYearWordValAndType(word);
        if (vat == null) return null;
        if (vat.type == WordType.hundred || vat.type == WordType.thousand) return null;
        return vat.val;
    }

    private static ValAndType getYearWordValAndType(String word) {
        Integer foundIndex;

        foundIndex = ParseHelper.findStringInArray(word, ones);
        if (foundIndex != null) return new ValAndType(foundIndex, WordType.one);
        foundIndex = ParseHelper.findStringInArray(word, teens);
        if (foundIndex != null) return new ValAndType(foundIndex + 10, WordType.teen);
        foundIndex = ParseHelper.findStringInArray(word, tens);
        if (foundIndex != null) return new ValAndType(foundIndex * 10, WordType.ten);
        if (word.equals(hundred)) return new ValAndType(100, WordType.hundred);
        if (word.equals(thousand)) return new ValAndType(1000, WordType.thousand);
        return null;
    }

}