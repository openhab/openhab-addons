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

/**
 * Unit tests for {@link ConfigurationUpdate}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class ConfigurationUpdateTest {

    private Configuration baseConfig;

    @BeforeEach
    void setUp() {
        baseConfig = new Configuration();
        baseConfig.hostname = "original.example.com";
        baseConfig.port = 8096;
        baseConfig.ssl = false;
        baseConfig.path = "/jellyfin";
        baseConfig.token = "test-token";
        baseConfig.refreshSeconds = 30;
        baseConfig.clientActiveWithInSeconds = 60;
    }

    @Test
    void testConfigurationUpdateWithChanges() {
        Configuration updated = new Configuration();
        updated.hostname = "new.example.com";
        updated.port = 9000;
        updated.ssl = true;
        updated.path = "/newpath";
        updated.token = baseConfig.token;
        updated.refreshSeconds = baseConfig.refreshSeconds;
        updated.clientActiveWithInSeconds = baseConfig.clientActiveWithInSeconds;

        ConfigurationUpdate update = new ConfigurationUpdate(updated, true);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.configuration().hostname);
        assertEquals(9000, update.configuration().port);
        assertTrue(update.configuration().ssl);
        assertEquals("/newpath", update.configuration().path);
    }

    @Test
    void testConfigurationUpdateWithoutChanges() {
        ConfigurationUpdate update = new ConfigurationUpdate(baseConfig, false);

        assertFalse(update.hasChanges());
        assertEquals("original.example.com", update.configuration().hostname);
        assertEquals(8096, update.configuration().port);
        assertFalse(update.configuration().ssl);
        assertEquals("/jellyfin", update.configuration().path);
    }

    @Test
    void testRecordAccessors() {
        Configuration updated = new Configuration();
        updated.hostname = "test.example.com";
        updated.port = 9000;
        updated.ssl = true;
        updated.path = "/test";

        ConfigurationUpdate update = new ConfigurationUpdate(updated, true);

        assertNotNull(update.configuration());
        assertTrue(update.hasChanges());
        assertEquals("test.example.com", update.configuration().hostname);
        assertEquals(9000, update.configuration().port);
        assertTrue(update.configuration().ssl);
        assertEquals("/test", update.configuration().path);
    }

    @Test
    void testPreservesAllConfigurationFields() {
        Configuration updated = new Configuration();
        updated.hostname = "new.example.com";
        updated.port = 9000;
        updated.ssl = true;
        updated.path = "/newpath";
        updated.token = baseConfig.token;
        updated.refreshSeconds = baseConfig.refreshSeconds;
        updated.clientActiveWithInSeconds = baseConfig.clientActiveWithInSeconds;

        ConfigurationUpdate update = new ConfigurationUpdate(updated, true);

        // Verify update fields
        assertEquals("new.example.com", update.configuration().hostname);
        assertEquals(9000, update.configuration().port);
        assertTrue(update.configuration().ssl);
        assertEquals("/newpath", update.configuration().path);

        // Verify other fields were preserved
        assertEquals("test-token", update.configuration().token);
        assertEquals(30, update.configuration().refreshSeconds);
        assertEquals(60, update.configuration().clientActiveWithInSeconds);
    }
}
