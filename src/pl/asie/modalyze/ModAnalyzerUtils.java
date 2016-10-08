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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class ModAnalyzerUtils {
    private ModAnalyzerUtils() {

    }

    public static boolean isValidMcVersion(String version) {
        // Check for @VERSION@, ${mcversion}, etc.
        return version.indexOf("ver") < 0 && version.indexOf("VER") < 0;
    }

    private static void appendModMetadata(Map<String, Map<String, ModMetadata>> metaMap, ModMetadata metadata, File file, boolean asFilenames) {
        if (metadata != null) {
            String key = asFilenames ? file.getName() : metadata.modid;
            if (key != null) {
                String version = metadata.version != null ? metadata.version : "UNKNOWN";

                if (!metaMap.containsKey(key)) {
                    metaMap.put(key, new HashMap<>());
                }
                Map<String, ModMetadata> versions = metaMap.get(key);
                versions.put(version, metadata);
            }
        }
    }

    public static Map<String, Map<String, ModMetadata>> analyzeMods(File file, boolean recursive, boolean heuristics, boolean asFilenames) {
        Map<String, Map<String, ModMetadata>> metaMap = new HashMap<>();

        if (!file.isDirectory()) {
            appendModMetadata(metaMap, new ModAnalyzer(file).setVersionHeuristics(heuristics).analyze(), file, asFilenames);
        } else {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    if (recursive) {
                        metaMap.putAll(analyzeMods(f, recursive, heuristics, asFilenames));
                    }
                } else {
                    appendModMetadata(metaMap, new ModAnalyzer(f).setVersionHeuristics(heuristics).analyze(), f, asFilenames);
                }
            }
        }

        return metaMap;
    }
}
