package pl.asie.modalyze;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class ModAnalyzerUtils {
    private ModAnalyzerUtils() {

    }

    private static void appendModMetadata(Map<String, Map<String, ModMetadata>> metaMap, ModMetadata metadata) {
        if (metadata != null && metadata.modid != null) {
            String version = metadata.version != null ? metadata.version : "UNKNOWN";

            if (!metaMap.containsKey(metadata.modid)) {
                metaMap.put(metadata.modid, new HashMap<>());
            }
            Map<String, ModMetadata> versions = metaMap.get(metadata.modid);
            versions.put(version, metadata);
        }
    }

    public static Map<String, Map<String, ModMetadata>> analyzeMods(File file, boolean recursive, boolean heuristics) {
        Map<String, Map<String, ModMetadata>> metaMap = new HashMap<>();

        if (!file.isDirectory()) {
            appendModMetadata(metaMap, new ModAnalyzer(file).setVersionHeuristics(heuristics).analyze());
        } else {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    if (recursive) {
                        metaMap.putAll(analyzeMods(f, recursive, heuristics));
                    }
                } else {
                    appendModMetadata(metaMap, new ModAnalyzer(f).setVersionHeuristics(heuristics).analyze());
                }
            }
        }

        return metaMap;
    }

}
