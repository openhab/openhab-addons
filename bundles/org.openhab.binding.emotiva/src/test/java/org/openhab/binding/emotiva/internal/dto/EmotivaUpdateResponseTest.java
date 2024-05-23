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
import static org.openhab.binding.emotiva.internal.protocol.EmotivaPropertyStatus.NOT_VALID;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaPropertyStatus.VALID;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;

/**
 * Unit tests for EmotivaUpdate responses.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaUpdateResponseTest extends AbstractDTOTestBase {

    public EmotivaUpdateResponseTest() throws JAXBException {
    }

    @Test
    void marshallWithNoProperty() {
        EmotivaUpdateResponse dto = new EmotivaUpdateResponse(Collections.emptyList());
        String xmlAsString = xmlUtils.marshallEmotivaDTO(dto);
        assertThat(xmlAsString).contains("<emotivaUpdate/>");
        assertThat(xmlAsString).doesNotContain("<property");
        assertThat(xmlAsString).doesNotContain("</emotivaUpdate>");
    }

    @Test
    void unmarshallV2() throws JAXBException {
        var dto = (EmotivaUpdateResponse) xmlUtils.unmarshallToEmotivaDTO(emotivaUpdate_Response_V2);
        assertThat(dto).isNotNull();
        assertThat(dto.getProperties()).isNull();
        List<EmotivaNotifyDTO> notifications = xmlUtils.unmarshallToNotification(dto.getTags());
        assertThat(notifications.size()).isEqualTo(3);

        assertThat(notifications.get(0).getName()).isEqualTo(EmotivaSubscriptionTags.power.name());
        assertThat(notifications.get(0).getValue()).isEqualTo("On");
        assertThat(notifications.get(0).getVisible()).isEqualTo("true");
        assertThat(notifications.get(0).getStatus()).isEqualTo(VALID.getValue());

        assertThat(notifications.get(1).getName()).isEqualTo(EmotivaSubscriptionTags.source.name());
        assertThat(notifications.get(1).getValue()).isEqualTo("HDMI 1");
        assertThat(notifications.get(1).getVisible()).isEqualTo("true");
        assertThat(notifications.get(1).getStatus()).isEqualTo(NOT_VALID.getValue());

        assertThat(notifications.get(2).getName()).isEqualTo(EmotivaSubscriptionTags.unknown.name());
        assertThat(notifications.get(2).getStatus()).isNull();
        assertThat(notifications.get(2).getValue()).isNull();
        assertThat(notifications.get(2).getVisible()).isNull();
    }

    @Test
    void unmarshallV3() throws JAXBException {
        var dto = (EmotivaUpdateResponse) xmlUtils.unmarshallToEmotivaDTO(emotivaUpdate_Response_V3);
        assertThat(dto).isNotNull();
        assertThat(dto.getTags()).isNull();
        assertThat(dto.getProperties().size()).isEqualTo(3);

        assertThat(dto.getProperties().get(0)).isInstanceOf(EmotivaPropertyDTO.class);
        assertThat(dto.getProperties().get(0).getName()).isEqualTo(EmotivaSubscriptionTags.power.name());
        assertThat(dto.getProperties().get(0).getValue()).isEqualTo("On");
        assertThat(dto.getProperties().get(0).getVisible()).isEqualTo("true");
        assertThat(dto.getProperties().get(0).getStatus()).isEqualTo(VALID.getValue());

        assertThat(dto.getProperties().get(1).getName()).isEqualTo(EmotivaSubscriptionTags.source.name());
        assertThat(dto.getProperties().get(1).getValue()).isEqualTo("HDMI 1");
        assertThat(dto.getProperties().get(1).getVisible()).isEqualTo("true");
        assertThat(dto.getProperties().get(1).getStatus()).isEqualTo(NOT_VALID.getValue());

        assertThat(dto.getProperties().get(2).getName()).isEqualTo("noKnownTag");
        assertThat(dto.getProperties().get(2).getStatus()).isNotNull();
        assertThat(dto.getProperties().get(2).getValue()).isNotNull();
        assertThat(dto.getProperties().get(2).getVisible()).isNotNull();
    }
}
