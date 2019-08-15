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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import pl.asie.modalyze.ModAnalyzer;
import pl.asie.modalyze.ModAnalyzerUtils;
import pl.asie.modalyze.ModMetadata;
import pl.asie.modalyze.StringUtils;
import pl.asie.modalyze.mcp.MCPUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModLoaderClassesProcessor {
	@Getter
	private final ModMetadata metadata;

	public ModLoaderClassesProcessor(ModMetadata metadata) {
		this.metadata = metadata;
	}

	public ClassVisitor getClassVisitor() {
		return new ModClassVisitor();
	}

	private class ModClassVisitor extends ClassVisitor {
		private String superName, className;
		private boolean isBaseMod, useClassNameAsModName;

		public ModClassVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public void visit(int version, int access, String name, String signature,
		                  String superName, String[] interfaces) {
			super.visit(version, access, name, signature, superName, interfaces);

			this.superName = superName;
			this.className = name;
			if (superName.endsWith("BaseMod") || superName.endsWith("BaseModMp") || superName.equals("forge/NetworkMod")) {
				isBaseMod = true;
				useClassNameAsModName = true;
				metadata.valid = true;
			}
		}

		@Override
		public void visitEnd() {
			if (useClassNameAsModName) {
				String[] data = className.split("/");
				metadata.modid = metadata.name = StringUtils.select(metadata.name, data[data.length - 1]);
			}
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
		                                 String signature, String[] exceptions) {
			if (useClassNameAsModName && name.equals("getName")) {
				// getName() is overridden so it is not reliable
				useClassNameAsModName = false;
			}

			return super.visitMethod(access, name, desc, signature, exceptions);
		}
	}
}
