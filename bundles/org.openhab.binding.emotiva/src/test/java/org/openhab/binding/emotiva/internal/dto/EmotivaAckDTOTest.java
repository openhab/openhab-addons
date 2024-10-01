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

import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;

/**
 * Unit tests for EmotivaAck message type.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaAckDTOTest extends AbstractDTOTestBase {

    public EmotivaAckDTOTest() throws JAXBException {
    }

    @Test
    void unmarshallValidCommand() throws JAXBException {
        EmotivaAckDTO dto = (EmotivaAckDTO) xmlUtils.unmarshallToEmotivaDTO(emotivaAckPowerOff);
        assertThat(dto, is(notNullValue()));
        assertThat(dto.getCommands().size(), is(1));
    }

    @Test
    void unmarshallOneValidCommand() throws JAXBException {
        EmotivaAckDTO dto = (EmotivaAckDTO) xmlUtils.unmarshallToEmotivaDTO(emotivaAckPowerOffAndNotRealCommand);
        assertThat(dto, is(notNullValue()));
        List<EmotivaCommandDTO> commands = xmlUtils.unmarshallXmlObjectsToControlCommands(dto.getCommands());
        assertThat(commands.size(), is(2));

        assertThat(commands.get(0), is(notNullValue()));
        assertThat(commands.get(0).getName(), is(EmotivaControlCommands.power_off.name()));
        assertThat(commands.get(0).getStatus(), is("ack"));
        assertThat(commands.get(0).getVisible(), is(nullValue()));
        assertThat(commands.get(0).getValue(), is(nullValue()));

        assertThat(commands.get(1), is(notNullValue()));
        assertThat(commands.get(1).getName(), is(EmotivaControlCommands.none.name()));
        assertThat(commands.get(1).getStatus(), is("ack"));
        assertThat(commands.get(1).getVisible(), is(nullValue()));
        assertThat(commands.get(1).getValue(), is(nullValue()));
    }
}
