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
        assertThat(xmlAsString, containsString("<emotivaUpdate/>"));
        assertThat(xmlAsString, not(containsString("<property")));
        assertThat(xmlAsString, not(containsString("</emotivaUpdate>")));
    }

    @Test
    void unmarshallV2() throws JAXBException {
        var dto = (EmotivaUpdateResponse) xmlUtils.unmarshallToEmotivaDTO(emotivaUpdateResponseV2);
        assertThat(dto, is(notNullValue()));
        assertThat(dto.getProperties(), is(nullValue()));
        List<EmotivaNotifyDTO> notifications = xmlUtils.unmarshallToNotification(dto.getTags());
        assertThat(notifications.size(), is(3));

        assertThat(notifications.get(0).getName(), is(EmotivaSubscriptionTags.power.name()));
        assertThat(notifications.get(0).getValue(), is("On"));
        assertThat(notifications.get(0).getVisible(), is("true"));
        assertThat(notifications.get(0).getStatus(), is(VALID.getValue()));

        assertThat(notifications.get(1).getName(), is(EmotivaSubscriptionTags.source.name()));
        assertThat(notifications.get(1).getValue(), is("HDMI 1"));
        assertThat(notifications.get(1).getVisible(), is("true"));
        assertThat(notifications.get(1).getStatus(), is(NOT_VALID.getValue()));

        assertThat(notifications.get(2).getName(), is(EmotivaSubscriptionTags.unknown.name()));
        assertThat(notifications.get(2).getStatus(), is(nullValue()));
        assertThat(notifications.get(2).getValue(), is(nullValue()));
        assertThat(notifications.get(2).getVisible(), is(nullValue()));
    }

    @Test
    void unmarshallV3() throws JAXBException {
        var dto = (EmotivaUpdateResponse) xmlUtils.unmarshallToEmotivaDTO(emotivaUpdateResponseV3);
        assertThat(dto, is(notNullValue()));
        assertThat(dto.getTags(), is(nullValue()));
        assertThat(dto.getProperties().size(), is(3));

        assertThat(dto.getProperties().get(0), instanceOf(EmotivaPropertyDTO.class));
        assertThat(dto.getProperties().get(0).getName(), is(EmotivaSubscriptionTags.power.name()));
        assertThat(dto.getProperties().get(0).getValue(), is("On"));
        assertThat(dto.getProperties().get(0).getVisible(), is("true"));
        assertThat(dto.getProperties().get(0).getStatus(), is(VALID.getValue()));

        assertThat(dto.getProperties().get(1).getName(), is(EmotivaSubscriptionTags.source.name()));
        assertThat(dto.getProperties().get(1).getValue(), is("HDMI 1"));
        assertThat(dto.getProperties().get(1).getVisible(), is("true"));
        assertThat(dto.getProperties().get(1).getStatus(), is(NOT_VALID.getValue()));

        assertThat(dto.getProperties().get(2).getName(), is("noKnownTag"));
        assertThat(dto.getProperties().get(2).getStatus(), is(notNullValue()));
        assertThat(dto.getProperties().get(2).getValue(), is(notNullValue()));
        assertThat(dto.getProperties().get(2).getVisible(), is(notNullValue()));
    }
}
