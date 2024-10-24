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
 * Unit tests for EmotivaTransponder message type.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaTransponderDTOTest extends AbstractDTOTestBase {

    public EmotivaTransponderDTOTest() throws JAXBException {
    }

    @Test
    void unmarshallV2() throws JAXBException {
        var dto = (EmotivaTransponderDTO) xmlUtils.unmarshallToEmotivaDTO(emotivaTransponderResponseV2);

        assertThat(dto, is(notNullValue()));
        assertThat(dto.getModel(), is("XMC-1"));
        assertThat(dto.getRevision(), is("2.0"));
        assertThat(dto.getName(), is("Living Room"));
        assertThat(dto.getControl().getVersion(), is("2.0"));
        assertThat(dto.getControl().getControlPort(), is(7002));
        assertThat(dto.getControl().getNotifyPort(), is(7003));
        assertThat(dto.getControl().getInfoPort(), is(7004));
        assertThat(dto.getControl().getSetupPortTCP(), is(7100));
        assertThat(dto.getControl().getKeepAlive(), is(10000));
    }

    @Test
    void unmarshallV3() throws JAXBException {
        var dto = (EmotivaTransponderDTO) xmlUtils.unmarshallToEmotivaDTO(emotivaTransponderResponseV3);

        assertThat(dto, is(notNullValue()));
        assertThat(dto.getModel(), is("XMC-2"));
        assertThat(dto.getRevision(), is("3.0"));
        assertThat(dto.getName(), is("Living Room"));
        assertThat(dto.getControl().getVersion(), is("3.0"));
        assertThat(dto.getControl().getControlPort(), is(7002));
        assertThat(dto.getControl().getNotifyPort(), is(7003));
        assertThat(dto.getControl().getInfoPort(), is(7004));
        assertThat(dto.getControl().getSetupPortTCP(), is(7100));
        assertThat(dto.getControl().getKeepAlive(), is(10000));
    }
}
