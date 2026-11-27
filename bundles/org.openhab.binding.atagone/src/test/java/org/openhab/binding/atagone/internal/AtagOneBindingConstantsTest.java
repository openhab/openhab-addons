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
 * Verifies that every {@code CHANNEL_*} constant is group-qualified and matches {@code thing-types.xml}.
 * A mismatched constant causes silent channel-update and command-handling failures with no build error.
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
            assertTrue(declaredInXml.contains(value), field.getName() + " = \"" + value
                    + "\" does not match any <channel> in a " + "<channel-group-type> in thing-types.xml");
        }
    }
}
