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

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
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
    public boolean hasCoremod;

    @Getter
    private transient List<String> versionCandidates = new ArrayList<>();
    public transient boolean valid;

    public void addVersionCandidate(String candidate) {
        versionCandidates.add(candidate);
    }

    public void addModLoaderStyleDependency(String dep) {
        if (dependencies == null) {
            dependencies = new HashMap<>();
        }

        String modName = dep;
        String modVersion = "*";
        if (dep.contains("@")) {
            modName = dep.split("@")[0];
            if (dep.split("@").length == 2) {
                modVersion = dep.split("@")[1];
            }
        }

        modName = modName.trim();
        modVersion = modVersion.trim();

        if (dependencies.containsKey(modName)) {
            if (!"*".equals(modVersion) && "*".equals(dependencies.get(modName))) {
                dependencies.put(modName, modVersion);
            }
        } else {
            dependencies.put(modName, modVersion);
        }
    }
}
