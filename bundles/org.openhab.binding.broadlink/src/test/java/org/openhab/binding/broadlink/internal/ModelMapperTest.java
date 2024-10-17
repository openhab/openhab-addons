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
package org.openhab.binding.broadlink.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

/**
 * Tests that each Thing Type maps to the right model number.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class ModelMapperTest { // NOPMD

    private Logger mockLogger = Mockito.mock(Logger.class);

    @Test
    public void mapsSpMini2ASp2() {
        assertEquals(BroadlinkBindingConstants.THING_TYPE_SP2, ModelMapper.getThingType(0x7539, mockLogger));
    }

    @Test
    public void mapsRmMini3AsRm3() {
        assertEquals(BroadlinkBindingConstants.THING_TYPE_RM3, ModelMapper.getThingType(0x27c2, mockLogger));
    }

    @Test
    public void mapsRm35f36AsRm3Q() {
        assertEquals(BroadlinkBindingConstants.THING_TYPE_RM3Q, ModelMapper.getThingType(0x5f36, mockLogger));
    }

    @Test
    public void mapsRm4bAsRm4() {
        assertEquals(BroadlinkBindingConstants.THING_TYPE_RM4_MINI, ModelMapper.getThingType(0x51da, mockLogger));
    }

    @Test
    public void mapsRm4ProAsRm4() {
        assertEquals(BroadlinkBindingConstants.THING_TYPE_RM4_PRO, ModelMapper.getThingType(0x61a2, mockLogger));
    }

    @Test
    public void mapsRm462bcAsRm4() {
        assertEquals(BroadlinkBindingConstants.THING_TYPE_RM4_MINI, ModelMapper.getThingType(0x62bc, mockLogger));
    }

    @Test
    public void mapsRm4Model6026AsRm4() {
        assertEquals(BroadlinkBindingConstants.THING_TYPE_RM4_PRO, ModelMapper.getThingType(0x6026, mockLogger));
    }

    @Test
    public void mapsRm4Model24846AsRm4() {
        assertEquals(BroadlinkBindingConstants.THING_TYPE_RM4_MINI, ModelMapper.getThingType(24846, mockLogger));
    }

    @Test
    public void throwsOnUnrecognisedDeviceModel() {
        try {
            ModelMapper.getThingType(0x6666, mockLogger);
            Assertions.fail("Should have thrown on unmapped device model");
        } catch (Exception e) {
            assertEquals(
                    "Device identifying itself as '26214' (hex 0x6666) is not currently supported. Please report this to the developer!",
                    e.getMessage());
        }
    }
}
