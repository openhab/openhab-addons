/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.panasonictv;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.panasonictv.internal.PanasonicTvBindingConstants.MUTE;
import static org.openhab.binding.panasonictv.internal.PanasonicTvBindingConstants.VOLUME;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.panasonictv.internal.StatusEventDTO;

/**
 * The {@link StatusEventTests} is a test class for status events
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class StatusEventTests {

    @Test
    public void parseTest() throws JAXBException {
        // @formatter:off
        String xmlMessage = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/RCS/\">" +
                "<InstanceID val=\"0\">" +
                "<PresetNameList val=\"FactoryDefaults\"/>" +
                "<Mute val=\"0\" channel=\"Master\"/>" +
                "<Volume val=\"11\" channel=\"Master\"/>" +
                "<X_AudioList val=\"-1,\"/>" +
                "<X_CurrentAudioID val=\"0\"/>" +
                "<X_DualMonoModeList val=\"-1,\"/>" +
                "<X_CurrentDualMonoModeID val=\"0\"/>" +
                "<X_SubtitleList val=\"-1,\"/>" +
                "<X_CurrentSubtitleID val=\"0\"/>" +
                "<X_SubtitleCharCodeList val=\"-1,\"/>" +
                "<X_CurrentSubtitleCharCodeID val=\"0\"/>" +
                "</InstanceID>" +
                "</Event>";
        // @formatter:on

        JAXBContext jc = JAXBContext.newInstance(StatusEventDTO.class);
        Unmarshaller un = jc.createUnmarshaller();
        xmlMessage = xmlMessage.replaceAll("xmlns(.*?)>", ">");
        StatusEventDTO statusEvent = (StatusEventDTO) un.unmarshal(new StringReader(xmlMessage));

        assertNotNull(statusEvent);
        assertNotNull(statusEvent.values);

        assertEquals(true, statusEvent.values.containsKey(VOLUME));
        assertEquals(true, statusEvent.values.containsKey(MUTE));
        assertEquals(11, statusEvent.values.size());
    }
}
