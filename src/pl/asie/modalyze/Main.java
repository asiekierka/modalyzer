package pl.asie.modalyze;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Main {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException {
        Map<String, Map<String, ModMetadata>> metadata = ModAnalyzerUtils.analyzeMods(new File(args[0]), true, true);
        System.out.println(GSON.toJson(metadata));
    }
}
