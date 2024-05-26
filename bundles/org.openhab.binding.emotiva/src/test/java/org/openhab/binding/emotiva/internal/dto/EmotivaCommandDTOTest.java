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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;

/**
 * Unit tests for EmotivaCommandDTO command types.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaCommandDTOTest extends AbstractDTOTestBase {

    public EmotivaCommandDTOTest() throws JAXBException {
    }

    @Test
    void unmarshallElements() {
        List<EmotivaCommandDTO> commandDTO = xmlUtils.unmarshallToCommands(emotivaCommandoPowerOn);
        assertThat(commandDTO).isNotNull();
        assertThat(commandDTO.size()).isEqualTo(1);
        assertThat(commandDTO.get(0).getName()).isEqualTo(EmotivaControlCommands.power_on.name());
    }

    @Test
    void unmarshallFromEmotivaAckWithMissingEnumType() {
        List<EmotivaCommandDTO> commandDTO = xmlUtils.unmarshallToCommands(emotivaAckPowerOffAndNotRealCommand);
        assertThat(commandDTO).isNotNull();
        assertThat(commandDTO.size()).isEqualTo(2);
        assertThat(commandDTO.get(0).getName()).isEqualTo(EmotivaControlCommands.power_off.name());
        assertThat(commandDTO.get(0).getStatus()).isEqualTo("ack");
        assertThat(commandDTO.get(0).getValue()).isNull();
        assertThat(commandDTO.get(0).getVisible()).isNull();
        assertThat(commandDTO.get(1).getName()).isEqualTo(EmotivaControlCommands.none.name());
        assertThat(commandDTO.get(1).getStatus()).isEqualTo("ack");
        assertThat(commandDTO.get(1).getValue()).isNull();
        assertThat(commandDTO.get(1).getVisible()).isNull();
    }

    @Test
    void unmarshallFromEmotivaAck() {
        List<EmotivaCommandDTO> commandDTO = xmlUtils.unmarshallToCommands(emotivaAckPowerOffAndVolume);
        assertThat(commandDTO).isNotNull();
        assertThat(commandDTO.size()).isEqualTo(2);
        assertThat(commandDTO.get(0).getName()).isEqualTo(EmotivaControlCommands.power_off.name());
        assertThat(commandDTO.get(0).getStatus()).isEqualTo("ack");
        assertThat(commandDTO.get(0).getValue()).isNull();
        assertThat(commandDTO.get(0).getVisible()).isNull();
        assertThat(commandDTO.get(1).getName()).isEqualTo(EmotivaControlCommands.volume.name());
        assertThat(commandDTO.get(1).getStatus()).isEqualTo("ack");
        assertThat(commandDTO.get(1).getValue()).isNull();
        assertThat(commandDTO.get(1).getVisible()).isNull();
    }
}
