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
package org.openhab.binding.luxtronikheatpump.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.w3c.dom.NodeList;

/**
 * Tests for the Luxtronik Heatpump binding update instructions.
 *
 * @author 樊孔缘 - Initial contribution
 */
class UpdateInstructionsTest {

    @Test
    void channelTypesHaveValidUids() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        try (InputStream input = Objects
                .requireNonNull(getClass().getResourceAsStream("/OH-INF/update/instructions.xml"))) {
            NodeList typeElements = factory.newDocumentBuilder().parse(input).getElementsByTagName("type");
            assertTrue(typeElements.getLength() > 0, "Expected update instructions to contain channel type UIDs");

            for (int i = 0; i < typeElements.getLength(); i++) {
                String uid = typeElements.item(i).getTextContent().trim();
                assertDoesNotThrow(() -> new ChannelTypeUID(uid), "Invalid channel type UID: " + uid);
            }
        }
    }
}
