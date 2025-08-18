/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pythonscripting.internal.PythonScriptEngineConfiguration;
import org.openhab.automation.pythonscripting.internal.console.handler.typing.ClassCollector;
import org.openhab.automation.pythonscripting.internal.console.handler.typing.ClassCollector.ClassContainer;
import org.openhab.automation.pythonscripting.internal.console.handler.typing.ClassConverter;
import org.openhab.core.io.console.Console;

/**
 * Update command implementations
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class TypingCmd {
    private final Logger logger;

    private static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();

    public TypingCmd(Logger logger) {
        this.logger = logger;
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
        logger.info("Total of " + (bundleClassMap.size() + reflectionClassMap.size()) + " type hint files create in '"
                + outputPath + "'");
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
                    if (file.toString().endsWith("__init__.py")) {
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
