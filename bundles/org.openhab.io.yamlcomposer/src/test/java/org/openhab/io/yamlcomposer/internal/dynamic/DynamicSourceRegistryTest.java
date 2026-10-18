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
package org.openhab.io.yamlcomposer.internal.dynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@NonNullByDefault
class DynamicSourceRegistryTest {

    private @Nullable DynamicSourceRegistry registry;

    private @Nullable TestSourceProvider thingsProvider;

    @BeforeEach
    void setUp() {
        TestSourceProvider localThingsProvider = new TestSourceProvider("things",
                List.of(Map.of("UID", "tapo:light:porch", "label", "Porch Light"),
                        Map.of("UID", "other:light:desk", "label", "Desk Light")));

        thingsProvider = localThingsProvider;

        // Pass 0 debounce delay for synchronous test execution
        registry = new DynamicSourceRegistry(Map.of("things", localThingsProvider), 0);
    }

    @Test
    void reportsSupportedSourcesCorrectly() {
        assertTrue(registry.supportsSource("things"));
        assertFalse(registry.supportsSource("unsupported_source"));
    }

    @Test
    void returnsSourceMapForSupportedSource() {
        Map<String, Map<String, @Nullable Object>> sourceMap = registry.getSourceMap("things");
        assertNotNull(sourceMap);
        assertEquals(2, sourceMap.size());

        Map<String, @Nullable Object> porch = sourceMap.get("tapo:light:porch");
        assertNotNull(porch);
        assertEquals("Porch Light", porch.get("label"));

        assertNull(registry.getSourceMap("unsupported_source"));
    }

    @Test
    void sourceMapIsUnmodifiable() {
        Map<String, Map<String, @Nullable Object>> sourceMap = registry.getSourceMap("things");
        assertNotNull(sourceMap);

        assertThrows(UnsupportedOperationException.class, () -> sourceMap.put("new:uid", Map.of()));
    }

    @Test
    void notifiesDependentFilesOnEntityChange() {
        Path porchFile = Path.of("porch.yaml");

        registry.registerDependency(porchFile, "things");

        List<Path> invalidatedPaths = new ArrayList<>();
        registry.setOnFileRecompileListener(invalidatedPaths::add);

        // Fire entity change matching things
        Map<String, @Nullable Object> oldEntity = Map.of("UID", "tapo:light:porch", "label", "Porch Light");
        Map<String, @Nullable Object> newEntity = Map.of("UID", "tapo:light:porch", "label", "Updated Porch Light");

        thingsProvider.fireEntityChange(oldEntity, newEntity);

        assertEquals(List.of(porchFile), invalidatedPaths, "Only porch.yaml should be invalidated");
    }

    @Test
    void unregistersFileSources() {
        Path porchFile = Path.of("porch.yaml");
        registry.registerDependency(porchFile, "things");

        registry.unregisterFileSources(porchFile);

        List<Path> invalidatedPaths = new ArrayList<>();
        registry.setOnFileRecompileListener(invalidatedPaths::add);

        thingsProvider.fireEntityChange(Map.of(), Map.of("UID", "tapo:light:porch"));

        assertTrue(invalidatedPaths.isEmpty(), "Unregistered file should not be invalidated");
    }

    @Test
    void supersedesTaskAndPreventsStaleCallbackExecution() throws InterruptedException {
        // Create registry with a short debounce delay (e.g., 50ms)
        TestSourceProvider provider = new TestSourceProvider("things", List.of());
        DynamicSourceRegistry debouncedRegistry = new DynamicSourceRegistry(Map.of("things", provider), 50);

        Path file = Path.of("porch.yaml");
        debouncedRegistry.registerDependency(file, "things");

        List<Path> recompiledPaths = new ArrayList<>();
        debouncedRegistry.setOnFileRecompileListener(recompiledPaths::add);

        // Fire first change event (creates Task 1)
        provider.fireEntityChange(Map.of(), Map.of("UID", "tapo:light:porch"));

        // Fire second change event 10ms later (supersedes Task 1 with Task 2)
        Thread.sleep(10);
        provider.fireEntityChange(Map.of(), Map.of("UID", "tapo:light:porch", "label", "Updated"));

        // Wait for Task 2 to complete
        Thread.sleep(100);

        // Assert that the listener was only invoked once (Task 1 was disarmed)
        assertEquals(1, recompiledPaths.size(), "Listener should only fire once for superseded tasks");
        assertEquals(file, recompiledPaths.get(0));

        debouncedRegistry.clear();
    }

    @Test
    void clearDisarmsPendingTasksWithoutFiringListener() throws InterruptedException {
        TestSourceProvider provider = new TestSourceProvider("things", List.of());
        DynamicSourceRegistry debouncedRegistry = new DynamicSourceRegistry(Map.of("things", provider), 50);

        Path file = Path.of("porch.yaml");
        debouncedRegistry.registerDependency(file, "things");

        List<Path> recompiledPaths = new ArrayList<>();
        debouncedRegistry.setOnFileRecompileListener(recompiledPaths::add);

        // Fire change event
        provider.fireEntityChange(Map.of(), Map.of("UID", "tapo:light:porch"));

        // Immediately clear/cancel before timer expires
        debouncedRegistry.clear();

        // Wait past the timer expiration
        Thread.sleep(100);

        assertTrue(recompiledPaths.isEmpty(), "Pending task should not fire after registry is cleared");
    }

    private static class TestSourceProvider implements DynamicSourceProvider<Map<String, @Nullable Object>> {
        private final String source;
        private final Collection<Map<String, @Nullable Object>> entities;
        private @Nullable Consumer<EntityChange> listener;

        TestSourceProvider(String source, Collection<Map<String, @Nullable Object>> entities) {
            this.source = source;
            this.entities = entities;
        }

        @Override
        public String getSourceName() {
            return source;
        }

        @Override
        public boolean supportsSource(String requestedSource) {
            return source.equals(requestedSource);
        }

        @Override
        public Collection<Map<String, @Nullable Object>> getAllEntities() {
            return entities;
        }

        @Override
        public String getKey(Map<String, @Nullable Object> entity) {
            Object uid = entity.get("UID");
            return uid != null ? uid.toString() : "";
        }

        @Override
        public Map<String, @Nullable Object> adaptToMap(Map<String, @Nullable Object> entity) {
            return entity;
        }

        @Override
        public void setOnChangeListener(Consumer<EntityChange> listener) {
            this.listener = listener;
        }

        void fireEntityChange(@Nullable Map<String, @Nullable Object> oldEntity,
                @Nullable Map<String, @Nullable Object> newEntity) {
            Consumer<EntityChange> currentListener = listener;
            if (currentListener != null) {
                currentListener.accept(new EntityChange(source, oldEntity, newEntity));
            }
        }
    }
}
