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

import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;

/**
 * Unit tests for EmotivaBarNotify message type.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaBarNotifyDTOTest extends AbstractDTOTestBase {

    public EmotivaBarNotifyDTOTest() throws JAXBException {
    }

    @Test
    void testUnmarshall() throws JAXBException {
        EmotivaBarNotifyWrapper dto = (EmotivaBarNotifyWrapper) xmlUtils
                .unmarshallToEmotivaDTO(emotivaBarNotifyBigText);
        assertThat(dto.getSequence(), is("98"));
        assertThat(dto.getTags().size(), is(1));

        List<EmotivaBarNotifyDTO> commands = xmlUtils.unmarshallToBarNotify(dto.getTags());
        assertThat(commands.get(0).getType(), is("bigText"));
        assertThat(commands.get(0).getText(), is("XBox One"));
        assertThat(commands.get(0).getUnits(), is(nullValue()));
        assertThat(commands.get(0).getMin(), is(nullValue()));
        assertThat(commands.get(0).getMax(), is(nullValue()));
    }
}
