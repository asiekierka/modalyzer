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

    public transient boolean valid;
}
