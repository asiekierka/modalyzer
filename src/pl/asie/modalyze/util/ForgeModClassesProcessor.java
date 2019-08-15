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

package pl.asie.modalyze.util;

import lombok.Getter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import pl.asie.modalyze.ModAnalyzerUtils;
import pl.asie.modalyze.ModMetadata;
import pl.asie.modalyze.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForgeModClassesProcessor {
	public static final String DESC_125_BETAS = "Lfml/Mod;";
	public static final String DESC_PRE_18 = "Lcpw/mods/fml/common/Mod;";
	public static final String DESC_POST_18 = "Lnet/minecraftforge/fml/common/Mod;";

	private static final List<String> FORGE_MOD_ANNOTATIONS = Arrays.asList(DESC_125_BETAS, DESC_PRE_18, DESC_POST_18);

	@Getter
	private final ModMetadata metadata;
	@Getter
	private String forgeModAnnotation;

	public ForgeModClassesProcessor(ModMetadata metadata) {
		this.metadata = metadata;
	}

	public ClassVisitor getClassVisitor() {
		return new ModClassVisitor();
	}

	public boolean isMatchingMinecraftVersion(String s) {
		if (forgeModAnnotation == null) {
			return true;
		}

		if (s.startsWith("a") || s.startsWith("b")) {
			return false;
		}

		try {
			List<Integer> parsedMcVersion = Stream.of(s.split("-")[0].split("\\.")).map(Integer::parseInt).collect(Collectors.toList());

			switch (forgeModAnnotation) {
				case DESC_125_BETAS:
					return parsedMcVersion.size() >= 2 && parsedMcVersion.get(0) == 1 && parsedMcVersion.get(1) == 2;
				case DESC_PRE_18:
					return parsedMcVersion.size() >= 2 && parsedMcVersion.get(0) == 1 && parsedMcVersion.get(1) >= 2 && parsedMcVersion.get(1) < 8;
				case DESC_POST_18:
					return parsedMcVersion.size() >= 2 && parsedMcVersion.get(0) == 1 && parsedMcVersion.get(1) >= 8;
				default:
					return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private class ModAnnotationVisitor extends AnnotationVisitor {
		private Map<String, Object> data = new HashMap<>();

		public ModAnnotationVisitor(AnnotationVisitor av) {
			super(Opcodes.ASM5, av);
		}

		@Override
		public void visit(String name, Object value) {
			super.visit(name, value);
			data.put(name, value);
		}

		@Override
		public void visitEnum(String name, String desc, String value) {
			super.visitEnum(name, desc, value);
			data.put(name, value);
		}

		@Override
		public void visitEnd() {
			super.visitEnd();
			metadata.valid = true;

			if (data.containsKey("modid")) {
				metadata.modid = (String) data.get("modid"); // always more accurate
				metadata.provides = StringUtils.append(metadata.provides, (String) data.get("modid"));
			}

			if (data.containsKey("version")) {
				String v = (String) data.get("version");
				if (v != null && v.length() > 0) {
					metadata.addVersionCandidate(v);
				}
			}

			String dependencyStr = data.containsKey("dependencies") ? ((String) data.get("dependencies"))
					: (data.containsKey("dependsOn") ? ((String) data.get("dependsOn")) : null);

			if (dependencyStr != null) {
				String[] dependencies = dependencyStr.split(";");
				for (String s : dependencies) {
					String[] dep = s.split(":");
					if (dep.length == 2 && dep[0].startsWith("require")) {
						// ModLoader used "require-" instead of "required-"
						metadata.addModLoaderStyleDependency(dep[1]);
					}
				}
			}

			if (data.containsKey("acceptedMinecraftVersions")
					&& ModAnalyzerUtils.isValidMcVersion((String) data.get("acceptedMinecraftVersions"))) {
				metadata.addModLoaderStyleDependency("minecraft@" + data.get("acceptedMinecraftVersions"));
			}

			if (data.containsKey("clientSideOnly")) {
				if (((boolean) data.get("clientSideOnly")) == true) {
					metadata.side = "client";
				}
			} else if (data.containsKey("serverSideOnly")) {
				if (((boolean) data.get("serverSideOnly")) == true) {
					metadata.side = "server";
				}
			}
		}
	}

	private class ModClassVisitor extends ClassVisitor {
		public ModClassVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			AnnotationVisitor visitor = super.visitAnnotation(desc, visible);

			if (FORGE_MOD_ANNOTATIONS.contains(desc)) {
				forgeModAnnotation = desc;
				return new ModAnnotationVisitor(visitor);
			} else {
				return visitor;
			}
		}
	}
}
