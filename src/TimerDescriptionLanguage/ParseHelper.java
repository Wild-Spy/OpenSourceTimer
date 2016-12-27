package TimerDescriptionLanguage;

/**
 * Created by mcochrane on 17/12/16.
 */
public class ParseHelper {

    /**
     * Finds a string within an array.  Must be an exact match.
     * @param strToFind the string to find in the array
     * @param searchAry the array of strings to be searched
     * @return the index of the string if found or null if the string
     *         wasn't present in the array
     */
    public static Integer findStringInArray(String strToFind, String[] searchAry) {
        for (int i = 0; i < searchAry.length; i++) {
            if (strToFind.equals(searchAry[i])) return i;
        }
        return null;
    }

    /**
     * Checks if a string starts with any of the strings in an array.
     * @param str string to check
     * @param findStrings array of strings
     * @return true if <code>str</code> starts with one of <code>findStrings</code>,
     *         false otherwise
     */
    public static boolean startsWithAnyOf(String str, String[] findStrings) {
        for (String s : findStrings) {
            if (str.startsWith(s)) return true;
        }
        return false;
    }

    /**
     * Checks if a string ends in any of the strings in an array.
     * @param str string to check
     * @param findStrings array of strings
     * @return true if <code>str</code> ends with one of <code>findStrings</code>,
     *         false otherwise
     */
    public static boolean endsWithAnyOf(String str, String[] findStrings) {
        for (String s : findStrings) {
            if (str.endsWith(s)) return true;
        }
        return false;
    }

    /**
     * Check if a string contains one of the string in an array.
     * @param str string to check
     * @param findStrings array of strings
     * @return true if <code>str</code> contains one of <code>findStrings</code>, false otherwise
     */
    public static boolean containsAnyOf(String str, String[] findStrings) {
        for (String s : findStrings) {
            if (str.contains(s)) return true;
        }
        return false;
    }

    /**
     * If <code>str</code> ends in any of <code>removeStrAry</code>, it is removed from
     * the end.  The function is not iterative, it will only remove a single string from
     * the end of <code>str</code>.
     * @param str string to check
     * @param removeStrAry array of strings to possibly be removed from end of <code>str</code>
     * @return an updated string with any one of <code>removeStrAry</code> removed from the end
     */
    public static String removeAnyFromEnd(String str, String[] removeStrAry) {
        for (String s : removeStrAry) {
            if (str.endsWith(s)) {
                return str.substring(0, str.length()-s.length());
            }
        }
        return str;
    }

    /**
     * If <code>str</code> starts in any of <code>removeStrAry</code>, it is removed from
     * the start.  The function is not iterative, it will only remove a single string from
     * the start of <code>str</code>.
     * @param str string to check
     * @param removeStrAry array of strings to possibly be removed from the start of
     *                     <code>str</code>
     * @return an updated string with any one of <code>removeStrAry</code> removed from
     *         the start
     */
    public static String removeAnyFromStart(String str, String[] removeStrAry) {
        for (String s : removeStrAry) {
            if (str.startsWith(s)) {
                return str.substring(s.length());
            }
        }
        return str;
    }

}
