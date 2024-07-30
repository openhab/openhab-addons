/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.solarman.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarman.internal.defmodel.InverterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class DefinitionParserTest {
    private final Logger logger = LoggerFactory.getLogger(DefinitionParserTest.class);
    private final DefinitionParser definitionParser = new DefinitionParser();

    @Test
    void testInverterDefinitionsCanBeLoaded() throws IOException {
        List<String> yamlFiles = scanForYamlFiles("definitions");
        List<String> definitionIds = extractDefinitionIdFromYamlFiles(yamlFiles);

        assertFalse(definitionIds.isEmpty());

        definitionIds.forEach(definitionId -> {
            @Nullable
            InverterDefinition inverterDefinition = definitionParser.parseDefinition(definitionId);
            assertNotNull(inverterDefinition);
        });
    }

    public static List<String> extractDefinitionIdFromYamlFiles(List<String> yamlFiles) {
        Pattern pattern = Pattern.compile("definitions/(.*)\\.yaml");

        return yamlFiles.stream().map(file -> {
            Matcher matcher = pattern.matcher(file);
            return matcher.matches() ? matcher.group(1) : file;
        }).collect(Collectors.toList());
    }

    public List<String> scanForYamlFiles(String directoryPath) throws IOException {
        List<String> yamlFiles = new ArrayList<>();
        ClassLoader classLoader = Objects.requireNonNull(DefinitionParserTest.class.getClassLoader());
        Enumeration<URL> resources = classLoader.getResources(directoryPath);

        Collections.list(resources).stream().flatMap(resource -> {
            try {
                if (resource.getProtocol().equals("jar")) {
                    String path = resource.getPath();
                    String jarPath = path.substring(5, path.indexOf("!"));
                    try (JarFile jarFile = new JarFile(jarPath)) {
                        return jarFile.stream()
                                .filter(e -> e.getName().startsWith(directoryPath) && e.getName().endsWith(".yaml"))
                                .map(JarEntry::getName);
                    }
                } else if (resource.getProtocol().equals("file")) {
                    return scanDirectory(directoryPath).stream();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return Stream.empty();
        }).forEach(yamlFiles::add);

        return yamlFiles;
    }

    private static List<String> scanDirectory(String directoryPath) throws IOException {
        URL url = Objects.requireNonNull(DefinitionParserTest.class.getClassLoader()).getResource(directoryPath);
        if (url == null) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }
        String[] files = new java.io.File(url.getPath()).list((dir, name) -> name.endsWith(".yaml"));
        if (files != null) {
            return Arrays.stream(files).map(file -> directoryPath + "/" + file).toList();
        }
        return Collections.emptyList();
    }
}
