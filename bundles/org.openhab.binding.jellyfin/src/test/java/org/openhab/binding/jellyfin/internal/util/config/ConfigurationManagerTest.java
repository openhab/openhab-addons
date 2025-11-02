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
package org.openhab.binding.jellyfin.internal.util.config;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;

/**
 * Unit tests for {@link ConfigurationManager}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class ConfigurationManagerTest {

    private ConfigurationManager manager;
    private Configuration baseConfig;

    @BeforeEach
    void setUp() {
        manager = new ConfigurationManager();
        baseConfig = new Configuration();
        baseConfig.hostname = "original.example.com";
        baseConfig.port = 8096;
        baseConfig.ssl = false;
        baseConfig.path = "/jellyfin";
    }

    @Test
    void testAnalyzeWithUriExtractor() throws Exception {
        URI uri = new URI("https://new.example.com:9000/newpath");
        UriConfigurationExtractor extractor = new UriConfigurationExtractor();

        ConfigurationUpdate update = manager.analyze(extractor, uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.hostname());
        assertEquals(9000, update.port());
        assertTrue(update.ssl());
        assertEquals("/newpath", update.path());
    }

    @Test
    void testAnalyzeWithSystemInfoExtractor() {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setServerName("new.example.com");
        SystemInfoConfigurationExtractor extractor = new SystemInfoConfigurationExtractor();

        ConfigurationUpdate update = manager.analyze(extractor, systemInfo, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.hostname());
        assertEquals(8096, update.port()); // Preserved from base config
        assertFalse(update.ssl()); // Preserved from base config
        assertEquals("/jellyfin", update.path()); // Preserved from base config
    }

    @Test
    void testAnalyzeWithNoChanges() throws Exception {
        URI uri = new URI("http://original.example.com:8096/jellyfin");
        UriConfigurationExtractor extractor = new UriConfigurationExtractor();

        ConfigurationUpdate update = manager.analyze(extractor, uri, baseConfig);

        assertFalse(update.hasChanges());
    }

    @Test
    void testAnalyzeWithCustomExtractor() {
        // Test with a custom extractor to verify the generic pattern works
        ConfigurationExtractor<String> customExtractor = (source, current) -> {
            // Simple custom extractor that sets hostname to the source string
            boolean hasChanges = !source.equals(current.hostname);
            return new ConfigurationUpdate(source, current.port, current.ssl, current.path, hasChanges);
        };

        String newHostname = "custom.example.com";
        ConfigurationUpdate update = manager.analyze(customExtractor, newHostname, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("custom.example.com", update.hostname());
        assertEquals(8096, update.port());
        assertFalse(update.ssl());
        assertEquals("/jellyfin", update.path());
    }

    @Test
    void testAnalyzeLogsChanges() throws Exception {
        // This test primarily verifies that analyze doesn't throw exceptions
        // Actual logging verification would require a logging framework mock
        URI uri = new URI("https://new.example.com:9000/newpath");
        UriConfigurationExtractor extractor = new UriConfigurationExtractor();

        assertDoesNotThrow(() -> manager.analyze(extractor, uri, baseConfig));
    }
}
