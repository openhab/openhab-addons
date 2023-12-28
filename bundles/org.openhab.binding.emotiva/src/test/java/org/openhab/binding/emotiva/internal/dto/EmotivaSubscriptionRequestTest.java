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
        EmotivaSubscriptionRequest emotivaSubscriptionRequest = new EmotivaSubscriptionRequest(subscriptionChannel);
        String xmlString = xmlUtils.marshallJAXBElementObjects(emotivaSubscriptionRequest);
        assertThat(xmlString).contains("<emotivaSubscription protocol=\"2.0\">");
        assertThat(xmlString).contains("<tuner_RDS ack=\"yes\" />");
        assertThat(xmlString).contains("</emotivaSubscription>");
    }

    @Test
    void marshallWithTwoSubscriptionsNoAck() {
        EmotivaCommandDTO command1 = new EmotivaCommandDTO(EmotivaControlCommands.volume, "10", "yes");
        EmotivaCommandDTO command2 = new EmotivaCommandDTO(EmotivaControlCommands.power_off);

        EmotivaSubscriptionRequest dto = new EmotivaSubscriptionRequest(List.of(command1, command2),
                PROTOCOL_V2.value());

        String xmlString = xmlUtils.marshallJAXBElementObjects(dto);
        assertThat(xmlString).contains("<emotivaSubscription protocol=\"2.0\">");
        assertThat(xmlString).contains("<volume value=\"10\" ack=\"yes\" />");
        assertThat(xmlString).contains("<power_off />");
        assertThat(xmlString).contains("</emotivaSubscription>");
        assertThat(xmlString).doesNotContain("<volume>");
        assertThat(xmlString).doesNotContain("<command>");
    }

    @Test
    void unmarshall() throws JAXBException {
        var dto = (EmotivaSubscriptionResponse) xmlUtils.unmarshallToEmotivaDTO(emotivaSubscriptionRequest);
        assertThat(dto).isNotNull();
        assertThat(dto.getTags().size()).isEqualTo(3);
        assertThat(dto.getProperties()).isNull();

        List<EmotivaNotifyDTO> commands = xmlUtils.unmarshallToNotification(dto.getTags());

        assertThat(commands.get(0).getName()).isEqualTo(EmotivaSubscriptionTags.selected_mode.name());
        assertThat(commands.get(0).getStatus()).isNull();
        assertThat(commands.get(0).getValue()).isNull();
        assertThat(commands.get(0).getVisible()).isNull();

        assertThat(commands.get(1).getName()).isEqualTo(EmotivaSubscriptionTags.power.name());
        assertThat(commands.get(1).getStatus()).isNull();
        assertThat(commands.get(1).getValue()).isNull();
        assertThat(commands.get(1).getVisible()).isNull();

        assertThat(commands.get(2).getName()).isEqualTo("unknown");
        assertThat(commands.get(2).getStatus()).isNull();
        assertThat(commands.get(2).getValue()).isNull();
        assertThat(commands.get(2).getVisible()).isNull();
    }
}
