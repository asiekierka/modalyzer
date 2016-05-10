package pl.asie.modalyze.mcp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MCPDataManager {
    private static class MCPVersion {
        final String mcpFile;
        final boolean hasSides;

        MCPVersion(String mcpFile, boolean hasSides) {
            this.mcpFile = mcpFile;
            this.hasSides = hasSides; // my sides
        }
    }

    private static final Map<String, MCPVersion> MCP_VERSION_MAP = new HashMap<>();
    public static File MCP_DIR = new File("./mcp/");
    private final Map<String, Set<String>> MAPPINGS = new HashMap<>();

    public MCPDataManager() {

    }

    public boolean hasSides(String version) {
        String v = version.split("-")[0];
        return MCP_VERSION_MAP.containsKey(v) ? MCP_VERSION_MAP.get(v).hasSides : (v.startsWith("b") ? true : false);
    }

    public Set<String> getVersionsForKeySet(Set<String> keys) {
        Map<String, Integer> versions = new HashMap<>();
        for (String s : MCP_VERSION_MAP.keySet()) {
            versions.put(s + "-client", 0);
            versions.put(s + "-server", 0);
        }

        /* for (String s : getMappings("1.5.2-client")) {
            System.out.println(s);
        }
        System.out.println("---");
        for (String s : keys) {
            System.out.println(s);
        } */

        for (String s : keys) {
            // HACK: No idea why that's needed.
            String s15x = s.replaceAll("net/minecraft/([a-z/]+)/([A-Z])", "net/minecraft/src/$2");
            for (String v : versions.keySet()) {
                Set<String> mappings = getMappings(v);
                if (v.startsWith("1.5") || v.startsWith("1.6")) {
                    if (mappings.contains(s15x)) {
                        versions.put(v, versions.get(v) + 1);
                    }
                } else {
                    if (mappings.contains(s)) {
                        versions.put(v, versions.get(v) + 1);
                    }
                }
            }
        }

        Set<String> versionSet = new HashSet<>();
        int maxV = 0;
        /* for (String s : versions.keySet()) {
            System.out.println(s + " = " + versions.get(s));
        } */

        for (String s : versions.keySet()) {
            if (versions.get(s) > maxV) {
                maxV = versions.get(s);
                versionSet.clear();
            }

            if (versions.get(s) == maxV) {
                versionSet.add(s);
            }
        }

        return versionSet.size() < MCP_VERSION_MAP.size() ? versionSet : null;
    }

    public Set<String> getMappings(String version, boolean server) {
        return getMappings(version + (server ? "-server" : "-client"));
    }

    public Set<String> getMappings(String version) {
        String target = version;
        if (!MAPPINGS.containsKey(target)) {
            try {
                loadMappings(version.split("-")[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return MAPPINGS.get(target);
    }

    public Set<String> getVersions() {
        return MCP_VERSION_MAP.keySet();
    }

    private void loadJoinedSrgMapping(String target, ZipFile file, ZipEntry entry) throws IOException {
        List<String> lines = IOUtils.readLines(file.getInputStream(entry), "UTF-8");
        Set<String> mapClient = new HashSet<>();
        Set<String> mapServer = new HashSet<>();

        if (lines != null) {
            for (String s : lines) {
                String[] sp = s.split(" ");
                if (!s.endsWith("#S")) {
                    if (s.startsWith("FD:") && sp.length >= 3) {
                        mapClient.add(MCPUtils.getFieldKey(sp[1]));
                        mapClient.add(MCPUtils.getFieldKey(sp[2]));
                    } else if (s.startsWith("MD:") && sp.length >= 5) {
                        mapClient.add(MCPUtils.getMethodKey(sp[1], sp[2]));
                        mapClient.add(MCPUtils.getMethodKey(sp[3], sp[4]));
                    }
                }

                if (!s.endsWith("#C")) {
                    if (s.startsWith("FD:") && sp.length >= 3) {
                        mapServer.add(MCPUtils.getFieldKey(sp[1]));
                        mapServer.add(MCPUtils.getFieldKey(sp[2]));
                    } else if (s.startsWith("MD:") && sp.length >= 5) {
                        mapServer.add(MCPUtils.getMethodKey(sp[1], sp[2]));
                        mapServer.add(MCPUtils.getMethodKey(sp[3], sp[4]));
                    }
                }
            }
        } else {
            System.err.println("Error loading SRG mapping for " + target);
        }

        MAPPINGS.put(target + "-client", mapClient);
        MAPPINGS.put(target + "-server", mapServer);
    }

    private void loadSrgMapping(String target, ZipFile file, ZipEntry entry) throws IOException {
        List<String> lines = IOUtils.readLines(file.getInputStream(entry), "UTF-8");
        Set<String> map = new HashSet<>();

        if (lines != null) {
            for (String s : lines) {
                String[] sp = s.split(" ");
                if (s.startsWith("FD:") && sp.length >= 3) {
                    map.add(MCPUtils.getFieldKey(sp[1]));
                    map.add(MCPUtils.getFieldKey(sp[2]));
                } else if (s.startsWith("MD:") && sp.length >= 5) {
                    map.add(MCPUtils.getMethodKey(sp[1], sp[2]));
                    map.add(MCPUtils.getMethodKey(sp[3], sp[4]));
                }
            }
        } else {
            System.err.println("Error loading SRG mapping for " + target);
        }

        MAPPINGS.put(target, map);
    }

    private String[] splitCsv(String s) {
        String[] sp = s.split(",");
        for (int i = 0; i < sp.length; i++) {
            sp[i] = sp[i].replace("\"", "");
        }
        return sp;
    }

    private void loadCsvMapping(String target, ZipFile file, ZipEntry fields, ZipEntry methods) throws IOException {
        List<String> fieldList = IOUtils.readLines(file.getInputStream(fields), "UTF-8");
        List<String> methodList = IOUtils.readLines(file.getInputStream(methods), "UTF-8");
        Set<String> mapClient = new HashSet<>();
        Set<String> mapServer = new HashSet<>();

        if (fieldList != null && methodList != null) {
            for (int i = 1; i < fieldList.size(); i++) {
                String[] sp = splitCsv(fieldList.get(i));
                if (sp.length == 9) {
                    Set<String> mapTarget = sp[8].equals("1") ? mapServer : mapClient;
                    mapTarget.add(MCPUtils.getFieldKey(sp[6] + "/" + sp[2]));
                }
            }
            for (int i = 1; i < methodList.size(); i++) {
                String[] sp = splitCsv(methodList.get(i));
                if (sp.length == 9) {
                    Set<String> mapTarget = sp[8].equals("1") ? mapServer : mapClient;
                    mapTarget.add(MCPUtils.getMethodKey(sp[6] + "/" + sp[2], sp[4]));
                }
            }
        } else {
            System.err.println("Error loading CSV mapping for " + target);
        }

        MAPPINGS.put(target + "-client", mapClient);
        MAPPINGS.put(target + "-server", mapServer);
    }

    private void loadMappings(String version) throws IOException {
        File mappingClient = new File(MCP_DIR, version + "-client.map");
        File mappingServer = new File(MCP_DIR, version + "-server.map");
        if (mappingClient.exists() && mappingServer.exists()) {
            MAPPINGS.put(version + "-client", new HashSet<>(FileUtils.readLines(mappingClient, "UTF-8")));
            MAPPINGS.put(version + "-server", new HashSet<>(FileUtils.readLines(mappingServer, "UTF-8")));
        } else {
            File mcpFile = new File(MCP_DIR, MCP_VERSION_MAP.get(version).mcpFile);
            if (mcpFile.exists()) {
                ZipFile zipFile = new ZipFile(mcpFile);
                ZipEntry joinedSrgEntry = zipFile.getEntry("conf/joined.srg");
                if (joinedSrgEntry != null) {
                    loadJoinedSrgMapping(version, zipFile, joinedSrgEntry);
                } else {
                    ZipEntry clientSrgEntry = zipFile.getEntry("conf/client.srg");
                    ZipEntry serverSrgEntry = zipFile.getEntry("conf/server.srg");
                    if (clientSrgEntry != null && serverSrgEntry != null) {
                        loadSrgMapping(version + "-client", zipFile, clientSrgEntry);
                        loadSrgMapping(version + "-server", zipFile, serverSrgEntry);
                    } else {
                        ZipEntry csvFields = zipFile.getEntry("conf/fields.csv");
                        ZipEntry csvMethods = zipFile.getEntry("conf/methods.csv");
                        if (csvFields != null && csvMethods != null) {
                            loadCsvMapping(version, zipFile, csvFields, csvMethods);
                        } else {
                            System.err.println("MCP file for Minecraft " + version + " (" + mcpFile.toString() + ") stored in an unknown format!");
                            MAPPINGS.put(version + "-client", Collections.EMPTY_SET);
                            MAPPINGS.put(version + "-server", Collections.EMPTY_SET);
                        }
                    }
                }

                if (MAPPINGS.get(version + "-client") != null) {
                    FileUtils.writeLines(mappingClient, MAPPINGS.get(version + "-client"));
                }

                if (MAPPINGS.get(version + "-server") != null) {
                    FileUtils.writeLines(mappingServer, MAPPINGS.get(version + "-server"));
                }
            } else {
                System.err.println("MCP file for Minecraft " + version + " (" + mcpFile.toString() + ") not found!");
                MAPPINGS.put(version + "-client", Collections.EMPTY_SET);
                MAPPINGS.put(version + "-server", Collections.EMPTY_SET);
            }
        }
    }

    static {
        MCP_VERSION_MAP.put("b1.6.6", new MCPVersion("mcp41.zip", true));
        MCP_VERSION_MAP.put("b1.7.2", new MCPVersion("mcp42.zip", true));
        MCP_VERSION_MAP.put("b1.7.3", new MCPVersion("mcp43.zip", true));
        MCP_VERSION_MAP.put("b1.8.1", new MCPVersion("mcp44.zip", true));
        MCP_VERSION_MAP.put("1.0.0", new MCPVersion("mcp50.zip", true));
        MCP_VERSION_MAP.put("1.1", new MCPVersion("mcp56.zip", true));
        MCP_VERSION_MAP.put("1.2.3", new MCPVersion("mcp60.zip", true));
        MCP_VERSION_MAP.put("1.2.4", new MCPVersion("mcp61.zip", true));
        MCP_VERSION_MAP.put("1.2.5", new MCPVersion("mcp62.zip", true));
        MCP_VERSION_MAP.put("1.3.1", new MCPVersion("mcp70a.zip", false));
        MCP_VERSION_MAP.put("1.3.2", new MCPVersion("mcp72.zip", false));
        MCP_VERSION_MAP.put("1.4.2", new MCPVersion("mcp719.zip", false));
        MCP_VERSION_MAP.put("1.4.4", new MCPVersion("mcp721.zip", false));
        MCP_VERSION_MAP.put("1.4.5", new MCPVersion("mcp723.zip", false));
        MCP_VERSION_MAP.put("1.4.6", new MCPVersion("mcp725.zip", false));
        MCP_VERSION_MAP.put("1.4.7", new MCPVersion("mcp726a.zip", false));
        MCP_VERSION_MAP.put("1.5", new MCPVersion("mcp742.zip", false));
        MCP_VERSION_MAP.put("1.5.1", new MCPVersion("mcp744.zip", false));
        MCP_VERSION_MAP.put("1.5.2", new MCPVersion("mcp751.zip", false));
        MCP_VERSION_MAP.put("1.6.1", new MCPVersion("mcp803.zip", false));
        MCP_VERSION_MAP.put("1.6.2", new MCPVersion("mcp805.zip", false));
        MCP_VERSION_MAP.put("1.6.4", new MCPVersion("mcp811.zip", false));
        MCP_VERSION_MAP.put("1.7.2", new MCPVersion("mcp903.zip", false));
        MCP_VERSION_MAP.put("1.7.10", new MCPVersion("mcp908.zip", false));
        MCP_VERSION_MAP.put("1.8", new MCPVersion("mcp910.zip", false));
        MCP_VERSION_MAP.put("1.8.8", new MCPVersion("mcp918.zip", false));
    }
}
