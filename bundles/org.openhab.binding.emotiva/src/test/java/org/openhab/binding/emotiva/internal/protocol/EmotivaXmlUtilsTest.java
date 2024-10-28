/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal.protocol;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;
import org.openhab.binding.emotiva.internal.dto.EmotivaNotifyWrapper;

/**
 * Unit tests for Emotiva message marshalling and unmarshalling.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaXmlUtilsTest extends AbstractDTOTestBase {

    public EmotivaXmlUtilsTest() throws JAXBException {
    }

    @Test
    void testUnmarshallEmptyString() {
        assertThrows(JAXBException.class, () -> xmlUtils.unmarshallToEmotivaDTO(""), "xml value is null or empty");
    }

    @Test
    void testUnmarshallNotValidXML() {
        assertThrows(UnmarshalException.class, () -> xmlUtils.unmarshallToEmotivaDTO("notXmlAtAll"));
    }

    @Test
    void testUnmarshallInstanceObject() throws JAXBException {
        Object object = xmlUtils.unmarshallToEmotivaDTO(emotivaNotifyV2KeepAlive);

        assertThat(object, instanceOf(EmotivaNotifyWrapper.class));
    }

    @Test
    void testUnmarshallXml() throws JAXBException {
        Object object = xmlUtils.unmarshallToEmotivaDTO(emotivaNotifyV2KeepAlive);

        assertThat(object, instanceOf(EmotivaNotifyWrapper.class));
    }

    @Test
    void testMarshallObjectWithoutXmlElements() {
        String commands = xmlUtils.marshallEmotivaDTO("");

        assertThat(commands, is(""));
    }

    @Test
    void testMarshallNoValueDTO() {
        var dto = new EmotivaNotifyWrapper();
        String xmlAsString = xmlUtils.marshallEmotivaDTO(dto);

        assertThat(xmlAsString, not(containsString("<emotivaNotify>")));
        assertThat(xmlAsString, containsString("<emotivaNotify/>"));
    }
}
