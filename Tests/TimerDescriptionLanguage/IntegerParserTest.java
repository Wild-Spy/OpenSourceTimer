package TimerDescriptionLanguage;

import TimerDescriptionLanguage.IntegerParser;
import TimerDescriptionLanguage.InvalidSyntaxException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by mcochrane on 17/11/16.
 */
public class IntegerParserTest {

    @Test
    public void testParseNumber1() throws InvalidSyntaxException {
        assertEquals((int) IntegerParser.parseNumber("1").value, 1);
    }

    @Test
    public void testParseNumber2() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("2").value, 2);
    }

    @Test
    public void testParseNumber0() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("0").value, 0);
    }

    @Test
    public void testParseNumberWithPadding() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("   1    ").value, 1);
    }

    @Test
    public void testParseNumberZero() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("zero").value, 0);
    }

    @Test
    public void testParseNumberFive() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("five").value, 5);
    }

    @Test
    public void testParseNumberTwenty() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("twenty").value, 20);
    }

    @Test
    public void testParseNumberTen() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("ten").value, 10);
    }

    @Test
    public void testParseNumberTeens() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("eleven").value, 11);
        assertEquals((int)IntegerParser.parseNumber("twelve").value, 12);
        assertEquals((int)IntegerParser.parseNumber("thirteen").value, 13);
        assertEquals((int)IntegerParser.parseNumber("fourteen").value, 14);
        assertEquals((int)IntegerParser.parseNumber("fifteen").value, 15);
        assertEquals((int)IntegerParser.parseNumber("sixteen").value, 16);
        assertEquals((int)IntegerParser.parseNumber("seventeen").value, 17);
        assertEquals((int)IntegerParser.parseNumber("eighteen").value, 18);
        assertEquals((int)IntegerParser.parseNumber("nineteen").value, 19);
    }

    @Test
    public void testParseNumberComplex() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("one hundred twenty three").value, 123);
    }

    @Test
    public void testParseNumberComplexWithAnd() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("one hundred and twenty three").value, 123);
    }

    @Test
    public void testParseNumberIgnoreCase() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseNumber("tWeNTy").value, 20);
    }

    @Test(expectedExceptions = InvalidSyntaxException.class)
    public void testParseNumberInvalidThrowsException() throws InvalidSyntaxException {
        IntegerParser.parseNumber("INVALIDSTRING");
    }

    @Test
    public void testParseOrdinals() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseOrdinal("first").value,   1);
        assertEquals((int)IntegerParser.parseOrdinal("second").value,  2);
        assertEquals((int)IntegerParser.parseOrdinal("third").value,   3);
        assertEquals((int)IntegerParser.parseOrdinal("fourth").value,  4);
        assertEquals((int)IntegerParser.parseOrdinal("fifth").value,   5);
        assertEquals((int)IntegerParser.parseOrdinal("sixth").value,   6);
        assertEquals((int)IntegerParser.parseOrdinal("seventh").value, 7);
        assertEquals((int)IntegerParser.parseOrdinal("eighth").value,  8);
        assertEquals((int)IntegerParser.parseOrdinal("ninth").value,   9);
        assertEquals((int)IntegerParser.parseOrdinal("tenth").value,   10);
        assertEquals((int)IntegerParser.parseOrdinal("eleventh").value,      11);
        assertEquals((int)IntegerParser.parseOrdinal("twelfth").value,       12);
        assertEquals((int)IntegerParser.parseOrdinal("thirteenth").value,    13);
        assertEquals((int)IntegerParser.parseOrdinal("fourteenth").value,    14);
        assertEquals((int)IntegerParser.parseOrdinal("fifteenth").value,     15);
        assertEquals((int)IntegerParser.parseOrdinal("sixteenth").value,     16);
        assertEquals((int)IntegerParser.parseOrdinal("seventeenth").value,   17);
        assertEquals((int)IntegerParser.parseOrdinal("eighteenth").value,    18);
        assertEquals((int)IntegerParser.parseOrdinal("nineteenth").value,    19);
        assertEquals((int)IntegerParser.parseOrdinal("twentieth").value,     20);
        assertEquals((int)IntegerParser.parseOrdinal("twenty first").value,  21);
        assertEquals((int)IntegerParser.parseOrdinal("twenty second").value, 22);
        assertEquals((int)IntegerParser.parseOrdinal("twenty third").value,  23);
        assertEquals((int)IntegerParser.parseOrdinal("twenty fourth").value, 24);
