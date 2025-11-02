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

/**
 * Unit tests for {@link UriConfigurationExtractor}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class UriConfigurationExtractorTest {

    private UriConfigurationExtractor extractor;
    private Configuration baseConfig;

    @BeforeEach
    void setUp() {
        extractor = new UriConfigurationExtractor();
        baseConfig = new Configuration();
        baseConfig.hostname = "original.example.com";
        baseConfig.port = 8096;
        baseConfig.ssl = false;
        baseConfig.path = "/jellyfin";
    }

    @Test
    void testExtractAllFieldsChanged() throws Exception {
        URI uri = new URI("https://new.example.com:8920/newpath");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.hostname());
        assertEquals(8920, update.port());
        assertTrue(update.ssl());
        assertEquals("/newpath", update.path());
    }

    @Test
    void testExtractOnlyHostnameChanged() throws Exception {
        URI uri = new URI("http://new.example.com:8096/jellyfin");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.hostname());
        assertEquals(8096, update.port());
        assertFalse(update.ssl());
        assertEquals("/jellyfin", update.path());
    }

    @Test
    void testExtractOnlyPortChanged() throws Exception {
        URI uri = new URI("http://original.example.com:9000/jellyfin");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("original.example.com", update.hostname());
        assertEquals(9000, update.port());
        assertFalse(update.ssl());
        assertEquals("/jellyfin", update.path());
    }

    @Test
    void testExtractOnlySslChanged() throws Exception {
        URI uri = new URI("https://original.example.com:8096/jellyfin");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("original.example.com", update.hostname());
        assertEquals(8096, update.port());
        assertTrue(update.ssl());
        assertEquals("/jellyfin", update.path());
    }

    @Test
    void testExtractOnlyPathChanged() throws Exception {
        URI uri = new URI("http://original.example.com:8096/newpath");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("original.example.com", update.hostname());
        assertEquals(8096, update.port());
        assertFalse(update.ssl());
        assertEquals("/newpath", update.path());
    }

    @Test
    void testExtractNoChanges() throws Exception {
        URI uri = new URI("http://original.example.com:8096/jellyfin");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertFalse(update.hasChanges());
        assertEquals("original.example.com", update.hostname());
        assertEquals(8096, update.port());
        assertFalse(update.ssl());
        assertEquals("/jellyfin", update.path());
    }

    @Test
    void testExtractWithMissingPort() throws Exception {
        URI uri = new URI("http://new.example.com/jellyfin");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.hostname());
        assertEquals(8096, update.port()); // Should preserve original port
        assertFalse(update.ssl());
        assertEquals("/jellyfin", update.path());
    }

    @Test
    void testExtractWithEmptyPath() throws Exception {
        URI uri = new URI("http://new.example.com:8096");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.hostname());
        assertEquals(8096, update.port());
        assertFalse(update.ssl());
        assertEquals("/jellyfin", update.path()); // Should preserve original path
    }

    @Test
    void testExtractHttpsScheme() throws Exception {
        URI uri = new URI("https://example.com:443");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertTrue(update.ssl());
    }

    @Test
    void testExtractHttpScheme() throws Exception {
        baseConfig.ssl = true; // Start with SSL enabled
        URI uri = new URI("http://example.com:80");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertFalse(update.ssl());
    }
}
