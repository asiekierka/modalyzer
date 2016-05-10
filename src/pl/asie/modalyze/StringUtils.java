package pl.asie.modalyze;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class StringUtils {
    private StringUtils() {

    }

    public static String selectLonger(String main, String secondary) {
        if (!isEmpty(main) && !isEmpty(secondary) && !main.equals(secondary)) {
            return secondary.length() > main.length() ? secondary : main;
        }
        return isEmpty(main) ? secondary : main;
    }


    public static String select(String main, String secondary) {
        if (!isEmpty(main) && !isEmpty(secondary) && !main.equals(secondary)) {
            System.err.println("[WARN] Mismatch! " + main + " =/= " + secondary);
        }
        return isEmpty(main) ? secondary : main;
    }

    public static List<String> append(List<String> orig, String entry) {
        List<String> target = orig != null ? orig : new ArrayList<>();
        if (!target.contains(entry)) {
            target.add(entry);
        }
        return target;
    }

    public static List<String> append(List<String> orig, Collection<String> entries) {
        List<String> target = orig != null ? orig : new ArrayList<>();
        for (String s : entries) {
            if (!target.contains(s)) {
                target.add(s);
            }
        }
        return target;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }
}
