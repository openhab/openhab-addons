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
        assertThat(xmlString, containsString("<emotivaSubscription/>"));
        assertThat(xmlString, not(containsString("</emotivaSubscription>")));
        assertThat(xmlString, not(containsString("<property")));
        assertThat(xmlString, not(containsString("<property>")));
        assertThat(xmlString, not(containsString("</property>")));
    }

    @Test
    void marshallWithOneProperty() {
        EmotivaPropertyDTO emotivaPropertyDTO = new EmotivaPropertyDTO(power_on.name(), "On", "true");
        var dto = new EmotivaSubscriptionResponse(Collections.singletonList(emotivaPropertyDTO));
        String xmlString = xmlUtils.marshallEmotivaDTO(dto);
        assertThat(xmlString, containsString("<emotivaSubscription>"));
        assertThat(xmlString, containsString("<property name=\"power_on\" value=\"On\" visible=\"true\"/>"));
        assertThat(xmlString, not(containsString("<property>")));
        assertThat(xmlString, not(containsString("</property>")));
        assertThat(xmlString, containsString("</emotivaSubscription>"));
    }

    @Test
    void unmarshall() throws JAXBException {
        var dto = (EmotivaSubscriptionResponse) xmlUtils.unmarshallToEmotivaDTO(emotivaSubscriptionResponse);
        assertThat(dto.tags, is(notNullValue()));
        assertThat(dto.tags.size(), is(5));
        List<EmotivaNotifyDTO> commands = xmlUtils.unmarshallToNotification(dto.getTags());
        assertThat(commands, is(notNullValue()));
        assertThat(commands.size(), is(dto.tags.size()));
        assertThat(commands.get(0), instanceOf(EmotivaNotifyDTO.class));
        assertThat(commands.get(0).getName(), is(EmotivaSubscriptionTags.power.name()));
        assertThat(commands.get(0).getStatus(), is(EmotivaPropertyStatus.VALID.getValue()));
        assertThat(commands.get(0).getVisible(), is(nullValue()));
        assertThat(commands.get(0).getValue(), is(nullValue()));

        assertThat(commands.get(1).getName(), is(EmotivaSubscriptionTags.source.name()));
        assertThat(commands.get(1).getValue(), is("SHIELD    "));
        assertThat(commands.get(1).getVisible(), is("true"));
        assertThat(commands.get(1).getStatus(), is(EmotivaPropertyStatus.VALID.getValue()));

        assertThat(commands.get(2).getName(), is(EmotivaSubscriptionTags.menu.name()));
        assertThat(commands.get(2).getValue(), is("Off"));
        assertThat(commands.get(2).getVisible(), is("true"));
        assertThat(commands.get(2).getStatus(), is(EmotivaPropertyStatus.VALID.getValue()));

        assertThat(commands.get(3).getName(), is(EmotivaSubscriptionTags.treble.name()));
        assertThat(commands.get(3).getValue(), is("+ 1.5"));
        assertThat(commands.get(3).getVisible(), is("true"));
        assertThat(commands.get(3).getStatus(), is(EmotivaPropertyStatus.VALID.getValue()));
        assertThat(commands.get(3).getAck(), is("yes"));

        assertThat(commands.get(4).getName(), is(EmotivaSubscriptionTags.UNKNOWN_TAG));
        assertThat(commands.get(4).getValue(), is(nullValue()));
        assertThat(commands.get(4).getVisible(), is(nullValue()));
        assertThat(commands.get(4).getStatus(), is(nullValue()));
        assertThat(commands.get(4).getAck(), is("no"));
    }
}
