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

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;

/**
 * Unit tests for EmotivaPing message type.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaPingDTOTest extends AbstractDTOTestBase {

    public EmotivaPingDTOTest() throws JAXBException {
    }

    @Test
    void marshallPlain() {
        EmotivaPingDTO dto = new EmotivaPingDTO();
        String xmlAsString = xmlUtils.marshallEmotivaDTO(dto);
        assertThat(xmlAsString).contains("<emotivaPing/>");
        assertThat(xmlAsString).doesNotContain("<property");
        assertThat(xmlAsString).doesNotContain("</emotivaPing>");
    }

    @Test
    void marshallWithProtocol() {
        EmotivaPingDTO dto = new EmotivaPingDTO("3.0");
        String xmlAsString = xmlUtils.marshallEmotivaDTO(dto);
        assertThat(xmlAsString).contains("<emotivaPing protocol=\"3.0\"/>");
        assertThat(xmlAsString).doesNotContain("<property");
        assertThat(xmlAsString).doesNotContain("</emotivaPing>");
    }

    @Test
    void unmarshallV2() throws JAXBException {
        EmotivaPingDTO dto = (EmotivaPingDTO) xmlUtils.unmarshallToEmotivaDTO(emotivaPingV2);
        assertThat(dto).isNotNull();
        assertThat(dto.getProtocol()).isNull();
    }

    @Test
    void unmarshallV3() throws JAXBException {
        EmotivaPingDTO dto = (EmotivaPingDTO) xmlUtils.unmarshallToEmotivaDTO(emotivaPingV3);
        assertThat(dto).isNotNull();
        assertThat(dto.getProtocol()).isNotNull();
        assertThat(dto.getProtocol()).isEqualTo("3.0");
    }
}
