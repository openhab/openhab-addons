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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openhab.core.OpenHAB;
import org.openhab.core.service.WatchService;

@DisplayName("YAML Composer Watch Service")
class YamlComposerWatchServiceTest extends AbstractYamlComposerTest {
    private static final String LONG_LABEL = "A long label value designed for configured width";

    @AfterEach
    void resetComposerRoots() {
        ComposerConfig.resetRootsForTesting();
    }

    @Test
    @DisplayName("Rewrites existing output when formatting settings change")
    void rewritesExistingOutputWhenFormattingSettingsChange() throws Exception {
        try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
            Path configRoot = Files.createDirectories(Objects.requireNonNull(sharedTempDir).resolve("conf"));
            openHABMock.when(OpenHAB::getConfigFolder).thenReturn(configRoot.toString());
            openHABMock.when(OpenHAB::getUserDataFolder).thenReturn(configRoot.toString());
            ComposerConfig.setRootsForTesting(configRoot, configRoot);

            Path sourceRoot = Files.createDirectories(ComposerConfig.sourceRoot());
            Path source = sourceRoot.resolve("formatting.yaml");
            Files.writeString(source, """
                    items:
                      First:
                        label: %s
                      Second:
                        type: Switch
                    things:
                      demo:
                        label: Demo
                    """.formatted(LONG_LABEL));

            WatchService watchService = mock(WatchService.class);
            YamlComposerWatchService composer = new YamlComposerWatchService(watchService, Map.of());
            try {
                Path output = ComposerConfig.resolveOutputPath(source);
                String initialYaml = yamlBody(Files.readString(output));
                assertTrue(initialYaml.contains("label: " + LONG_LABEL));
                assertTrue(initialYaml.contains("\n\n  Second:"));

                composer.modified(Map.of("maxLineWidth", 30, "splitLines", true, "sectionSpacing", 2));
                String wrappedYaml = yamlBody(Files.readString(output));
                assertFalse(wrappedYaml.contains("label: " + LONG_LABEL));
                assertTrue(wrappedYaml.contains("\n\n\n  Second:"));
                assertTrue(wrappedYaml.contains("\n\n\nthings:"));

                composer.modified(Map.of("maxLineWidth", 30, "splitLines", false, "sectionSpacing", 2));
                String unsplitYaml = yamlBody(Files.readString(output));
                assertTrue(unsplitYaml.contains("label: " + LONG_LABEL));
                assertTrue(unsplitYaml.contains("\n\n\n  Second:"));
            } finally {
                composer.deactivate();
            }
        }
    }

    private static String yamlBody(String generatedFile) {
        return generatedFile.substring(generatedFile.indexOf("\n\n") + 2);
    }
}
