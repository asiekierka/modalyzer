# Modalyzer

Modalyzer is a tool for analyzing and gathering information from Minecraft mod files.

Currently, it supports the following methods of gathering metadata:

* mcmod.info files
* Forge @Mod annotations
* MCP mapping heuristics to determine sidedness/Minecraft version (requires MCP ZIPs to be downloaded manually)
* BaseMod class name to get mod ID/name (unless getName() is overridden in the class)
* (Optional) Generating SHA256 hashes of mods

For help, type "java -jar Modalyzer.jar --help".

Licensed under Apache 2.0.
