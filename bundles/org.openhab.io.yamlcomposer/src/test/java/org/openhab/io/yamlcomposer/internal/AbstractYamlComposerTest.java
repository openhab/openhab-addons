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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openhab.core.OpenHAB;
import org.openhab.io.yamlcomposer.internal.YamlComposer.CacheEntry;
import org.openhab.io.yamlcomposer.internal.directives.Directive;
import org.openhab.io.yamlcomposer.internal.placeholders.Placeholder;

/**
 * The {@link AbstractYamlComposerTest} contains common test utilities and setup for YAML composer tests.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
abstract class AbstractYamlComposerTest {
    private static final Pattern UNRESOLVED_STRING_PATTERN = Pattern
            .compile("(?s)^[A-Z].+(Placeholder|Directive)\\[.*");

    protected static final Path SOURCE_PATH = Path.of("src/test/resources/model/composer");
    protected @Nullable TestInfo currentTest = null; // so loadFixture can tell which test is calling it

    protected @TempDir @Nullable Path sharedTempDir = null; // Initialized to avoid null warning

    // logSession is used by loadFixture to capture logs during YAML loading,
    // which can be helpful for debugging test failures
    protected final LogSession logSession = new LogSession();

    @BeforeEach
    void setup(TestInfo testInfo) {
        this.currentTest = testInfo;
    }

    @AfterEach
    void tearDown() {
        logSession.close(); // This will flush logs to the console automatically
    }

    protected MockedStatic<OpenHAB> mockOpenHabMetadata() {
        MockedStatic<OpenHAB> openHABMock = Mockito.mockStatic(OpenHAB.class);
        openHABMock.when(OpenHAB::getVersion).thenReturn("5.3.0");
        openHABMock.when(OpenHAB::buildString).thenReturn("test-build");
        return openHABMock;
    }

    /**
     * Load a YAML fixture file from the test resources.
     * <p>
     * This helper method simplifies loading fixture files by automatically resolving the path
     * relative to the standard test resources directory and parsing the YAML content.
     * <p>
     * The method also includes enhanced error handling to provide clear context about
     * which test and fixture caused a failure, making it easier to diagnose issues
     * when a fixture cannot be loaded or parsed correctly.
     * <p>
     * The returned Map is guaranteed to contain no Placeholder or Directive instances, as the method will
     * fail the test if any unresolved items are found in the loaded data.
     * This ensures that all placeholders and directives are properly resolved before the test continues.
     *
     * @param source the name of the YAML file to load (relative to the fixture directory)
     * @return the parsed YAML content as a Map
     * @throws IOException if an error occurs reading the file
     */
    @SuppressWarnings({ "unchecked", "null" })
    protected Map<Object, @Nullable Object> loadFixture(Path source) throws IOException {
        // If 'source' is absolute (like a temp file), resolve() returns 'source' as is.
        // If it's relative, it appends it to SOURCE_PATH.
        Path filePath = source.isAbsolute() ? source : SOURCE_PATH.resolve(source);

        try {
            ConcurrentHashMap<Path, CacheEntry> includeCache = new ConcurrentHashMap<>();
            Object result = YamlComposer.load(filePath, path -> {
            }, env -> {
            }, logSession, includeCache);

            if (result instanceof Map<?, ?> dataMap) {
                Map<Object, @Nullable Object> map = (Map<Object, @Nullable Object>) dataMap;
                assertNoUnresolvedNodes(map);
                return map;
            }

            fail("Fixture did not produce a Map structure: " + source);
        } catch (Exception e) {
            fail("\n%s#%s: %s\nError loading fixture '%s': %s".formatted(
                    currentTest.getTestClass().get().getSimpleName(), currentTest.getTestMethod().get().getName(),
                    currentTest.getDisplayName(), source, e.getMessage()), e);
        }
        return Map.of();
    }

    protected Map<Object, @Nullable Object> loadYaml(String content) throws IOException {
        Path tempFile = Objects.requireNonNull(sharedTempDir).resolve("inline-test-" + System.nanoTime() + ".yaml");
        Files.writeString(tempFile, content);
        return loadFixture(tempFile);
    }

    protected @Nullable Object loadYamlValue(String content) throws IOException {
        Path tempFile = Objects.requireNonNull(sharedTempDir).resolve("inline-test-" + System.nanoTime() + ".yaml");
        Files.writeString(tempFile, content);

        ConcurrentHashMap<Path, CacheEntry> includeCache = new ConcurrentHashMap<>();
        Object result = YamlComposer.load(tempFile, path -> {
        }, env -> {
        }, logSession, includeCache);

        if (result != null) {
            assertNoUnresolvedNodes(result);
        }
        return result;
    }

    /**
     * Recursively assert that the given object graph contains no Placeholder or Directive
     * instances or stringified representations of them. Uses identity-based cycle detection
     * to avoid infinite loops on self-referencing structures.
     */
    protected void assertNoUnresolvedNodes(Object root) {
        Objects.requireNonNull(root);
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        ArrayDeque<String> path = new ArrayDeque<>();

        checkForUnresolvedNodes(root, visited, path);
    }

    protected void checkForUnresolvedNodes(Object obj, Set<Object> visited, ArrayDeque<String> path) {
        if (obj == null || obj instanceof Number || obj instanceof Boolean) {
            return;
        }

        if (obj instanceof String str) {
            // Check for instances where String.valueOf or toString was called on a Placeholder or Directive,
            // which would indicate an unresolved object that was coerced to a string during processing
            if (UNRESOLVED_STRING_PATTERN.matcher(str).matches()) {
                failAt(path, "unresolved string representation: " + str);
            }
            return;
        }

        if (!visited.add(obj)) {
            return;
        }

        if (obj instanceof Placeholder) {
            failAt(path, "unresolved Placeholder instance: " + obj.getClass().getName());
        } else if (obj instanceof Directive) {
            failAt(path, "unresolved Directive instance: " + obj.getClass().getName());
        }

        if (obj instanceof Map<?, ?> m) {
            for (Map.Entry<?, ?> e : m.entrySet()) {
                String keyName = String.valueOf(e.getKey());
                path.addLast(keyName);
                checkForUnresolvedNodes(e.getKey(), visited, path); // Check the key itself
                checkForUnresolvedNodes(e.getValue(), visited, path); // Check the value
                path.removeLast();
            }
        } else if (obj instanceof Iterable<?> it) {
            int i = 0;
            for (Object o : it) {
                path.addLast("[" + (i++) + "]");
                checkForUnresolvedNodes(o, visited, path);
                path.removeLast();
            }
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                path.addLast("[" + i + "]");
                checkForUnresolvedNodes(Array.get(obj, i), visited, path);
                path.removeLast();
            }
        }
    }

    protected void failAt(ArrayDeque<String> path, String reason) {
        TestInfo testInfo = Objects.requireNonNull(currentTest);
        String p = path.isEmpty() ? "" : " at " + String.join("/", path);
        fail("\n%s#%s: %s\nFound %s%s".formatted(testInfo.getTestClass().get().getSimpleName(),
                testInfo.getTestMethod().get().getName(), testInfo.getDisplayName(), reason, p));
    }

    /**
     * Writes a helper YAML file to the shared temporary directory.
     *
     * @param fileName The name of the file (e.g.,
     *            "subOverIncludeLiterals.inc.yaml")
     * @param content The YAML content for the include file
     */
    protected Path writeFixture(String fileName, String content) throws IOException {
        Path inputPath = Path.of(fileName);

        Path includePath = Objects.requireNonNull(sharedTempDir).resolve(inputPath).normalize();

        // Create parent directories if the filename contains a subfolder (e.g.,
        // "includes/file.yaml")
        Path parentDir = includePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        Files.writeString(includePath, content);
        return includePath;
    }

    /**
     * Retrieve a nested value from a map using the provided keys.
     * <p>
     * This method navigates through a map structure using the given keys in order.
     * If a key is not found or the current value is not a map, the method returns
     * {@code null}.
     *
     * @param data the map to retrieve the value from; must not be {@code null}.
     * @param key the sequence of keys to navigate the map; must not be
     *            {@code null}.
     * @return the nested value if found, or {@code null} if a key is missing or the
     *         value is not a map.
     */
    protected static @Nullable Object getNestedValue(Map<Object, @Nullable Object> data, String... key) {
        if (data == null) {
            return null;
        }
        Object value = data;
        for (String k : key) {
            if (value instanceof Map<?, ?> map) {
                value = map.get(k);
            } else {
                return null;
            }
        }
        return value;
    }
}
