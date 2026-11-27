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
package org.openhab.binding.tibber.internal.pulse;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Tests the scenario "Add-on metadata" of the {@code documentation} spec: the add-on file declares the connection
 * {@code hybrid}, names the Tibber API and the local Pulse bridge, keeps the name, and agrees with the default
 * translation of the description.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class AddonMetadataTest {

    private static Document addon() throws Exception {
        try (InputStream in = AddonMetadataTest.class.getResourceAsStream("/OH-INF/addon/addon.xml")) {
            assertNotNull(in);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(in);
        }
    }

    private static String text(Document document, String tag) {
        assertEquals(1, document.getElementsByTagName(tag).getLength(), tag);
        return ((Element) document.getElementsByTagName(tag).item(0)).getTextContent().trim().replaceAll("\\s+", " ");
    }

    @Test
    void whenTheAddonFileIsReadThenItIsHybridNamesTheApiAndThePulseBridgeAndKeepsItsIdAndName() throws Exception {
        Document addon = addon();

        assertEquals("tibber", addon.getDocumentElement().getAttribute("id"));
        assertEquals("hybrid", text(addon, "connection"));
        assertEquals("Tibber Binding", text(addon, "name"));
        String description = text(addon, "description");
        assertTrue(description.contains("Tibber API"), description);
        assertTrue(description.contains("Tibber Pulse"), description);
        assertTrue(description.contains("local network"), description);
        for (String forbidden : new String[] { "Sample", "sample", "TibberPulse" + "Local Binding Thing",
                "Tibber Pulse Local Binding" }) {
            assertFalse(description.contains(forbidden), forbidden);
        }
    }

    @Test
    void whenTheDescriptionOfTheAddonFileAndTheDefaultTranslationAreComparedThenTheyAgree() throws Exception {
        Properties properties = new Properties();
        try (InputStream in = AddonMetadataTest.class.getResourceAsStream("/OH-INF/i18n/tibber.properties")) {
            assertNotNull(in);
            properties.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            fail(e);
        }

        assertEquals(text(addon(), "description"), properties.getProperty("addon.tibber.description"));
        assertEquals("Tibber Binding", properties.getProperty("addon.tibber.name"));
    }
}
