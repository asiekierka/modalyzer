package pl.asie.modalyze;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.asie.modalyze.mcp.MCPDataManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    private static class Parameters {
        @Parameter(names = {"-H", "--hash"}, description = "Generate SHA256 hashes of mods")
        private boolean hash = false;

        @Parameter(names = {"-I", "--sort-id"}, description = "Index by mod IDs")
        private boolean sortId = false;

        @Parameter(names = {"-F", "--sort-filename"}, description = "Index by mod filenames")
        private boolean sortFilename = false;

        @Parameter(names = {"-f", "--filename"}, description = "Store mod filenames (implied by -F)")
        private boolean filenames = false;

        @Parameter(names = {"-h", "--help"}, description = "Print usage", help = true)
        private boolean help;

        @Parameter(names = {"-v", "--verbose"}, description = "Be more verbose")
        private boolean verbose;

        @Parameter(names = {"-U", "--unknown"}, description = "List unknown mod IDs and versions")
        private boolean unknown;

        @Parameter(names = {"-m", "--mcp"}, description = "Location to MCP (./mcp/ by default)")
        private String mcpPath;

        @Parameter(description = "Input files and directories")
        private List<String> files = new ArrayList<>();
    }

    private static final Set<ModMetadata> modMetadata = new HashSet<>();
    private static Parameters parameters = new Parameters();

    private static ModAnalyzer analyzer(File file) {
        return new ModAnalyzer(file)
                .setVersionHeuristics(true)
                .setGenerateHash(parameters.hash)
                .setStoreFilenames(parameters.filenames)
                .setIsVerbose(parameters.verbose);
    }

    public static void analyzeMods(File file) {
        if (!file.isDirectory()) {
            modMetadata.add(analyzer(file).analyze());
        } else {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    analyzeMods(f);
                } else {
                    modMetadata.add(analyzer(f).analyze());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        JCommander jCommander = new JCommander(parameters, args);

        if (parameters.help) {
            jCommander.usage();
            System.exit(0);
        }

        if (parameters.sortFilename) {
            parameters.filenames = true;
        }

        if (parameters.mcpPath != null && parameters.mcpPath.length() > 0) {
            File f = new File(parameters.mcpPath);
            if (f.isDirectory()) {
                MCPDataManager.MCP_DIR = f;
            } else {
                System.err.println("Not a directory: " + f.getAbsolutePath());
                System.exit(0);
            }
        }

        boolean isDir = false;
        for (String s : parameters.files) {
            File f = new File(s);
            isDir |= f.isDirectory();
            analyzeMods(f);
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();

        if (isDir || parameters.files.size() > 1 || modMetadata.size() > 1) {
            if (parameters.sortFilename) {
                Map<String, ModMetadata> metadataMap = new HashMap<>();
                for (ModMetadata m : modMetadata) {
                    if (m == null) {
                        continue;
                    }
                    metadataMap.put(m.filename, m);
                }
                System.out.println(gson.toJson(metadataMap));
            } else if (parameters.sortId) {
                Map<String, List<ModMetadata>> metadataMap = new HashMap<>();
                for (ModMetadata m : modMetadata) {
                    if (m == null) {
                        continue;
                    }
                    String key = m.modid;
                    if (key == null && parameters.unknown) {
                        int i = 0;
                        while (metadataMap.containsKey("UNKNOWN-" + i)) {
                            i++;
                        }
                        key = "UNKNOWN-" + i;
                    }
                    if (key != null) {
                        List<ModMetadata> mods = metadataMap.get(key);
                        if (mods == null) {
                            mods = new ArrayList<>();
                            metadataMap.put(key, mods);
                        }
                        mods.add(m);
                    }
                }
                for (List<ModMetadata> list : metadataMap.values()) {
                    Collections.sort(list, new Comparator<ModMetadata>() {
                        @Override
                        public int compare(ModMetadata m1, ModMetadata m2) {
                            if (m1.version == null && m2.version == null) {
                                return 0;
                            } else if (m1.version != null && m2.version != null) {
                                return m1.version.compareTo(m2.version);
                            } else {
                                return m1.version != null ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                            }
                        }
                    });
                }
                System.out.println(gson.toJson(metadataMap));
            } else {
                Map<String, Map<String, ModMetadata>> metadataMap = new HashMap<>();
                for (ModMetadata m : modMetadata) {
                    if (m == null) {
                        continue;
                    }

                    String key;
                    if (m.modid != null) {
                        key = m.modid;
                    } else if (!parameters.unknown) {
                        continue;
                    } else {
                        int i = 0;
                        while (metadataMap.containsKey("UNKNOWN-" + i)) {
                            i++;
                        }
                        key = "UNKNOWN-" + i;
                    }

                    Map<String, ModMetadata> metadataMap1 = metadataMap.get(key);
                    if (metadataMap1 == null) {
                        metadataMap1 = new HashMap<>();
                        metadataMap.put(key, metadataMap1);
                    }
                    if (m.version != null) {
                        metadataMap1.put(m.version, m);
                    } else if (parameters.unknown) {
                        int i = 0;
                        while (metadataMap1.containsKey("UNKNOWN-" + i)) {
                            i++;
                        }
                        metadataMap1.put("UNKNOWN-" + i, m);
                    }
                }
                System.out.println(gson.toJson(metadataMap));
            }
        } else if (modMetadata.size() >= 1) {
            System.out.println(gson.toJson(modMetadata.toArray()[0]));
        } else {
            System.err.println("[ERROR] No mods found!");
        }
    }
}
