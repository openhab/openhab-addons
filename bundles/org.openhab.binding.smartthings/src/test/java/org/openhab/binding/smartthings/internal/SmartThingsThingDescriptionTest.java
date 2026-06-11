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
package org.openhab.binding.smartthings.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Tests for SmartThings thing description metadata.
 */
@NonNullByDefault
class SmartThingsThingDescriptionTest {

    @Test
    void dynamicThingDiscoveryIsAdvancedBridgeConfiguration() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/account.xml");

        Element parameter = findParameter(document, "useDynamicThings");

        assertNotNull(parameter);
        Node advanced = parameter.getElementsByTagName("advanced").item(0);
        assertNotNull(advanced);
        assertEquals("true", advanced.getTextContent());
    }

    @Test
    void callbackUrlIsAdvancedBridgeConfiguration() throws Exception {
        Document document = parseThingDescription("OH-INF/thing/account.xml");

        Element parameter = findParameter(document, "callbackUrl");

        assertNotNull(parameter);
        Node advanced = parameter.getElementsByTagName("advanced").item(0);
        assertNotNull(advanced);
        assertEquals("true", advanced.getTextContent());
    }

    @Test
    void configParameterLabelsUseTitleStyleCapitalization() throws Exception {
        Document accountDocument = parseThingDescription("OH-INF/thing/account.xml");
        Document sceneDocument = parseThingDescription("OH-INF/thing/scene.xml");

        assertEquals("App Name", findParameterLabel(accountDocument, "appName"));
        assertEquals("Create Things Using Dynamic Capability Discovery",
                findParameterLabel(accountDocument, "useDynamicThings"));
        assertEquals("Location ID (Optional)", findParameterLabel(sceneDocument, "locationId"));
    }

    private Document parseThingDescription(String resourceName) throws Exception {
        InputStream stream = SmartThingsThingDescriptionTest.class.getResourceAsStream("/" + resourceName);
        assertNotNull(stream);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(stream);
    }

    private Element findParameter(Document document, String parameterName) {
        NodeList parameters = document.getElementsByTagName("parameter");
        for (int i = 0; i < parameters.getLength(); i++) {
            Element parameter = (Element) parameters.item(i);
            if (parameterName.equals(parameter.getAttribute("name"))) {
                return parameter;
            }
        }
        throw new AssertionError("Missing parameter: " + parameterName);
    }

    private String findParameterLabel(Document document, String parameterName) {
        Element parameter = findParameter(document, parameterName);
        Node label = parameter.getElementsByTagName("label").item(0);
        assertNotNull(label);
        return label.getTextContent();
    }
}
