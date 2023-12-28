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
 * Unit tests for EmotivaMenuNotify message type.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaMenuNotifyDTOTest extends AbstractDTOTestBase {

    public EmotivaMenuNotifyDTOTest() throws JAXBException {
    }

    @Test
    void testUnmarshallMenu() throws JAXBException {
        EmotivaMenuNotifyDTO dto = (EmotivaMenuNotifyDTO) xmlUtils.unmarshallToEmotivaDTO(emotivaMenuNotify);
        assertThat(dto.getProgress()).isEqualTo(null);
        assertThat(dto.getSequence()).isEqualTo("2378");
        assertThat(dto.getRow().size()).isEqualTo(11);
        assertThat(dto.getRow().size()).isEqualTo(11);
        assertThat(dto.getRow().get(0).getNumber()).isEqualTo("0");
        assertThat(dto.getRow().get(0).getCol().size()).isEqualTo(3);
        assertThat(dto.getRow().get(0).getCol().get(0).getNumber()).isEqualTo("0");
        assertThat(dto.getRow().get(0).getCol().get(0).getValue()).isEqualTo("");
        assertThat(dto.getRow().get(0).getCol().get(0).getHighlight()).isEqualTo("no");
        assertThat(dto.getRow().get(0).getCol().get(0).getArrow()).isEqualTo("no");
        assertThat(dto.getRow().get(0).getCol().get(1).getNumber()).isEqualTo("1");
        assertThat(dto.getRow().get(0).getCol().get(1).getValue()).isEqualTo("Left Display");
        assertThat(dto.getRow().get(0).getCol().get(1).getHighlight()).isEqualTo("no");
        assertThat(dto.getRow().get(0).getCol().get(1).getArrow()).isEqualTo("up");
        assertThat(dto.getRow().get(0).getCol().get(2).getNumber()).isEqualTo("2");
        assertThat(dto.getRow().get(0).getCol().get(2).getValue()).isEqualTo("Full Status");
        assertThat(dto.getRow().get(0).getCol().get(2).getHighlight()).isEqualTo("no");
        assertThat(dto.getRow().get(0).getCol().get(2).getArrow()).isEqualTo("no");
    }

    @Test
    void testUnmarshallProgress() throws JAXBException {
        EmotivaMenuNotifyDTO dto = (EmotivaMenuNotifyDTO) xmlUtils.unmarshallToEmotivaDTO(emotivaMenuNotify_Progress);
        assertThat(dto.getSequence()).isEqualTo("2405");
        assertThat(dto.getRow()).isEqualTo(null);
        assertThat(dto.getProgress().getTime()).isEqualTo("15");
    }
}
