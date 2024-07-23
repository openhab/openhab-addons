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
        assertThat(dto.getProgress(), is(nullValue()));
        assertThat(dto.getSequence(), is("2378"));
        assertThat(dto.getRow().size(), is(11));
        assertThat(dto.getRow().size(), is(11));
        assertThat(dto.getRow().get(0).getNumber(), is("0"));
        assertThat(dto.getRow().get(0).getCol().size(), is(3));
        assertThat(dto.getRow().get(0).getCol().get(0).getNumber(), is("0"));
        assertThat(dto.getRow().get(0).getCol().get(0).getValue(), is(""));
        assertThat(dto.getRow().get(0).getCol().get(0).getHighlight(), is("no"));
        assertThat(dto.getRow().get(0).getCol().get(0).getArrow(), is("no"));
        assertThat(dto.getRow().get(0).getCol().get(1).getNumber(), is("1"));
        assertThat(dto.getRow().get(0).getCol().get(1).getValue(), is("Left Display"));
        assertThat(dto.getRow().get(0).getCol().get(1).getHighlight(), is("no"));
        assertThat(dto.getRow().get(0).getCol().get(1).getArrow(), is("up"));
        assertThat(dto.getRow().get(0).getCol().get(2).getNumber(), is("2"));
        assertThat(dto.getRow().get(0).getCol().get(2).getValue(), is("Full Status"));
        assertThat(dto.getRow().get(0).getCol().get(2).getHighlight(), is("no"));
        assertThat(dto.getRow().get(0).getCol().get(2).getArrow(), is("no"));
    }

    @Test
    void testUnmarshallProgress() throws JAXBException {
        EmotivaMenuNotifyDTO dto = (EmotivaMenuNotifyDTO) xmlUtils.unmarshallToEmotivaDTO(emotivaMenuNotifyProgress);
        assertThat(dto.getSequence(), is("2405"));
        assertThat(dto.getRow(), is(nullValue()));
        assertThat(dto.getProgress().getTime(), is("15"));
    }
}
