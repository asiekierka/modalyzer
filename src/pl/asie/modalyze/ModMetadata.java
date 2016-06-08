package pl.asie.modalyze;

import java.util.List;
import java.util.Map;

public class ModMetadata {
    public String modid;
    public List<String> provides;
    public String name, description, version, homepage;
    public String side;
    public String filename, sha256;
    public List<String> authors;
    public Map<String, String> dependencies;

    public transient boolean valid;
}
