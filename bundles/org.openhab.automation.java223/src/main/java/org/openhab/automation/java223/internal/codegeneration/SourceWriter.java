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
package org.openhab.automation.java223.internal.codegeneration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.common.Java223Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Write java files in the lib directory.
 * Do not write if already the same
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class SourceWriter {

    public static final String HELPER_PACKAGE = "helper";

    private final Logger logger = LoggerFactory.getLogger(SourceWriter.class);

    // There is no memory issue in storing the whole source code here. The JVM deduplicate Strings
    // and sources are already stored in memory by our implementation of MemoryJavaFileObject
    protected final Map<String, String> generatedClassesSources = new HashMap<>();

    protected final Path folder;

    public SourceWriter(Path folder) {
        this.folder = folder;
    }

    public void createHelperDirectory() throws IOException {
        Files.createDirectories(getHelperPath());
    }

    public Path getHelperPath() {
        return getPath(HELPER_PACKAGE, null);
    }

    public void removeSourceFile(Path fullPath) {
        generatedClassesSources.remove(fullPath.toString());
    }

    protected synchronized void replaceHelperFileIfNotEqual(String packageName, String className, String generatedClass)
            throws IOException {
        String key = packageName + "." + className;

        if (sourceHasChangeOrIsNew(packageName, className, generatedClass)) {
            Path javaFile = getPath(packageName, className);

            Files.createDirectories(javaFile.getParent());
            try (FileOutputStream outFile = new FileOutputStream(javaFile.toFile())) {
                outFile.write(generatedClass.getBytes(StandardCharsets.UTF_8));
                logger.debug("Wrote generated class: {}", javaFile.toAbsolutePath());
            }
        } else {
            logger.debug("{} has not changed.", key);
        }
    }

    /**
     * Return the path to the file to write
     * Or the path to the folder if className is null
     * 
     * @param packageName Package name
     * @param className Class name
     * @return Path to the file to write or the path to the folder if className is null
     */
    protected Path getPath(String packageName, @Nullable String className) {
        String packageFolder = packageName.replace('.', File.separatorChar);
        if (className != null && !className.isEmpty()) {
            return folder.resolve(packageFolder + File.separator + className + "." + Java223Constants.JAVA_FILE_TYPE);
        } else {
            return folder.resolve(packageFolder);
        }
    }

    private boolean sourceHasChangeOrIsNew(String packageName, String className, String newSource) {
        String previousSource = generatedClassesSources.put(getPath(packageName, className).toString(), newSource);
        return !newSource.equals(previousSource);
    }

    /**
     * Get the full package name from the given package array path
     * 
     * @param packagePath Package path
     * @return The full package name
     */
    public static String getPackageName(String... packagePath) {
        String sanitizedPath = Stream.of(packagePath).map(part -> part.replaceAll("[^a-zA-Z0-9_]", "_"))
                .collect(Collectors.joining("."));
        return HELPER_PACKAGE + "." + sanitizedPath;
    }
}
