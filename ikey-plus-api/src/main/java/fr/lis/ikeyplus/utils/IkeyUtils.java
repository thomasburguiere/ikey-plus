package fr.lis.ikeyplus.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IkeyUtils {

    public static final String UNKNOWN_DATA = "unknownData";
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
    public static List<?> intersection(Iterable<?> list1, Collection<?> list2) {
        List<Object> list = new ArrayList<>();
        for (Object o : list1) {
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
    public static List<?> exclusion(Iterable<?> primaryList, Collection<?> excludedList) {
        List<Object> list = new ArrayList<>();
        for (Object o : primaryList) {
            if (!excludedList.contains(o)) {
                list.add(o);
            }
        }
        return list;
    }

    /**
     * This method returns the union of two Lists
     */
    public static List<?> union(Collection<?> list1, Collection<?> list2) {
        Set<Object> set = new HashSet<>();
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
    public static float roundFloat(float floatToRound, int roundFactor) {
        double roundedFloat;
        double multiplier = Math.pow((double) 10, (double) roundFactor);

        roundedFloat = multiplier * floatToRound;
        roundedFloat = (int) (roundedFloat + .5);
        roundedFloat /= multiplier;
        return (float) roundedFloat;
    }


    public static void generatedKeyFolderPathIfNeeded() {
        final String generatedKeyFolderPath = IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.prefix")
                + IkeyConfig.getBundleConfOverridableElement("generatedKeyFiles.folder");
        if (!new File(generatedKeyFolderPath).exists()) {
            final boolean created = new File(generatedKeyFolderPath).mkdirs();
            if (!created) {
                throw new IllegalStateException("Could not create non-existing " + generatedKeyFolderPath + "!");
            }
        }
    }
}
