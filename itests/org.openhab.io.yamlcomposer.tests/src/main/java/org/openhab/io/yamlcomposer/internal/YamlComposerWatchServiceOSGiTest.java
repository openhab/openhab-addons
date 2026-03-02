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
package org.openhab.io.yamlcomposer.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.service.WatchService.Kind;
import org.openhab.core.test.java.JavaOSGiTest;

/**
 * OSGi integration tests for {@link YamlComposerWatchService}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class YamlComposerWatchServiceOSGiTest extends JavaOSGiTest {
    private static final String TEST_DIR_NAME = "yamlcomposer-itest";

    private YamlComposerWatchService watchService;
    private Path testSourceDir;
    private Path testOutputDir;

    @BeforeEach
    public void setUp() throws IOException {
        watchService = getService(YamlComposerWatchService.class);
        assertNotNull(watchService);

        testSourceDir = ComposerConfig.sourceRoot().resolve(TEST_DIR_NAME);
        testOutputDir = ComposerConfig.outputRoot().resolve(TEST_DIR_NAME);

        deleteRecursively(testSourceDir);
        deleteRecursively(testOutputDir);

        Files.createDirectories(testSourceDir.resolve("defs"));
        Files.createDirectories(testOutputDir);
    }

    @AfterEach
    public void tearDown() throws IOException {
        deleteRecursively(testSourceDir);
        deleteRecursively(testOutputDir);
    }

    @Test
    public void shouldGenerateOutputFromSourceAndRecompileOnIncludeChange() throws IOException {
        Path includeFile = testSourceDir.resolve("defs/items.inc.yaml");
        Path mainFile = testSourceDir.resolve("main.yaml");
        Path generatedFile = testOutputDir.resolve("main.yaml");

        Files.writeString(includeFile, """
                - type: Switch
                  name: MySwitch
                  label: Label One
                """);
        Files.writeString(mainFile, """
                version: 1
                items: !include defs/items.inc.yaml
                """);

        watchService.processWatchEvent(Kind.CREATE, mainFile);

        waitForAssert(() -> {
            assertTrue(Files.exists(generatedFile));
            assertTrue(fileContains(generatedFile, "Label One"));
        });

        Files.writeString(includeFile, """
                - type: Switch
                  name: MySwitch
                  label: Label Two
                """);

        watchService.processWatchEvent(Kind.MODIFY, includeFile);

        waitForAssert(() -> {
            assertTrue(Files.exists(generatedFile));
            assertTrue(fileContains(generatedFile, "Label Two"));
        });
    }

    @Test
    public void shouldRemoveGeneratedOutputOnSourceDelete() throws IOException {
        Path mainFile = testSourceDir.resolve("delete-me.yaml");
        Path generatedFile = testOutputDir.resolve("delete-me.yaml");

        Files.writeString(mainFile, """
                version: 1
                items:
                  - type: Switch
                    name: DeleteMe
                """);

        watchService.processWatchEvent(Kind.CREATE, mainFile);

        waitForAssert(() -> assertTrue(Files.exists(generatedFile)));

        watchService.processWatchEvent(Kind.DELETE, mainFile);

        waitForAssert(() -> assertFalse(Files.exists(generatedFile)));
    }

    private static boolean fileContains(Path file, String expectedContent) {
        try {
            return Files.readString(file).contains(expectedContent);
        } catch (IOException e) {
            throw new AssertionError("Failed to read file " + file, e);
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
