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
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_RDS;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.PROTOCOL_V2;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;

/**
 * Unit tests for EmotivaSubscription requests.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaSubscriptionRequestTest extends AbstractDTOTestBase {

    public EmotivaSubscriptionRequestTest() throws JAXBException {
    }

    @Test
    void marshalFromChannelUID() {
        EmotivaSubscriptionTags subscriptionChannel = EmotivaSubscriptionTags.fromChannelUID(CHANNEL_TUNER_RDS);
        var emotivaSubscriptionRequest = new EmotivaSubscriptionRequest(subscriptionChannel);
        String xmlString = xmlUtils.marshallJAXBElementObjects(emotivaSubscriptionRequest);

        assertThat(xmlString, containsString("<emotivaSubscription protocol=\"2.0\">"));
        assertThat(xmlString, containsString("<tuner_RDS ack=\"yes\" />"));
        assertThat(xmlString, containsString("</emotivaSubscription>"));
    }

    @Test
    void marshallWithSubscriptionNoAck() {
        var command = new EmotivaCommandDTO(EmotivaControlCommands.volume, "10", "yes");
        var dto = new EmotivaSubscriptionRequest(command, PROTOCOL_V2.value());
        String xmlString = xmlUtils.marshallJAXBElementObjects(dto);

        assertThat(xmlString, containsString("<emotivaSubscription protocol=\"2.0\">"));
        assertThat(xmlString, containsString("<volume value=\"10\" ack=\"yes\" />"));
        assertThat(xmlString, containsString("</emotivaSubscription>"));
        assertThat(xmlString, not(containsString("<volume>")));
    }

    @Test
    void unmarshall() throws JAXBException {
        var dto = (EmotivaSubscriptionResponse) xmlUtils.unmarshallToEmotivaDTO(emotivaSubscriptionRequest);

        assertThat(dto, is(notNullValue()));
        assertThat(dto.getTags().size(), is(3));
        assertThat(dto.getProperties(), is(nullValue()));

        List<EmotivaNotifyDTO> commands = xmlUtils.unmarshallToNotification(dto.getTags());

        assertThat(commands.get(0).getName(), is(EmotivaSubscriptionTags.selected_mode.name()));
        assertThat(commands.get(0).getStatus(), is(nullValue()));
        assertThat(commands.get(0).getValue(), is(nullValue()));
        assertThat(commands.get(0).getVisible(), is(nullValue()));

        assertThat(commands.get(1).getName(), is(EmotivaSubscriptionTags.power.name()));
        assertThat(commands.get(1).getStatus(), is(nullValue()));
        assertThat(commands.get(1).getValue(), is(nullValue()));
        assertThat(commands.get(1).getVisible(), is(nullValue()));

        assertThat(commands.get(2).getName(), is("unknown"));
        assertThat(commands.get(2).getStatus(), is(nullValue()));
        assertThat(commands.get(2).getValue(), is(nullValue()));
        assertThat(commands.get(2).getVisible(), is(nullValue()));
    }
}
