package fr.lis.ikeyplus.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IkeyUtils {

//    public static final String UNKNOWN_DATA = "unknownData";
    // file prefix
    public static final String KEY = "key_";
    public static final String ERROR = "error_";
    // specific file extension
    public static final String GV = "gv";
    // buffer size
    public static final int BUFFER = 2048;

    private IkeyUtils() {
        // private empty constructor to prevent instantiation
    }

    /**
     * This method returns the intersection of two Lists
     */
    public static List<?> intersection(final List<?> list1, final List<?> list2) {
        final List<Object> list = new ArrayList<>();
        for (final Object o : list1) {
            if (list2.contains(o)) {
                list.add(o);
            }
        }
        return list;
    }

    /**
     * This method returns a list containing the elements of a primary list that do not appear in a list of
     * excluded elements
     *
     * @param primaryList  , the list which elements are to be retained
     * @param excludedList , the list which elements shall not remain in the final list
     * @return List
     */
    public static List<?> exclusion(final List<?> primaryList, final List<?> excludedList) {
        final List<Object> list = new ArrayList<>();
        for (final Object o : primaryList) {
            if (!excludedList.contains(o)) {
                list.add(o);
            }
        }
        return list;
    }

    /**
     * This method returns the union of two Lists
     */
    public static List<?> union(final List<?> list1, final List<?> list2) {
        final Set<Object> set = new HashSet<>();
        set.addAll(list1);
        set.addAll(list2);
        return new ArrayList<>(set);
    }

    /**
     * This method round a float value
     *
     * @param floatToRound the float number that will be rounded
     * @param roundFactor  the power of 10 used to round the float, e.g. if roundFactor = 3, the float number will be
     *                     rounded with 10^3 as a multiplier
     * @return float, the rounded value
     */
    public static float roundFloat(final float floatToRound, final int roundFactor) {
        double roundedFloat;
        final double multiplier = Math.pow((double) 10, (double) roundFactor);

        roundedFloat = multiplier * floatToRound;
        roundedFloat = (int) (roundedFloat + .5);
        roundedFloat /= multiplier;
        return (float) roundedFloat;
    }

    /**
     * This method round a double value
     *
     * @param score       the double number that will be rounded
     * @param roundFactor the power of 10 used to round the float, e.g. if roundFactor = 3, the float number will be
     *                    rounded with 10^3 as a multiplier
     * @return double, the rounded value
     */
    public static double roundDouble(final double score, final int roundFactor) {
        double roundedDouble;
        final double multiplier = Math.pow((double) 10, (double) roundFactor);

        roundedDouble = multiplier * score;
        roundedDouble = (int) (roundedDouble + .5);
        roundedDouble /= multiplier;
        return (float) roundedDouble;
    }

    /**
     * This method delete accent containing in a string
     *
     * @param s , the string candidate to delete accents
     * @return String, the string without accents
     */
    public static String unAccent(final String s) {
        final String temp = Normalizer.normalize(s, Normalizer.Form.NFC);
        return temp.replaceAll("[^\\p{ASCII}]", "");
    }
}
