package pl.asie.modalyze;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class ModAnalyzerUtils {
    private ModAnalyzerUtils() {

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
