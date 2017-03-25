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

package pl.asie.modalyze.mcp;

public final class MCPUtils {
    private MCPUtils() {

    }

    public static String getFieldKey(String name) {
        String basename = name.substring(name.lastIndexOf("/")+1);
        if (basename.startsWith("func_")) {
            return "F:" + basename;
        } else {
            return "F:" + name;
        }
    }

    public static String getMethodKey(String name, String sig) {
        String basename = name.substring(name.lastIndexOf("/")+1);
        if (basename.startsWith("func_")) {
            return "M:" + basename + ":" + sig;
        } else {
            return "M:" + name + ":" + sig;
        }
    }
}
