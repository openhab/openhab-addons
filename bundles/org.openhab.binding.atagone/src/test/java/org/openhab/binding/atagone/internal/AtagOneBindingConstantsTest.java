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
package org.openhab.binding.atagone.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Guards against a specific silent-failure mode: once
 * {@code thing-types.xml} moves a channel under a {@code <channel-group-type>},
 * {@code org.openhab.core.thing.ChannelUID#getId()} starts returning the group-qualified id
 * ({@code "control#preset-mode"}), not the bare one. Every {@code CHANNEL_*} constant here MUST match that
 * shape, or {@code AtagOneHandler}'s channel-id switch and {@code updateState()} calls silently stop
 * matching anything — command handling and channel updates both fail with no exception, no log entry, and no
 * change in build/test result, since the existing tests exercise {@code buildControlUpdate} directly with
 * these same constants rather than through a real {@code ChannelUID}.
 * <p>
 * This test parses the actual {@code thing-types.xml} shipped with the binding and cross-checks every
 * {@code CHANNEL_*} constant against it, so a channel moved to a different group in the XML without the
 * matching constant update fails the build instead of only failing on a live device.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
class AtagOneBindingConstantsTest {

    private Set<String> loadGroupQualifiedChannelIdsFromThingTypesXml()
            throws ParserConfigurationException, SAXException, IOException {
        Set<String> groupQualifiedIds = new HashSet<>();
        try (InputStream xml = getClass().getResourceAsStream("/OH-INF/thing/thing-types.xml")) {
            assertNotNull(xml, "thing-types.xml not found on the classpath");
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(xml);
            NodeList groupTypes = doc.getElementsByTagName("channel-group-type");
            for (int i = 0; i < groupTypes.getLength(); i++) {
                Element groupType = (Element) groupTypes.item(i);
                String groupId = groupType.getAttribute("id");
                NodeList channels = groupType.getElementsByTagName("channel");
                for (int j = 0; j < channels.getLength(); j++) {
                    Element channel = (Element) channels.item(j);
                    groupQualifiedIds.add(groupId + "#" + channel.getAttribute("id"));
                }
            }
        }
        return groupQualifiedIds;
    }

    @Test
    void everyChannelConstantIsGroupQualified() throws ReflectiveOperationException {
        for (Field field : AtagOneBindingConstants.class.getDeclaredFields()) {
            if (!field.getName().startsWith("CHANNEL_") || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String value = (String) field.get(null);
            assertTrue(value.contains("#"),
                    field.getName() + " = \"" + value + "\" is not group-qualified (missing \"group#\" prefix)");
        }
    }

    @Test
    void everyWiredChannelConstantMatchesThingTypesXml()
            throws ReflectiveOperationException, ParserConfigurationException, SAXException, IOException {
        Set<String> declaredInXml = loadGroupQualifiedChannelIdsFromThingTypesXml();
        assertFalse(declaredInXml.isEmpty(), "Parsed zero channels from thing-types.xml — parsing itself is broken");

        for (Field field : AtagOneBindingConstants.class.getDeclaredFields()) {
            if (!field.getName().startsWith("CHANNEL_") || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String value = (String) field.get(null);
            // The five "advanced writable device-configuration channels" constants are declared for a
            // later phase and deliberately have no <channel-type> in thing-types.xml yet — everything
            // else is a real, currently-shipped channel and must match exactly.
            if (field.getName().equals("CHANNEL_FROST_PROTECTION")
                    || field.getName().equals("CHANNEL_FROST_PROTECTION_TEMPERATURE")
                    || field.getName().equals("CHANNEL_LEGIONELLA_PROTECTION")
                    || field.getName().equals("CHANNEL_SUMMER_ECO_MODE")
                    || field.getName().equals("CHANNEL_SUMMER_ECO_TEMPERATURE")) {
                continue;
            }
            assertTrue(declaredInXml.contains(value), field.getName() + " = \"" + value
                    + "\" does not match any <channel> in a " + "<channel-group-type> in thing-types.xml");
        }
    }
}
