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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Inputs.*;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.Commands.ZONE_INPUT_QUERY;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link InputConverterXML}.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class InputConverterXMLTest extends AbstractXMLProtocolTest {

    private InputConverterXML subject;

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        ctx.prepareForModel("HTR-4069");
        ctx.respondWith(String.format("<Main_Zone>%s</Main_Zone>", ZONE_INPUT_QUERY),
                "Main_Zone_Input_Input_Sel_Item.xml");
    }

    @Test
    public void when_noMapping_fromStateName_returnsCanonicalNames() {
        // arrange
        subject = new InputConverterXML(con, "");

        // act
        String hdmi1 = subject.fromStateName("HDMI1");
        String hdmi2 = subject.fromStateName("HDMI2");
        String av1 = subject.fromStateName("AV1");
        String av2 = subject.fromStateName("AV2");
        String audio1 = subject.fromStateName("AUDIO1");
        String audio2 = subject.fromStateName("AUDIO2");
        String bluetooth = subject.fromStateName(INPUT_BLUETOOTH);
        String usb = subject.fromStateName(INPUT_USB);
        String tuner = subject.fromStateName(INPUT_TUNER);
        String netRadio = subject.fromStateName(INPUT_NET_RADIO);
        String server = subject.fromStateName(INPUT_SERVER);
        String multiCastLink = subject.fromStateName(INPUT_MUSIC_CAST_LINK);
        String spotify = subject.fromStateName(INPUT_SPOTIFY);

        // assert
        assertEquals("HDMI1", hdmi1);
        assertEquals("HDMI2", hdmi2);
        assertEquals("AV1", av1);
        assertEquals("AV2", av2);
        assertEquals("AUDIO1", audio1);
        assertEquals("AUDIO2", audio2);
        assertEquals("Bluetooth", bluetooth);
        assertEquals("USB", usb);
        assertEquals("TUNER", tuner);
        assertEquals("NET RADIO", netRadio);
        assertEquals("SERVER", server);
        assertEquals("MusicCast Link", multiCastLink);
        assertEquals("Spotify", spotify);
    }

    @Test
    public void when_mapping_fromStateName_takesUserMappingAboveAll() {
        // arrange
        subject = new InputConverterXML(con, "HDMI1=HDMI 1,Bluetooth=BLUETOOTH");

        // act
        String hdmi1 = subject.fromStateName("HDMI1");
        String bluetooth = subject.fromStateName(INPUT_BLUETOOTH);

        // assert
        assertEquals("HDMI 1", hdmi1);
        assertEquals("BLUETOOTH", bluetooth);
    }
}
