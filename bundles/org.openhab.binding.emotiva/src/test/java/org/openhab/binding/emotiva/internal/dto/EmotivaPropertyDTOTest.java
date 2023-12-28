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
import static org.openhab.binding.emotiva.internal.protocol.EmotivaPropertyStatus.VALID;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;

/**
 * Unit tests for EmotivaCommandDTO command types.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaPropertyDTOTest extends AbstractDTOTestBase {

    public EmotivaPropertyDTOTest() throws JAXBException {
    }

    @Test
    void unmarshallFromEmotivaNotify() throws JAXBException {
        EmotivaPropertyDTO commandDTO = (EmotivaPropertyDTO) xmlUtils
                .unmarshallToEmotivaDTO(emotivaNotify_emotivaProperty_Power);
        assertThat(commandDTO).isNotNull();
        assertThat(commandDTO.getName()).isEqualTo(EmotivaSubscriptionTags.tuner_channel.name());
        assertThat(commandDTO.getValue()).isEqualTo("FM 106.50MHz");
        assertThat(commandDTO.getVisible()).isEqualTo("true");
        assertThat(commandDTO.getStatus()).isNull();
    }

    @Test
    void unmarshallFromEmotivaUpdate() throws JAXBException {
        EmotivaPropertyDTO commandDTO = (EmotivaPropertyDTO) xmlUtils
                .unmarshallToEmotivaDTO(emotivaUpdate_emotivaProperty_Power);
        assertThat(commandDTO).isNotNull();
        assertThat(commandDTO.getName()).isEqualTo(EmotivaControlCommands.power.name());
        assertThat(commandDTO.getValue()).isEqualTo("On");
        assertThat(commandDTO.getVisible()).isEqualTo("true");
        assertThat(commandDTO.getStatus()).isEqualTo(VALID.getValue());
    }
}
