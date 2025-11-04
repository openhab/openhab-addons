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
        baseConfig.serverName = "";
        baseConfig.hostname = "192.168.1.100";
        baseConfig.port = 8096;
        baseConfig.ssl = false;
        baseConfig.path = "/jellyfin";
    }

    @Test
    void testExtractWithServerNameAndLocalAddress() {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setServerName("My Jellyfin Server");
        systemInfo.setLocalAddress("192.168.1.200");

        ConfigurationUpdate update = extractor.extract(systemInfo, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("My Jellyfin Server", update.configuration().serverName);
        assertEquals("192.168.1.200", update.configuration().hostname);
        assertEquals(8096, update.configuration().port); // Port should be preserved
        assertFalse(update.configuration().ssl); // SSL should be preserved
        assertEquals("/jellyfin", update.configuration().path); // Path should be preserved
    }

    @Test
    void testExtractWithNoChanges() {
        baseConfig.serverName = "My Jellyfin Server";
        baseConfig.hostname = "192.168.1.100";

        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setServerName("My Jellyfin Server");
        systemInfo.setLocalAddress("192.168.1.100");

        ConfigurationUpdate update = extractor.extract(systemInfo, baseConfig);

        assertFalse(update.hasChanges());
        assertEquals("My Jellyfin Server", update.configuration().serverName);
        assertEquals("192.168.1.100", update.configuration().hostname);
        assertEquals(8096, update.configuration().port);
        assertFalse(update.configuration().ssl);
        assertEquals("/jellyfin", update.configuration().path);
    }

    @Test
    void testExtractPreservesUserServerName() {
        baseConfig.serverName = "Custom User Name";

        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setServerName("Default Server Name");
        systemInfo.setLocalAddress("192.168.1.100");

        ConfigurationUpdate update = extractor.extract(systemInfo, baseConfig);

        // serverName should be preserved, but hostname still updates
        assertFalse(update.hasChanges()); // No hostname change
        assertEquals("Custom User Name", update.configuration().serverName); // User name preserved
        assertEquals("192.168.1.100", update.configuration().hostname);
    }

    @Test
    void testExtractWithNullValues() {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setServerName(null);
        systemInfo.setLocalAddress(null);

        ConfigurationUpdate update = extractor.extract(systemInfo, baseConfig);

        assertFalse(update.hasChanges());
        assertEquals("", update.configuration().serverName); // Empty since it was empty before
        assertEquals("192.168.1.100", update.configuration().hostname); // Should preserve original
        assertEquals(8096, update.configuration().port);
        assertFalse(update.configuration().ssl);
        assertEquals("/jellyfin", update.configuration().path);
    }
}
