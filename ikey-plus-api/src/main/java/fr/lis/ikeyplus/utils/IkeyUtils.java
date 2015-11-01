package fr.lis.ikeyplus.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IkeyUtils {


    /**
     * This method returns the intersection of two Lists
     *
     * @param list1
     * @param list2
     * @return List<?>
     */
    public static List<?> intersection(List<?> list1, List<?> list2) {
        List<Object> list = new ArrayList<Object>();
        for (Object o : list1) {
            if (list2.contains(o))
                list.add(o);
        }
        return list;
    }

    /**
     * This method returns a list containing the elements of a primary list that do not appear in a list of
     * excluded elements
     *
     * @param primaryList  , the list which elements are to be retained
     * @param excludedList , the list which elements shall not remain in the final list
     * @return List<?>
     */
    public static List<?> exclusion(List<?> primaryList, List<?> excludedList) {
        List<Object> list = new ArrayList<Object>();
        for (Object o : primaryList) {
            if (!excludedList.contains(o))
                list.add(o);
        }
        return list;
    }

    /**
     * This method returns the union of two Lists
     *
     * @param list1
     * @param list2
     * @return List<?>
     */
    public static List<?> union(List<?> list1, List<?> list2) {
        Set<Object> set = new HashSet<Object>();
        set.addAll(list1);
        set.addAll(list2);
        return new ArrayList<Object>(set);
    }

    /**
     * This method round a float value
     *
     * @param floatToRound the float number that will be rounded
     * @param roundFactor  the power of 10 used to round the float, e.g. if roundFactor = 3, the float number will be
     *                     rounded with 10^3 as a multiplier
     * @return float, the rounded value
     */
    public static float roundFloat(float floatToRound, int roundFactor) {
        double roundedFloat = 0;
        double multiplier = Math.pow((double) 10, (double) roundFactor);

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
    public static double roundDouble(double score, int roundFactor) {
        double roundedDouble = 0;
        double multiplier = Math.pow((double) 10, (double) roundFactor);

        roundedDouble = multiplier * score;
        roundedDouble = (int) (roundedDouble + .5);
        roundedDouble /= multiplier;
        return (float) roundedDouble;
    }

    /**
     * This method delete accent containing in a string
     *
     * @param String , the string candidate to delete accents
     * @return String, the string without accents
     */
    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFC);
        return temp.replaceAll("[^\\p{ASCII}]", "");
    }
}
