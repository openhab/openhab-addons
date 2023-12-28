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
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.power_on;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;
import org.openhab.binding.emotiva.internal.protocol.EmotivaPropertyStatus;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;

/**
 * Unit tests for EmotivaSubscription responses.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaSubscriptionResponseTest extends AbstractDTOTestBase {

    public EmotivaSubscriptionResponseTest() throws JAXBException {
    }

    @Test
    void marshallNoProperty() {
        var dto = new EmotivaSubscriptionResponse(Collections.emptyList());
        String xmlString = xmlUtils.marshallEmotivaDTO(dto);
        assertThat(xmlString).contains("<emotivaSubscription/>");
        assertThat(xmlString).doesNotContain("</emotivaSubscription>");
        assertThat(xmlString).doesNotContain("<property");
        assertThat(xmlString).doesNotContain("<property>");
        assertThat(xmlString).doesNotContain("</property>");
    }

    @Test
    void marshallWithOneProperty() {
        EmotivaPropertyDTO emotivaPropertyDTO = new EmotivaPropertyDTO(power_on.name(), "On", "true");
        var dto = new EmotivaSubscriptionResponse(Collections.singletonList(emotivaPropertyDTO));
        String xmlString = xmlUtils.marshallEmotivaDTO(dto);
        assertThat(xmlString).contains("<emotivaSubscription>");
        assertThat(xmlString).contains("<property name=\"power_on\" value=\"On\" visible=\"true\"/>");
        assertThat(xmlString).doesNotContain("<property>");
        assertThat(xmlString).doesNotContain("</property>");
        assertThat(xmlString).contains("</emotivaSubscription>");
    }

    @Test
    void unmarshall() throws JAXBException {
        var dto = (EmotivaSubscriptionResponse) xmlUtils.unmarshallToEmotivaDTO(emotivaSubscriptionResponse);
        assertThat(dto.tags).isNotNull();
        assertThat(dto.tags.size()).isEqualTo(5);
        List<EmotivaNotifyDTO> commands = xmlUtils.unmarshallToNotification(dto.getTags());
        assertThat(commands).isNotNull();
        assertThat(commands.size()).isEqualTo(dto.tags.size());
        assertThat(commands.get(0)).isInstanceOf(EmotivaNotifyDTO.class);
        assertThat(commands.get(0).getName()).isEqualTo(EmotivaSubscriptionTags.power.name());
        assertThat(commands.get(0).getStatus()).isEqualTo(EmotivaPropertyStatus.VALID.getValue());
        assertThat(commands.get(0).getVisible()).isNull();
        assertThat(commands.get(0).getValue()).isNull();

        assertThat(commands.get(1).getName()).isEqualTo(EmotivaSubscriptionTags.source.name());
        assertThat(commands.get(1).getValue()).isEqualTo("SHIELD    ");
        assertThat(commands.get(1).getVisible()).isEqualTo("true");
        assertThat(commands.get(1).getStatus()).isEqualTo(EmotivaPropertyStatus.VALID.getValue());

        assertThat(commands.get(2).getName()).isEqualTo(EmotivaSubscriptionTags.menu.name());
        assertThat(commands.get(2).getValue()).isEqualTo("Off");
        assertThat(commands.get(2).getVisible()).isEqualTo("true");
        assertThat(commands.get(2).getStatus()).isEqualTo(EmotivaPropertyStatus.VALID.getValue());

        assertThat(commands.get(3).getName()).isEqualTo(EmotivaSubscriptionTags.treble.name());
        assertThat(commands.get(3).getValue()).isEqualTo("+ 1.5");
        assertThat(commands.get(3).getVisible()).isEqualTo("true");
        assertThat(commands.get(3).getStatus()).isEqualTo(EmotivaPropertyStatus.VALID.getValue());
        assertThat(commands.get(3).getAck()).isEqualTo("yes");

        assertThat(commands.get(4).getName()).isEqualTo(EmotivaSubscriptionTags.UNKNOWN_TAG);
        assertThat(commands.get(4).getValue()).isNull();
        assertThat(commands.get(4).getVisible()).isNull();
        assertThat(commands.get(4).getStatus()).isNull();
        assertThat(commands.get(4).getAck()).isEqualTo("no");
    }
}
