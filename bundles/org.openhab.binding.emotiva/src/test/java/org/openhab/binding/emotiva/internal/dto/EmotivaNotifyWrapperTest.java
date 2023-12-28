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
        EmotivaNotifyWrapper dto = new EmotivaNotifyWrapper(emotivaNotifyV2_KeepAlive_Sequence,
                Collections.emptyList());
        String xmlAsString = xmlUtils.marshallEmotivaDTO(dto);
        assertThat(xmlAsString).contains("<emotivaNotify sequence=\"" + emotivaNotifyV2_KeepAlive_Sequence + "\"/>");
        assertThat(xmlAsString).doesNotContain("<property");
        assertThat(xmlAsString).doesNotContain("</emotivaNotify>");
    }

    @Test
    void marshallWithOneProperty() {
        List<EmotivaPropertyDTO> keepAliveProperty = List.of(new EmotivaPropertyDTO("keepAlive", "7500", "true"));
        EmotivaNotifyWrapper dto = new EmotivaNotifyWrapper(emotivaNotifyV2_KeepAlive_Sequence, keepAliveProperty);

        String xmlAsString = xmlUtils.marshallEmotivaDTO(dto);
        assertThat(xmlAsString).contains("<emotivaNotify sequence=\"" + emotivaNotifyV2_KeepAlive_Sequence + "\">");
        assertThat(xmlAsString).contains("<property name=\"keepAlive\" value=\"7500\" visible=\"true\"/>");
        assertThat(xmlAsString).contains("</emotivaNotify>");
    }

    @Test
    void testUnmarshallV2() throws JAXBException {
        EmotivaNotifyWrapper dto = (EmotivaNotifyWrapper) xmlUtils.unmarshallToEmotivaDTO(emotivaNotifyV2_KeepAlive);
        assertThat(dto.getSequence()).isEqualTo(emotivaNotifyV2_KeepAlive_Sequence);
        assertThat(dto.getTags().size()).isEqualTo(1);
        assertThat(dto.getTags().get(0)).isInstanceOf(Element.class);
        Element keepAlive = (Element) dto.getTags().get(0);
        assertThat(keepAlive.getTagName()).isEqualTo(EmotivaSubscriptionTags.keepAlive.name());
        assertThat(keepAlive.hasAttribute("value")).isTrue();
        assertThat(keepAlive.getAttribute("value")).isEqualTo("7500");
        assertThat(keepAlive.hasAttribute("visible")).isTrue();
        assertThat(keepAlive.getAttribute("visible")).isEqualTo("true");
        assertThat(dto.getProperties()).isNull();
    }

    @Test
    void testUnmarshallV2UnknownProperty() throws JAXBException {
        EmotivaNotifyWrapper dto = (EmotivaNotifyWrapper) xmlUtils.unmarshallToEmotivaDTO(emotivaNotifyV2_unknownTag);
        assertThat(dto.getSequence()).isEqualTo(emotivaNotifyV2_KeepAlive_Sequence);
        assertThat(dto.getTags().size()).isEqualTo(1);
        assertThat(dto.getTags().get(0)).isInstanceOf(Element.class);
        Element unknownCommand = (Element) dto.getTags().get(0);
        assertThat(unknownCommand.getTagName()).isEqualTo("unknownTag");
        assertThat(dto.getProperties()).isNull();
    }

    @Test
    void testUnmarshallV3() throws JAXBException {
        EmotivaNotifyWrapper dto = (EmotivaNotifyWrapper) xmlUtils.unmarshallToEmotivaDTO(emotivaNotifyV3_KeepAlive);
        assertThat(dto.getSequence()).isEqualTo(emotivaNotifyV2_KeepAlive_Sequence);
        assertThat(dto.getProperties().size()).isEqualTo(1);
        assertThat(dto.getTags()).isNull();
    }
}
