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

/**
 * Unit tests for {@link ConfigurationUpdate}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class ConfigurationUpdateTest {

    private Configuration targetConfig;

    @BeforeEach
    void setUp() {
        targetConfig = new Configuration();
        targetConfig.hostname = "original.example.com";
        targetConfig.port = 8096;
        targetConfig.ssl = false;
        targetConfig.path = "/jellyfin";
    }

    @Test
    void testApplyToWithChanges() {
        ConfigurationUpdate update = new ConfigurationUpdate("new.example.com", 9000, true, "/newpath", true);

        update.applyTo(targetConfig);

        assertEquals("new.example.com", targetConfig.hostname);
        assertEquals(9000, targetConfig.port);
        assertTrue(targetConfig.ssl);
        assertEquals("/newpath", targetConfig.path);
    }

    @Test
    void testApplyToWithoutChanges() {
        ConfigurationUpdate update = new ConfigurationUpdate("original.example.com", 8096, false, "/jellyfin", false);

        update.applyTo(targetConfig);

        // Should not modify anything when hasChanges is false
        assertEquals("original.example.com", targetConfig.hostname);
        assertEquals(8096, targetConfig.port);
        assertFalse(targetConfig.ssl);
        assertEquals("/jellyfin", targetConfig.path);
    }

    @Test
    void testHasChangesTrue() {
        ConfigurationUpdate update = new ConfigurationUpdate("new.example.com", 8096, false, "/jellyfin", true);

        assertTrue(update.hasChanges());
    }

    @Test
    void testHasChangesFalse() {
        ConfigurationUpdate update = new ConfigurationUpdate("original.example.com", 8096, false, "/jellyfin", false);

        assertFalse(update.hasChanges());
    }

    @Test
    void testRecordAccessors() {
        ConfigurationUpdate update = new ConfigurationUpdate("test.example.com", 9000, true, "/test", true);

        assertEquals("test.example.com", update.hostname());
        assertEquals(9000, update.port());
        assertTrue(update.ssl());
        assertEquals("/test", update.path());
        assertTrue(update.hasChanges());
    }

    @Test
    void testApplyToPreservesOtherConfigFields() {
        targetConfig.refreshSeconds = 30;
        targetConfig.clientActiveWithInSeconds = 60;
        targetConfig.token = "test-token";

        ConfigurationUpdate update = new ConfigurationUpdate("new.example.com", 9000, true, "/newpath", true);

        update.applyTo(targetConfig);

        // Verify update fields were applied
        assertEquals("new.example.com", targetConfig.hostname);
        assertEquals(9000, targetConfig.port);
        assertTrue(targetConfig.ssl);
        assertEquals("/newpath", targetConfig.path);

        // Verify other fields were preserved
        assertEquals(30, targetConfig.refreshSeconds);
        assertEquals(60, targetConfig.clientActiveWithInSeconds);
        assertEquals("test-token", targetConfig.token);
    }
}
