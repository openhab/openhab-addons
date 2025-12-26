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
        baseConfig.serverName = "";
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
        assertEquals("new.example.com", update.configuration().hostname);
        assertEquals(8920, update.configuration().port);
        assertTrue(update.configuration().ssl);
        assertEquals("/newpath", update.configuration().path);
    }

    @Test
    void testExtractOnlyHostnameChanged() throws Exception {
        URI uri = new URI("http://new.example.com:8096/jellyfin");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.configuration().hostname);
        assertEquals(8096, update.configuration().port);
        assertFalse(update.configuration().ssl);
        assertEquals("/jellyfin", update.configuration().path);
    }

    @Test
    void testExtractOnlyPortChanged() throws Exception {
        URI uri = new URI("http://original.example.com:9000/jellyfin");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("original.example.com", update.configuration().hostname);
        assertEquals(9000, update.configuration().port);
        assertFalse(update.configuration().ssl);
        assertEquals("/jellyfin", update.configuration().path);
    }

    @Test
    void testExtractOnlySslChanged() throws Exception {
        URI uri = new URI("https://original.example.com:8096/jellyfin");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("original.example.com", update.configuration().hostname);
        assertEquals(8096, update.configuration().port);
        assertTrue(update.configuration().ssl);
        assertEquals("/jellyfin", update.configuration().path);
    }

    @Test
    void testExtractOnlyPathChanged() throws Exception {
        URI uri = new URI("http://original.example.com:8096/newpath");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("original.example.com", update.configuration().hostname);
        assertEquals(8096, update.configuration().port);
        assertFalse(update.configuration().ssl);
        assertEquals("/newpath", update.configuration().path);
    }

    @Test
    void testExtractNoChanges() throws Exception {
        URI uri = new URI("http://original.example.com:8096/jellyfin");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertFalse(update.hasChanges());
        assertEquals("original.example.com", update.configuration().hostname);
        assertEquals(8096, update.configuration().port);
        assertFalse(update.configuration().ssl);
        assertEquals("/jellyfin", update.configuration().path);
    }

    @Test
    void testExtractWithMissingPort() throws Exception {
        URI uri = new URI("http://new.example.com/jellyfin");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.configuration().hostname);
        assertEquals(8096, update.configuration().port); // Should preserve original port
        assertFalse(update.configuration().ssl);
        assertEquals("/jellyfin", update.configuration().path);
    }

    @Test
    void testExtractWithEmptyPath() throws Exception {
        URI uri = new URI("http://new.example.com:8096");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("new.example.com", update.configuration().hostname);
        assertEquals(8096, update.configuration().port);
        assertFalse(update.configuration().ssl);
        assertEquals("/jellyfin", update.configuration().path); // Should preserve original path
    }

    @Test
    void testExtractHttpsScheme() throws Exception {
        URI uri = new URI("https://example.com:443");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertTrue(update.configuration().ssl);
    }

    @Test
    void testExtractHttpScheme() throws Exception {
        baseConfig.ssl = true; // Start with SSL enabled
        URI uri = new URI("http://example.com:80");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertFalse(update.configuration().ssl);
    }

    @Test
    void testExtractPreservesServerName() throws Exception {
        baseConfig.serverName = "My Custom Server Name";
        URI uri = new URI("https://new.example.com:8920/newpath");

        ConfigurationUpdate update = extractor.extract(uri, baseConfig);

        assertTrue(update.hasChanges());
        assertEquals("My Custom Server Name", update.configuration().serverName); // Should be preserved
        assertEquals("new.example.com", update.configuration().hostname);
        assertEquals(8920, update.configuration().port);
        assertTrue(update.configuration().ssl);
        assertEquals("/newpath", update.configuration().path);
    }
}
