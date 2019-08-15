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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MCPHeuristicsProcessor {
	@Getter
	private final ModMetadata metadata;
	@Getter
	private final Set<String> keys = new HashSet<>();

	public MCPHeuristicsProcessor(ModMetadata metadata) {
		this.metadata = metadata;
	}

	public ClassVisitor getClassVisitor() {
		return new ModClassVisitor();
	}

	public class ModHMethodVisitor extends MethodVisitor {
		public ModHMethodVisitor(MethodVisitor mv) {
			super(Opcodes.ASM5, mv);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name,
		                            String desc, boolean itf) {
			keys.add(MCPUtils.getMethodKey(owner + "/" + name, desc));
		}
	}

	public class ModClassVisitor extends ClassVisitor {
		private String className;

		public ModClassVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public void visit(int version, int access, String name, String signature,
		                  String superName, String[] interfaces) {
			super.visit(version, access, name, signature, superName, interfaces);
			this.className = name;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
		                                 String signature, String[] exceptions) {
			// potential override
			if (name.startsWith("func_")) {
				keys.add(MCPUtils.getMethodKey(className + "/" + name, desc));
			}

			return new ModHMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions));
		}
	}

}
