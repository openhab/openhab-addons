/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.pythonscripting.internal.console.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngine;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineConfiguration;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineFactory;
import org.openhab.automation.pythonscripting.internal.console.handler.typing.ClassCollector;
import org.openhab.automation.pythonscripting.internal.console.handler.typing.ClassCollector.ClassContainer;
import org.openhab.automation.pythonscripting.internal.console.handler.typing.ClassConverter;
import org.openhab.core.automation.module.script.ScriptEngineContainer;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.io.console.Console;

/**
 * Update command implementations
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class TypingCmd {
    private final Logger logger;
    private final ScriptEngineManager scriptEngineManager;

    private static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();

    public TypingCmd(Logger logger, ScriptEngineManager scriptEngineManager) {
        this.logger = logger;
        this.scriptEngineManager = scriptEngineManager;
    }

    public void build() throws Exception {
        Path outputPath = PythonScriptEngineConfiguration.PYTHON_TYPINGS_PATH;

        // Cleanup Directory
        if (Files.isDirectory(outputPath)) {
            try (Stream<Path> paths = Files.walk(outputPath)) {
                paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        }
        ClassCollector collector = new ClassCollector(logger);

        Map<String, ClassContainer> fileContainerMap = new HashMap<String, ClassContainer>();
        Set<String> imports = new HashSet<String>();
        // Collect Bundle Classes
        Map<String, ClassContainer> bundleClassMap = collector.collectBundleClasses("org.openhab");
        for (ClassContainer container : bundleClassMap.values()) {
            ClassConverter converter = new ClassConverter(container);
            String classBody = converter.build();
            imports.addAll(converter.getImports());
            dumpClassContentToFile(classBody, container, outputPath, fileContainerMap);
        }

        Collection<String> _imports = dumpScope(outputPath);
        imports.addAll(_imports);

        imports = imports.stream().filter(i -> !i.startsWith("org.openhab")).collect(Collectors.toSet());
        Map<String, ClassContainer> reflectionClassMap = collector.collectReflectionClasses(imports);
        for (ClassContainer container : reflectionClassMap.values()) {
            ClassConverter converter = new ClassConverter(container);
            String classBody = converter.build();
            dumpClassContentToFile(classBody, container, outputPath, fileContainerMap);
        }

        // Generate __init__.py Files
        dumpInit(outputPath.toString(), fileContainerMap);

        logger.info(bundleClassMap.size() + " bundle and " + reflectionClassMap.size() + " java classes processed");
        logger.info("Total of " + (bundleClassMap.size() + reflectionClassMap.size()) + " type hint files created in '"
                + outputPath + "'");
    }

    private Collection<String> dumpScope(Path outputPath) throws IOException {
        Map<String, String> imports = new HashMap<String, String>();
        String identifier = "pythonscripting-cli-" + UUID.randomUUID().toString();
        ScriptEngineContainer container = scriptEngineManager.createScriptEngine(PythonScriptEngineFactory.SCRIPT_TYPE,
                identifier);
        if (container != null) {
            StringBuilder scopeBody = new StringBuilder();
            ScriptEngine engine = container.getScriptEngine();

            Map<String, Object> scope = ((PythonScriptEngine) engine).getScope();
            for (Entry<String, Object> entry : scope.entrySet()) {
                Object value = entry.getValue();
                String packageName;
                String pythonClassName;
                String pythonModuleName;
                String definition;

                if (value instanceof Class) {
                    packageName = ((Class) value).getName();
                    pythonClassName = ClassContainer.parsePythonClassName(packageName);
                    pythonModuleName = ClassContainer.parsePythonModuleName(packageName);
                    definition = entry.getKey() + ": Type = _" + pythonClassName;
                } else {
                    Class<? extends Object> cls = value.getClass();
                    packageName = value.getClass().getName();

                    if (packageName
                            .equals("org.openhab.automation.pythonscripting.internal.provider.LifecycleTracker")) {
                        packageName = "org.openhab.core.automation.module.script.LifecycleScriptExtensionProvider_LifecycleTracker";
                    } else if (packageName.endsWith("Impl") || packageName.endsWith("Delegate")) {
                        cls = value.getClass().getInterfaces()[0];
                        packageName = cls.getName();
                    }

                    pythonClassName = ClassContainer.parsePythonClassName(packageName);
                    pythonModuleName = ClassContainer.parsePythonModuleName(packageName);

                    if (cls.isEnum()) {
                        definition = entry.getKey() + ": _" + pythonClassName + " = " + "_" + pythonClassName + "."
                                + entry.getKey();
                    } else {
                        definition = entry.getKey() + ": _" + pythonClassName;
                    }
                }

                imports.put(packageName,
                        "from " + pythonModuleName + " import " + pythonClassName + " as _" + pythonClassName);

                String classUrl = ClassConverter.buildDocumentationLink(packageName);

                scopeBody.append(definition);
                scopeBody.append("\n");
                scopeBody.append("\"\"\"\n");
                scopeBody.append("Java class: ").append(packageName).append("\n\n");
                scopeBody.append("Java doc: ").append(classUrl).append("\n");
                scopeBody.append("\"\"\"\n\n");
            }

            scopeBody.insert(0, "\n\n");
            scopeBody.insert(0, "from typing import Type");
            scopeBody.insert(0, ClassConverter.buildClassImports(imports.values()));

            dumpContentToFile(scopeBody.toString(), outputPath.resolve("scope.py"));
        }
        scriptEngineManager.removeEngine(identifier);

        return imports.keySet();
    }

    public void dumpInit(String path, Map<String, ClassContainer> fileContainerMap) throws IOException {
        File root = new File(path);
        File[] list = root.listFiles();
        if (list != null) {
            ArrayList<File> files = new ArrayList<File>();
            for (File f : list) {
                if (f.isDirectory()) {
                    dumpInit(f.getAbsolutePath(), fileContainerMap);
                } else {
                    files.add(f);
                }
            }

            if (!files.isEmpty()) {
                StringBuilder initBody = new StringBuilder();
                // List<String> modules = new ArrayList<String>();
                for (File file : files) {
                    if (file.toString().endsWith("__init__.py") || file.toString().endsWith("scope.py")) {
                        continue;
                    }
                    ClassContainer container = fileContainerMap.get(file.toString());
                    initBody.append("from .__" + container.getPythonClassName().toLowerCase() + "__ import "
                            + container.getPythonClassName() + "\n");
                }

                String packageUrl = path.replace(".", PATH_SEPARATOR) + "/__init__.py";
                dumpContentToFile(initBody.toString(), Paths.get(packageUrl));
            }
        }
    }

    private void dumpClassContentToFile(String classBody, ClassContainer container, Path outputPath,
            Map<String, ClassContainer> fileContainerMap) throws IOException {
        if (classBody.isEmpty()) {
            return;
        }

        String modulePath = container.getPythonModuleName().replace(".", PATH_SEPARATOR);
        Path path = outputPath.resolve(modulePath)
                .resolve("__" + container.getPythonClassName().toLowerCase() + "__.py");

        fileContainerMap.put(path.toString(), container);

        dumpContentToFile(classBody, path);
    }

    private void dumpContentToFile(String content, Path path) throws IOException {
        Path parent = path.getParent();
        File directory = parent.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        Files.write(path, content.getBytes());
    }

    public static class Logger {
        private Object logger;

        public Logger(Console console) {
            this.logger = console;
        }

        public Logger(org.slf4j.Logger logger) {
            this.logger = logger;
        }

        public void info(String s) {
            if (logger instanceof Console console) {
                console.println("INFO: " + s);
            } else {
                ((org.slf4j.Logger) logger).info("{}", s);
            }
        }

        public void warn(String s) {
            if (logger instanceof Console console) {
                console.println("WARN: " + s);
            } else {
                ((org.slf4j.Logger) logger).warn("{}", s);
            }
        }
    }
}
