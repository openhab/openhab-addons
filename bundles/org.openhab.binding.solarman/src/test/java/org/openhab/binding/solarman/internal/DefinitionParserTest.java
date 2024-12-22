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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarman.internal.defmodel.InverterDefinition;

/**
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class DefinitionParserTest {
    private static final String YAML_EXTENSION = ".yaml";
    private static final String DIRECTORY_PREFIX = "definitions/";

    private final DefinitionParser definitionParser = new DefinitionParser();

    @Test
    void testInverterDefinitionsCanBeLoaded() throws IOException {
        List<String> yamlFiles = scanForYamlFiles(DIRECTORY_PREFIX);
        List<String> definitionIds = extractDefinitionIdFromYamlFiles(yamlFiles);

        assertFalse(definitionIds.isEmpty(), "No YAML files found in directory: " + DIRECTORY_PREFIX);

        definitionIds.forEach(definitionId -> {
            @Nullable
            InverterDefinition inverterDefinition = definitionParser.parseDefinition(definitionId);
            assertNotNull(inverterDefinition, "Failed to parse inverter definition: " + definitionId);
        });
    }

    public static List<String> extractDefinitionIdFromYamlFiles(List<String> yamlFiles) {
        Pattern pattern = Pattern.compile("definitions/(.*)\\" + YAML_EXTENSION);

        return yamlFiles.stream().map(file -> {
            Matcher matcher = pattern.matcher(file);
            return matcher.matches() ? matcher.group(1) : file;
        }).toList();
    }

    public List<String> scanForYamlFiles(String directoryPath) throws IOException {
        List<String> yamlFiles = new ArrayList<>();
        ClassLoader classLoader = Objects.requireNonNull(DefinitionParserTest.class.getClassLoader());
        Enumeration<URL> resources = classLoader.getResources(directoryPath);

        Collections.list(resources).stream().flatMap(resource -> {
            try {
                if ("jar".equals(resource.getProtocol())) {
                    String path = resource.getPath();
                    String jarPath = path.substring(path.indexOf("file:") + 5, path.indexOf("!"));
                    try (JarFile jarFile = new JarFile(jarPath)) {
                        return jarFile.stream().filter(
                                e -> e.getName().startsWith(directoryPath) && e.getName().endsWith(YAML_EXTENSION))
                                .map(JarEntry::getName);
                    }
                } else if ("file".equals(resource.getProtocol())) {
                    return scanDirectory(directoryPath).stream();
                }
            } catch (IOException | URISyntaxException e) {
                throw new IllegalStateException("Error processing resource: " + resource, e);
            }
            return Stream.empty();
        }).forEach(yamlFiles::add);

        return yamlFiles;
    }

    private static List<String> scanDirectory(String directoryPath) throws IOException, URISyntaxException {
        URL url = Objects.requireNonNull(DefinitionParserTest.class.getClassLoader()).getResource(directoryPath);
        Path directory = Paths.get(url.toURI());

        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        try (Stream<Path> stream = Files.list(directory)) {
            List<String> files = stream.filter(file -> file.getFileName().toString().endsWith(YAML_EXTENSION))
                    .map(file -> directoryPath + "/" + file.getFileName().toString()).toList();
            return files;
        }
    }
}
