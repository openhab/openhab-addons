/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.miflora.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.miflora.internal.MiBeaconParser.Frame;
import org.openhab.binding.bluetooth.miflora.internal.MiBeaconParser.Reading;
import org.openhab.core.util.HexUtils;

/**
 * Tests {@link MiBeaconParser} with frames other than the captured ones.
 *
 * @author Lukas Brinias - Initial contribution
 */
@NonNullByDefault
public class MiBeaconParserTest {

    private static Frame parse(String hex) {
        Frame frame = MiBeaconParser.parse(HexUtils.hexToBytes(hex));
        assertNotNull(frame, hex);
        return frame;
    }

    @Test
    public void capturedFrame() {
        Frame frame = parse("712098009627B1127E855C0D041002DD00");
        assertEquals(0x0098, frame.productId());
        assertEquals(0x96, frame.frameCounter());
        assertEquals(List.of(new Reading(MiBeaconParser.OBJECT_TEMPERATURE, 221)), frame.readings());
    }

    @Test
    public void negativeTemperature() {
        // -2.5 °C
        assertEquals(List.of(new Reading(MiBeaconParser.OBJECT_TEMPERATURE, -25)),
                parse("712098009627B1127E855C0D041002E7FF").readings());
    }

    @Test
    public void frameWithoutAddress() {
        // Frame control 0x2061: address flag cleared
        assertEquals(List.of(new Reading(MiBeaconParser.OBJECT_MOISTURE, 40)),
                parse("6120980097" + "0D" + "08100128").readings());
    }

    @Test
    public void capabilityWithIoByte() {
        // Capability 0x2D has bit 0x20 set, so an I/O capability byte follows
        assertEquals(List.of(new Reading(MiBeaconParser.OBJECT_MOISTURE, 40)),
                parse("7120980097BCB8127E855C" + "2D00" + "08100128").readings());
    }

    @Test
    public void severalObjects() {
        assertEquals(
                List.of(new Reading(MiBeaconParser.OBJECT_MOISTURE, 40),
                        new Reading(MiBeaconParser.OBJECT_CONDUCTIVITY, 725)),
                parse("7120980097BCB8127E855C0D" + "08100128" + "091002D502").readings());
    }

    @Test
    public void longObjectIsSkipped() {
        assertEquals(List.of(new Reading(MiBeaconParser.OBJECT_MOISTURE, 40)),
                parse("7120980097BCB8127E855C0D" + "FF000501020304FF" + "08100128").readings());
    }

    @Test
    public void rejectedFrames() {
        String[] rejected = { "", //
                "71209800", // shorter than the header
                "7920980097BCB8127E855C0D08100128", // encrypted
                "F120980097BCB8127E855C0D08100128", // mesh
                "3120980097BCB8127E855C0D08100128", // no object
                "7120980097BCB8127E855C0D081001", // object cut off
                "7120980097BCB8127E855C0D0810012800", // trailing byte
                "7120980097BCB8127E855C0D081000", // empty object
                "7120980097BCB8127E855C0D", // object flag set, but no object
        };
        for (String hex : rejected) {
            assertNull(MiBeaconParser.parse(HexUtils.hexToBytes(hex)), hex);
        }
    }
}