//        assertEquals((int)IntegerParser.parseOrdinal("thirty fourth").value, 34);
//        assertEquals((int)IntegerParser.parseOrdinal("fifty third").value,   53);
//        assertEquals((int)IntegerParser.parseOrdinal("ninety ninth").value,  99);
//        assertEquals((int)IntegerParser.parseOrdinal("one hundred and seventeenth").value,   117);
    }

    @Test
    public void testParseOrdinalsDates() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseOrdinal("1st").value,   1);
        assertEquals((int)IntegerParser.parseOrdinal("2nd").value,   2);
        assertEquals((int)IntegerParser.parseOrdinal("3rd").value,   3);
        assertEquals((int)IntegerParser.parseOrdinal("4th").value,   4);
        assertEquals((int)IntegerParser.parseOrdinal("5th").value,   5);
        assertEquals((int)IntegerParser.parseOrdinal("6th").value,   6);
        assertEquals((int)IntegerParser.parseOrdinal("7th").value,   7);
        assertEquals((int)IntegerParser.parseOrdinal("8th").value,   8);
        assertEquals((int)IntegerParser.parseOrdinal("9th").value,   9);
        assertEquals((int)IntegerParser.parseOrdinal("10th").value, 10);
        assertEquals((int)IntegerParser.parseOrdinal("11th").value, 11);
        assertEquals((int)IntegerParser.parseOrdinal("12th").value, 12);
        assertEquals((int)IntegerParser.parseOrdinal("13th").value, 13);
        assertEquals((int)IntegerParser.parseOrdinal("14th").value, 14);
        assertEquals((int)IntegerParser.parseOrdinal("15th").value, 15);
        assertEquals((int)IntegerParser.parseOrdinal("16th").value, 16);
        assertEquals((int)IntegerParser.parseOrdinal("17th").value, 17);
        assertEquals((int)IntegerParser.parseOrdinal("18th").value, 18);
        assertEquals((int)IntegerParser.parseOrdinal("19th").value, 19);
        assertEquals((int)IntegerParser.parseOrdinal("20th").value, 20);
        assertEquals((int)IntegerParser.parseOrdinal("21st").value, 21);
        assertEquals((int)IntegerParser.parseOrdinal("22nd").value, 22);
        assertEquals((int)IntegerParser.parseOrdinal("23rd").value, 23);
        assertEquals((int)IntegerParser.parseOrdinal("24th").value, 24);
        assertEquals((int)IntegerParser.parseOrdinal("25th").value, 25);
        assertEquals((int)IntegerParser.parseOrdinal("26th").value, 26);
        assertEquals((int)IntegerParser.parseOrdinal("27th").value, 27);
        assertEquals((int)IntegerParser.parseOrdinal("28th").value, 28);
        assertEquals((int)IntegerParser.parseOrdinal("29th").value, 29);
        assertEquals((int)IntegerParser.parseOrdinal("30th").value, 30);
        assertEquals((int)IntegerParser.parseOrdinal("31st").value, 31);
        assertEquals((int)IntegerParser.parseOrdinal("107th").value, 107);
        assertEquals((int)IntegerParser.parseOrdinal("123rd").value, 123);
    }

    @Test
    public void testParseAny() throws InvalidSyntaxException {
        assertEquals((int)IntegerParser.parseAny("first").value,   1);
        assertEquals((int)IntegerParser.parseAny("second").value,  2);
        assertEquals((int)IntegerParser.parseAny("third").value,   3);
        assertEquals((int)IntegerParser.parseAny("fourth").value,  4);
        assertEquals((int)IntegerParser.parseAny("fifth").value,   5);
        assertEquals((int)IntegerParser.parseAny("sixth").value,   6);
        assertEquals((int)IntegerParser.parseAny("seventh").value, 7);
        assertEquals((int)IntegerParser.parseAny("eighth").value,  8);
        assertEquals((int)IntegerParser.parseAny("ninth").value,   9);
        assertEquals((int)IntegerParser.parseAny("tenth").value,   10);
        assertEquals((int)IntegerParser.parseAny("eleventh").value,      11);
        assertEquals((int)IntegerParser.parseAny("twelfth").value,       12);
        assertEquals((int)IntegerParser.parseAny("thirteenth").value,    13);
        assertEquals((int)IntegerParser.parseAny("fourteenth").value,    14);
        assertEquals((int)IntegerParser.parseAny("fifteenth").value,     15);
        assertEquals((int)IntegerParser.parseAny("sixteenth").value,     16);
        assertEquals((int)IntegerParser.parseAny("seventeenth").value,   17);
        assertEquals((int)IntegerParser.parseAny("eighteenth").value,    18);
        assertEquals((int)IntegerParser.parseAny("nineteenth").value,    19);
        assertEquals((int)IntegerParser.parseAny("twentieth").value,     20);
        assertEquals((int)IntegerParser.parseAny("twenty first").value,  21);
        assertEquals((int)IntegerParser.parseAny("twenty second").value, 22);
        assertEquals((int)IntegerParser.parseAny("twenty third").value,  23);
        assertEquals((int)IntegerParser.parseAny("twenty fourth").value, 24);

        assertEquals((int)IntegerParser.parseAny("thirty four").value, 34);
        assertEquals((int)IntegerParser.parseAny("fifty three").value,   53);
        assertEquals((int)IntegerParser.parseAny("99").value,  99);
        assertEquals((int)IntegerParser.parseAny("one hundred and seventeen").value,   117);
        assertEquals((int)IntegerParser.parseAny("one thousand two hundred and seventeen").value,   1217);
    }

    @Test(expectedExceptions = InvalidSyntaxException.class)
    public void testParseBrokenTensShouldThrowException() throws InvalidSyntaxException {
        IntegerParser.parseAny("twenty cat");
    }

    @Test(expectedExceptions = InvalidSyntaxException.class)
    public void testParseBrokenHundredsShouldThrowException() throws InvalidSyntaxException {
        IntegerParser.parseAny("one hundred and cat");
    }

    @Test(expectedExceptions = InvalidSyntaxException.class)
    public void testParseBrokenThousandsShouldThrowException() throws InvalidSyntaxException {
        IntegerParser.parseAny("one thousand seven cat and six");
    }

    @Test
    public void testParseDefiniteOrdinals() throws Exception {
        IntegerParser.Result result;

        result = IntegerParser.parseOrdinal("the 123rd");
        assertEquals((int)result.value, 123);
        assertEquals(result.type, IntegerParser.ResultType.DefiniteOrdinal);

        result = IntegerParser.parseOrdinal("the third");
        assertEquals((int)result.value, 3);
        assertEquals(result.type, IntegerParser.ResultType.DefiniteOrdinal);
    }

    @Test
    public void testParseIndefiniteOrdinals() throws Exception {
        IntegerParser.Result result;

        result = IntegerParser.parseOrdinal("123rd");
        assertEquals((int)result.value, 123);
        assertEquals(result.type, IntegerParser.ResultType.Ordinal);

        result = IntegerParser.parseOrdinal("third");
        assertEquals((int)result.value, 3);
        assertEquals(result.type, IntegerParser.ResultType.Ordinal);
    }

}