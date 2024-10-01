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

import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;
import org.w3c.dom.Element;

/**
 * Unit tests for EmotivaNotify wrapper.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaNotifyWrapperTest extends AbstractDTOTestBase {

    public EmotivaNotifyWrapperTest() throws JAXBException {
    }

    @Test
    void marshallWithNoProperty() {
        EmotivaNotifyWrapper dto = new EmotivaNotifyWrapper(emotivaNotifyV2KeepAliveSequence, Collections.emptyList());
        String xmlAsString = xmlUtils.marshallEmotivaDTO(dto);
        assertThat(xmlAsString,
                containsString("<emotivaNotify sequence=\"" + emotivaNotifyV2KeepAliveSequence + "\"/>"));
        assertThat(xmlAsString, not(containsString("<property")));
        assertThat(xmlAsString, not(containsString("</emotivaNotify>")));
    }

    @Test
    void marshallWithOneProperty() {
        List<EmotivaPropertyDTO> keepAliveProperty = List.of(new EmotivaPropertyDTO("keepAlive", "7500", "true"));
        EmotivaNotifyWrapper dto = new EmotivaNotifyWrapper(emotivaNotifyV2KeepAliveSequence, keepAliveProperty);

        String xmlAsString = xmlUtils.marshallEmotivaDTO(dto);
        assertThat(xmlAsString,
                containsString("<emotivaNotify sequence=\"" + emotivaNotifyV2KeepAliveSequence + "\">"));
        assertThat(xmlAsString, containsString("<property name=\"keepAlive\" value=\"7500\" visible=\"true\"/>"));
        assertThat(xmlAsString, containsString("</emotivaNotify>"));
    }

    @Test
    void testUnmarshallV2() throws JAXBException {
        EmotivaNotifyWrapper dto = (EmotivaNotifyWrapper) xmlUtils.unmarshallToEmotivaDTO(emotivaNotifyV2KeepAlive);
        assertThat(dto.getSequence(), is(emotivaNotifyV2KeepAliveSequence));
        assertThat(dto.getTags().size(), is(1));
        assertThat(dto.getTags().get(0), instanceOf(Element.class));
        Element keepAlive = (Element) dto.getTags().get(0);
        assertThat(keepAlive.getTagName(), is(EmotivaSubscriptionTags.keepAlive.name()));
        assertThat(keepAlive.hasAttribute("value"), is(true));
        assertThat(keepAlive.getAttribute("value"), is("7500"));
        assertThat(keepAlive.hasAttribute("visible"), is(true));
        assertThat(keepAlive.getAttribute("visible"), is("true"));
        assertThat(dto.getProperties(), is(nullValue()));
    }

    @Test
    void testUnmarshallV2UnknownProperty() throws JAXBException {
        EmotivaNotifyWrapper dto = (EmotivaNotifyWrapper) xmlUtils.unmarshallToEmotivaDTO(emotivaNotifyV2UnknownTag);
        assertThat(dto.getSequence(), is(emotivaNotifyV2KeepAliveSequence));
        assertThat(dto.getTags().size(), is(1));
        assertThat(dto.getTags().get(0), instanceOf(Element.class));
        Element unknownCommand = (Element) dto.getTags().get(0);
        assertThat(unknownCommand.getTagName(), is("unknownTag"));
        assertThat(dto.getProperties(), is(nullValue()));
    }

    @Test
    void testUnmarshallV3() throws JAXBException {
        EmotivaNotifyWrapper dto = (EmotivaNotifyWrapper) xmlUtils.unmarshallToEmotivaDTO(emotivaNotifyV3KeepAlive);
        assertThat(dto.getSequence(), is(emotivaNotifyV2KeepAliveSequence));
        assertThat(dto.getProperties().size(), is(1));
        assertThat(dto.getTags(), is(nullValue()));
    }

    @Test
    void testUnmarshallV3EmptyValue() throws JAXBException {
        EmotivaNotifyWrapper dto = (EmotivaNotifyWrapper) xmlUtils
                .unmarshallToEmotivaDTO(emotivaNotifyV3EmptyMenuValue);
        assertThat(dto.getSequence(), is("23929"));
        assertThat(dto.getProperties().size(), is(1));
        assertThat(dto.getProperties().get(0).getName(), is("menu"));
        assertThat(dto.getProperties().get(0).getValue(), is(""));
        assertThat(dto.getProperties().get(0).getVisible(), is("true"));
        assertThat(dto.getProperties().get(0).getStatus(), is(notNullValue()));
        assertThat(dto.getTags(), is(nullValue()));
    }
}
