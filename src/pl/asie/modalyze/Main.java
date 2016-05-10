package pl.asie.modalyze;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException {
        Map<String, Map<String, ModMetadata>> metadata = new Modalyzer().analyzeMods(new File(args[0]), true);
        System.out.println(GSON.toJson(metadata));
    }
}
