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
package org.openhab.binding.knx.internal.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests that the parser {@link XmlInput} opens does not process document type declarations, apart from the
 * refusal in {@link XmlInput#next}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class XmlInputTest {

    @Test
    void anEntityDeclaredInADocumentTypeDeclarationIsNotExpanded() throws Exception {
        String document = "<!DOCTYPE KNX [<!ENTITY made \"made-up text\">]><KNX>&made;</KNX>";
        XMLStreamReader xml = XmlInput.open(new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)),
                "test.xml", new ReadBudget(ReadLimits.DEFAULT));
        StringBuilder text = new StringBuilder();
        try {
            // the plain parser, not XmlInput.next, which would stop at the declaration
            while (xml.hasNext()) {
                if (xml.next() == XMLStreamConstants.CHARACTERS) {
                    text.append(xml.getText());
                }
            }
        } catch (XMLStreamException e) {
            // an undeclared entity is an error here, which is as good
        } finally {
            XmlInput.close(xml);
        }

        assertFalse(text.toString().contains("made-up"), text.toString());
    }
}
