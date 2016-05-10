package pl.asie.modalyze;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Modalyzer {
    private static final List<String> FORGE_MOD_ANNOTATIONS = Arrays.asList("Lcpw/mods/fml/common/Mod;", "Lnet/minecraftforge/fml/common/Mod;");

    public class ModClassVisitor extends ClassVisitor {
        private final ModMetadata metadata;

        public ModClassVisitor(ModMetadata metadata) {
            super(Opcodes.ASM5);
            this.metadata = metadata;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (FORGE_MOD_ANNOTATIONS.contains(desc)) {
                AnnotationVisitor visitor = new AnnotationVisitor(Opcodes.ASM5) {
                    private Map<String, Object> data = new HashMap<>();

                    @Override
                    public void visit(String name, Object value) {
                        data.put(name, value);
                    }

                    @Override
                    public void visitEnum(String name, String desc, String value) {
                        data.put(name, value);
                    }

                    @Override
                    public void visitEnd() {
                        if (data.containsKey("modid")) {
                            metadata.provides = StringUtils.append(metadata.provides, (String) data.get("modid"));
                        }
                        if (data.containsKey("version")) {
                            metadata.version = StringUtils.select(metadata.version, (String) data.get("version"));
                        }
                        if (data.containsKey("dependencies")) {
                            List<String> dependencies = Arrays.asList(((String) data.get("dependencies")).split(";"));
                            for (String s : dependencies) {
                                String[] dep = s.split(":");
                                if (dep.length == 2 && dep[0].startsWith("required")) {
                                    metadata.dependencies = addDependency(metadata.dependencies, dep[1]);
                                }
                            }
                        }
                    }
                };

                return visitor;
            }
            return super.visitAnnotation(desc, visible);
        }
    }

    public Modalyzer() {

    }

    private Map<String, String> addDependency(Map<String, String> deps, String dep) {
        if (deps == null) {
            deps = new HashMap<>();
        }

        String modName = dep;
        String modVersion = "*";
        if (dep.contains("@") && dep.split("@").length == 2) {
            modName = dep.split("@")[0];
            modVersion = dep.split("@")[1];
        }

        modName = modName.trim();
        modVersion = modVersion.trim();

        if (deps.containsKey(modName)) {
            if (!"*".equals(modVersion) && "*".equals(deps.get(modName))) {
                deps.put(modName, modVersion);
            }
        } else {
            deps.put(modName, modVersion);
        }

        return deps;
    }

    private ModMetadata getOrCreate(Map<String, ModMetadata> metaMap, String id) {
        ModMetadata metadata = metaMap.get(id);
        if (metadata == null) {
            metadata = new ModMetadata();
            metadata.modid = id;
            metaMap.put(metadata.modid, metadata);
        }
        return metadata;
    }

    private void appendMcmodInfo(ModMetadata metadata, InputStream stream) throws IOException {
        McmodInfo info = McmodInfo.get(stream);
        if (info != null && info.modList != null) {
            for (McmodInfo.Entry entry : info.modList) {
                if (entry.modid == null) {
                    continue;
                }

                if (metadata.modid == null) {
                    metadata.modid = entry.modid;
                } else if (!metadata.modid.equals(entry.modid)) {
                    continue;
                }

                metadata.provides = StringUtils.append(metadata.provides, entry.modid);
                metadata.name = StringUtils.selectLonger(entry.name, metadata.name);
                metadata.description = StringUtils.select(entry.description, metadata.description);
                metadata.version = StringUtils.select(entry.version, metadata.version);
                metadata.mcversion = StringUtils.select(entry.mcversion, metadata.mcversion);
                metadata.homepage = StringUtils.select(entry.url, metadata.homepage);
                if (entry.authorList != null) {
                    metadata.authors = StringUtils.append(metadata.authors, entry.authorList);
                }
                if (!StringUtils.isEmpty(entry.credits)) {
                    metadata.authors = StringUtils.append(metadata.authors, entry.credits);
                }
                if (entry.requiredMods != null) {
                    for (String s : entry.requiredMods) {
                        if (!StringUtils.isEmpty(s)) {
                            metadata.dependencies = addDependency(metadata.dependencies, s);
                        }
                    }
                }
            }
        }
    }

    private void appendClassInfo(ModMetadata metadata, InputStream stream) throws IOException {
        try {
            ClassVisitor visitor = new ModClassVisitor(metadata);
            ClassReader reader = new ClassReader(stream);
            reader.accept(visitor, 0);
        } catch (Exception e) {
            // Oh well.
        }
    }

    private void appendModMetadata(Map<String, Map<String, ModMetadata>> metaMap, ModMetadata metadata) {
        if (metadata != null && metadata.modid != null) {
            String version = metadata.version != null ? metadata.version : "UNKNOWN";

            if (!metaMap.containsKey(metadata.modid)) {
                metaMap.put(metadata.modid, new HashMap<>());
            }
            Map<String, ModMetadata> versions = metaMap.get(metadata.modid);
            versions.put(version, metadata);
        }
    }

    public Map<String, Map<String, ModMetadata>> analyzeMods(File file, boolean recursive) {
        Map<String, Map<String, ModMetadata>> metaMap = new HashMap<>();

        if (!file.isDirectory()) {
            appendModMetadata(metaMap, analyzeModFile(file));
        } else {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    if (recursive) {
                        metaMap.putAll(analyzeMods(f, recursive));
                    }
                } else {
                    appendModMetadata(metaMap, analyzeModFile(f));
                }
            }
        }

        return metaMap;
    }

    public ModMetadata analyzeModFile(File file) {
        ModMetadata metadata = new ModMetadata();
        System.out.println("[*] " + file.toString());

        try {
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().equals("mcmod.info")) {
                    appendMcmodInfo(metadata, zipFile.getInputStream(entry));
                } else if (entry.getName().endsWith(".class")) {
                    appendClassInfo(metadata, zipFile.getInputStream(entry));
                }
            }
        } catch (ZipException exception) {
            return null;
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }

        if (metadata.provides != null) {
            metadata.provides.remove(metadata.modid);
            if (metadata.provides.size() == 0) {
                metadata.provides = null;
            }
        }

        if (metadata.dependencies != null) {
            metadata.dependencies.remove(metadata.modid);
            if (metadata.provides != null) {
                for (String id : metadata.provides) {
                    metadata.dependencies.remove(id);
                }
            }
            if (metadata.dependencies.size() == 0) {
                metadata.dependencies = null;
            }
        }

        return metadata.modid != null ? metadata : null;
    }
}
