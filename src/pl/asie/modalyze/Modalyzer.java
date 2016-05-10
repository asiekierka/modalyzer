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
import java.util.zip.ZipFile;

public class Modalyzer {
    private static final List<String> FORGE_MOD_ANNOTATIONS = Arrays.asList("Lcpw/mods/fml/common/Mod;", "Lnet/minecraftforge/fml/common/Mod;");

    public class ModClassVisitor extends ClassVisitor {
        private final Map<String, ModMetadata> metaMap;

        public ModClassVisitor(Map<String, ModMetadata> metadataMap) {
            super(Opcodes.ASM5);
            this.metaMap = metadataMap;
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
                            ModMetadata metadata = getOrCreate(metaMap, (String) data.get("modid"));
                            if (data.containsKey("name")) {
                                metadata.name = StringUtils.selectLonger(metadata.name, (String) data.get("name"));
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
        if (dep.contains("@")) {
            modName = dep.split("@")[0];
            modVersion = dep.split("@")[1];
        }

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

    private void appendMcmodInfo(Map<String, ModMetadata> metaMap, InputStream stream) throws IOException {
        McmodInfo info = McmodInfo.get(stream);
        if (info != null && info.modList != null) {
            for (McmodInfo.Entry entry : info.modList) {
                if (entry.modid == null) {
                    continue;
                }

                ModMetadata metadata = getOrCreate(metaMap, entry.modid);
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
                        metadata.dependencies = addDependency(metadata.dependencies, s);
                    }
                }
            }
        }
    }

    private void appendClassInfo(Map<String, ModMetadata> metaMap, InputStream stream) throws IOException {
        ClassVisitor visitor = new ModClassVisitor(metaMap);
        ClassReader reader = new ClassReader(stream);
        reader.accept(visitor, 0);
    }

    public Map<String, ModMetadata> analyzeMods(File file) {
        Map<String, ModMetadata> metadata = new HashMap<>();

        if (!file.isDirectory()) {
            analyzeModFile(metadata, file);
        } else {
            for (File f : file.listFiles()) {
                analyzeModFile(metadata, f);
            }
        }

        return metadata;
    }

    public Map<String, ModMetadata> analyzeModFile(Map<String, ModMetadata> metadata, File file) {
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
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }

        return metadata;
    }
}
