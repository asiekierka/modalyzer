/*
 * Copyright 2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
