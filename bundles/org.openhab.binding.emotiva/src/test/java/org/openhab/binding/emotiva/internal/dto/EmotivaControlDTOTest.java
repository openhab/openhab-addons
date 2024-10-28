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
package org.openhab.binding.emotiva.internal.dto;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;

/**
 * Unit tests for EmotivaControl message type.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaControlDTOTest extends AbstractDTOTestBase {

    public EmotivaControlDTOTest() throws JAXBException {
    }

    @Test
    void marshalWithNoCommand() {
        var control = new EmotivaControlDTO(null);
        String xmlString = xmlUtils.marshallJAXBElementObjects(control);

        assertThat(xmlString, containsString("<emotivaControl/>"));
        assertThat(xmlString, not(containsString("<property")));
        assertThat(xmlString, not(containsString("</emotivaControl>")));
    }

    @Test
    void marshalNoCommand() {
        var control = new EmotivaControlDTO(Collections.emptyList());
        String xmlString = xmlUtils.marshallJAXBElementObjects(control);

        assertThat(xmlString, containsString("<emotivaControl/>"));
    }

    @Test
    void marshalCommand() {
        EmotivaCommandDTO command = EmotivaCommandDTO.fromTypeWithAck(EmotivaControlCommands.set_volume, "10");
        var control = new EmotivaControlDTO(List.of(command));
        String xmlString = xmlUtils.marshallJAXBElementObjects(control);

        assertThat(xmlString, containsString("<emotivaControl>"));
        assertThat(xmlString, containsString("<set_volume value=\"10\" ack=\"yes\" />"));
        assertThat(xmlString, endsWith("</emotivaControl>\n"));
    }

    @Test
    void marshalWithTwoCommands() {
        var control = new EmotivaControlDTO(List.of(EmotivaCommandDTO.fromTypeWithAck(EmotivaControlCommands.power_on),
                EmotivaCommandDTO.fromTypeWithAck(EmotivaControlCommands.hdmi1)));
        String xmlString = xmlUtils.marshallJAXBElementObjects(control);

        assertThat(xmlString, containsString("<emotivaControl>"));
        assertThat(xmlString, containsString("<power_on ack=\"yes\" />"));
        assertThat(xmlString, containsString("<hdmi1 ack=\"yes\" />"));
        assertThat(xmlString, endsWith("</emotivaControl>\n"));
    }
}
