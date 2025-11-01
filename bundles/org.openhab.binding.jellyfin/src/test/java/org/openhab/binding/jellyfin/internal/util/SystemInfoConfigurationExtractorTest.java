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
package org.openhab.binding.jellyfin.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;

/**
 * Unit tests for {@link SystemInfoConfigurationExtractor}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class SystemInfoConfigurationExtractorTest {

    private SystemInfoConfigurationExtractor extractor;
    private Configuration baseConfig;

    @BeforeEach
    void setUp() {
        extractor = new SystemInfoConfigurationExtractor();
        baseConfig = new Configuration();
        baseConfig.hostname = "original.example.com";
        baseConfig.port = 8096;
        baseConfig.ssl = false;
        baseConfig.path = "/jellyfin";
    }

    @Test
    void testExtractWithServerNameChange() {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setServerName("new.example.com");

        ConfigurationUpdate update = extractor.extract(systemInfo, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.hostname());
        assertEquals(8096, update.port()); // Port should be preserved
        assertFalse(update.ssl()); // SSL should be preserved
        assertEquals("/jellyfin", update.path()); // Path should be preserved
    }

    @Test
    void testExtractWithNoServerNameChange() {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setServerName("original.example.com");

        ConfigurationUpdate update = extractor.extract(systemInfo, baseConfig);

        assertFalse(update.hasChanges());
        assertEquals("original.example.com", update.hostname());
        assertEquals(8096, update.port());
        assertFalse(update.ssl());
        assertEquals("/jellyfin", update.path());
    }

    @Test
    void testExtractWithNullServerName() {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setServerName(null);

        ConfigurationUpdate update = extractor.extract(systemInfo, baseConfig);

        assertFalse(update.hasChanges());
        assertEquals("original.example.com", update.hostname()); // Should preserve original
        assertEquals(8096, update.port());
        assertFalse(update.ssl());
        assertEquals("/jellyfin", update.path());
    }

    @Test
    void testExtractPreservesOtherFields() {
        baseConfig.port = 9000;
        baseConfig.ssl = true;
        baseConfig.path = "/custom";

        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setServerName("new.example.com");

        ConfigurationUpdate update = extractor.extract(systemInfo, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.hostname());
        assertEquals(9000, update.port()); // Preserved
        assertTrue(update.ssl()); // Preserved
        assertEquals("/custom", update.path()); // Preserved
    }
}
