package TimerDescriptionLanguage;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by mcochrane on 17/12/16.
 */
public class YearParserTest {

    @Test
    public void testParseYearDigits() throws InvalidSyntaxException {
        assertEquals((int)YearParser.parse("1900"), 1900);
        assertEquals((int)YearParser.parse("1923"), 1923);
    }

    @Test
    public void testParseYearDigitsAndADOrBC() throws InvalidSyntaxException {
        assertEquals((int)YearParser.parse("1900 AD"), 1900);
        assertEquals((int)YearParser.parse("1923 AD"), 1923);
        assertEquals((int)YearParser.parse("100 BC"), -100);

        assertEquals((int)YearParser.parse("1900 A.D."), 1900);
        assertEquals((int)YearParser.parse("1923 A.D."), 1923);
        assertEquals((int)YearParser.parse("111 B.C."), -111);

        assertEquals((int)YearParser.parse("1900AD"), 1900);
        assertEquals((int)YearParser.parse("1923AD"), 1923);
        assertEquals((int)YearParser.parse("100BC"), -100);

        assertEquals((int)YearParser.parse("1900CE"), 1900);
        assertEquals((int)YearParser.parse("1923CE"), 1923);
        assertEquals((int)YearParser.parse("123BCE"), -123);

        assertEquals((int)YearParser.parse("1900 CE"), 1900);
        assertEquals((int)YearParser.parse("100 BCE"), -100);

        assertEquals((int)YearParser.parse("1923 C.E."), 1923);
        assertEquals((int)YearParser.parse("100 B.C.E."), -100);

        assertEquals((int)YearParser.parse("1900C.E."), 1900);
        assertEquals((int)YearParser.parse("1995B.C.E."), -1995);
    }

    @Test
    public void testParseYearWordsLengthOne() throws InvalidSyntaxException {
        assertEquals((int)YearParser.parse("one"), 1);
        assertEquals((int)YearParser.parse("eleven"), 11);
        assertEquals((int)YearParser.parse("ten"), 10);
        assertEquals((int)YearParser.parse("fifty"), 50);
        try {
            YearParser.parse("hundred");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {}
        try {
            YearParser.parse("thousand");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {}
    }

    @Test
    public void testParseYearWordsLengthTwo() throws InvalidSyntaxException {
        assertEquals((int)YearParser.parse("six hundred"), 600);
        assertEquals((int)YearParser.parse("two thousand"), 2000);
        try {
            assertEquals(YearParser.parse("five nineteen"), null);
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {}
        assertEquals((int)YearParser.parse("twenty one"), 21);
        assertEquals((int)YearParser.parse("forty five"), 45);
        assertEquals((int)YearParser.parse("ten ten"), 1010);
        assertEquals((int)YearParser.parse("twelve ten"), 1210);
        assertEquals((int)YearParser.parse("eighteen fifteen"), 1815);
        assertEquals((int)YearParser.parse("ninety thousand"), 90000);
        try {
            YearParser.parse("nineteen seven");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {}
        try {
            YearParser.parse("thirty hundred");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {}
        assertEquals((int)YearParser.parse("nineteen eleven"), 1911);
        assertEquals((int)YearParser.parse("nineteen thirty"), 1930);
        assertEquals((int)YearParser.parse("nineteen hundred"), 1900);
        assertEquals((int)YearParser.parse("seventeen hundred"), 1700);
        assertEquals((int)YearParser.parse("eleven thousand"), 11000);
    }

    @Test
    public void testParseYearWordsLengthThree() throws InvalidSyntaxException {
        assertEquals((int)YearParser.parse("nineteen twenty three"), 1923);
        assertEquals((int)YearParser.parse("nineteen ninety five"), 1995);
        assertEquals((int)YearParser.parse("twenty one thirty"), 2130);
        assertEquals((int)YearParser.parse("twenty two eighteen"), 2218);
        assertEquals((int)YearParser.parse("two thousand and four"), 2004); //and is removed
        assertEquals((int)YearParser.parse("two thousand four"), 2004);
        assertEquals((int)YearParser.parse("two thousand and sixteen"), 2016); //and is removed
        assertEquals((int)YearParser.parse("one hundred and ten"), 110); //and is removed
        assertEquals((int)YearParser.parse("one hundred ten"), 110);
        assertEquals((int)YearParser.parse("twelve hundred and ten"), 1210); //and is removed
        assertEquals((int)YearParser.parse("nineteen hundred and ninety"), 1990); //and is removed
        assertEquals((int)YearParser.parse("fifty thousand and one"), 50001); //and is removed

        try {
            YearParser.parse("hundred hundred and hundred");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {}

        try {
            YearParser.parse("thousand hundred and one");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {}

        try {
            YearParser.parse("one thousand and thousand");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {}

        try {
            YearParser.parse("thousand fifty one");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {}

    }

    @Test
    public void testParseYearWordsLengthFour() throws InvalidSyntaxException {
        assertEquals((int)YearParser.parse("one hundred and twenty four"), 124);

        assertEquals((int)YearParser.parse("two thousand and twenty one"), 2021);
        assertEquals((int)YearParser.parse("twenty one twenty one"), 2121);
        assertEquals((int)YearParser.parse("ninety nine fifty four"), 9954);
        assertEquals((int)YearParser.parse("two thousand one hundred"), 2100);
        assertEquals((int)YearParser.parse("one hundred eight hundred"), 100800); //a bit bogus...

        try {
            YearParser.parse("twenty one thirty sixteen");
        } catch (InvalidSyntaxException ex) {}

        try {
            YearParser.parse("hundred hundred one hundred");
        } catch (InvalidSyntaxException ex) {}

        try {
            YearParser.parse("five thousand and hundred one");
        } catch (InvalidSyntaxException ex) {}

        try {
            YearParser.parse("twenty hundred and fifty five");
        } catch (InvalidSyntaxException ex) {}

        try {
            YearParser.parse("five thousand thirty hundred");
        } catch (InvalidSyntaxException ex) {}

        try {
            YearParser.parse("five thousand one thousand");
        } catch (InvalidSyntaxException ex) {}
    }

    //TODO: Add worded with BC or AD after.  ie. ten BC, two thousand AD
    //TODO: Check whether there is a zero year - there shouldn't be!? so 100bc should actually be -99???  OR is the zero year just ignored? Does it even really matter?

}