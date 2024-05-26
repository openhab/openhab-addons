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
                .unmarshallToEmotivaDTO(emotivaNotifyEmotivaPropertyPower);
        assertThat(commandDTO, is(notNullValue()));
        assertThat(commandDTO.getName(), is(EmotivaSubscriptionTags.tuner_channel.name()));
        assertThat(commandDTO.getValue(), is("FM 106.50MHz"));
        assertThat(commandDTO.getVisible(), is("true"));
        assertThat(commandDTO.getStatus(), is(notNullValue()));
    }

    @Test
    void unmarshallFromEmotivaUpdate() throws JAXBException {
        EmotivaPropertyDTO commandDTO = (EmotivaPropertyDTO) xmlUtils
                .unmarshallToEmotivaDTO(emotivaUpdateEmotivaPropertyPower);
        assertThat(commandDTO, is(notNullValue()));
        assertThat(commandDTO.getName(), is(EmotivaControlCommands.power.name()));
        assertThat(commandDTO.getValue(), is("On"));
        assertThat(commandDTO.getVisible(), is("true"));
        assertThat(commandDTO.getStatus(), is(VALID.getValue()));
    }
}
