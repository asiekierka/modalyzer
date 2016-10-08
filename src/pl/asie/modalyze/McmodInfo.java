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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class McmodInfo {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Type LIST_TYPE_TOKEN = new TypeToken<List<Entry>>(){}.getType();

    public class Entry {
        public String modid;
        public String name;
        public String description;
        public String version;
        public String mcversion;
        public String url;
        public String updateUrl;
        public List<String> authorList;
        public String credits;
        public String logoFile;
        public List<String> screenshots;
        public String parent;
        public List<String> requiredMods;
        public List<String> dependencies;
        public List<String> dependants;
        public boolean useDependencyInformation;

        public Entry() {

        }
    }

    public List<Entry> modList;

    public McmodInfo() {

    }

    public static McmodInfo get(InputStream stream) throws IOException {
        McmodInfo info = null;
        String jsonText = IOUtils.toString(stream, "UTF-8");

        try {
            info = GSON.fromJson(jsonText, McmodInfo.class);
        } catch (JsonSyntaxException e) {
            try {
                info = new McmodInfo();
                info.modList = GSON.fromJson(jsonText, LIST_TYPE_TOKEN);
                if (info.modList == null) {
                    info = null;
                }
            } catch (Exception ee) {
                ee.printStackTrace();
                info = null;
            }
        } catch (Exception e) {
            info = null;
        }

        return info;
    }
}
