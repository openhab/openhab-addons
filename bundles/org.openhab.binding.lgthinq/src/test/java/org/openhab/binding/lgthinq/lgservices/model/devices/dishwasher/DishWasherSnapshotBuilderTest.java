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
package org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityFactory;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the decoding of binary dishwasher monitoring data.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class DishWasherSnapshotBuilderTest {
    private final DishWasherSnapshotBuilder builder = new DishWasherSnapshotBuilder();
    private DishWasherCapability capability = new DishWasherCapability();

    @BeforeEach
    void setUp() throws IOException, LGThinqException {
        ClassLoader classLoader = getClass().getClassLoader();
        assertNotNull(classLoader);
        try (InputStream inputStream = classLoader.getResourceAsStream("thinq-dishwasher.json")) {
            assertNotNull(inputStream, "thinq-dishwasher.json not found in resources");
            capability = CapabilityFactory.getInstance().create(new ObjectMapper().readTree(inputStream),
                    DishWasherCapability.class);
        }
    }

    /**
     * Payloads reported by a D30 dishwasher in https://github.com/openhab/openhab-addons/issues/19726.
     * State 0 and 4 are both "@DW_STATE_POWER_OFF_W"; the door is bit 1 of Option1 (byte 11).
     */
    @ParameterizedTest
    @CsvSource({ //
            "BAAAAwoAAAMKAAByAAMAAAAAAAAAAAAB, 4, DV_POWER_OFF, 1, 03:10", //
            "AQAAAwoFAAMKAAByBAMAAAAAAAAAAAAB, 1, DV_POWER_ON, 1, 03:10", //
            "AAAAAwoAAAMKAABwAAMAAAAAAAAAAAAB, 0, DV_POWER_OFF, 0, 03:10", //
            "BAAAAwoAAAABAAAQAAAAAAAAAAAAAAAA, 4, DV_POWER_OFF, 0, 00:01" })
    void decodesStatePowerAndDoorFromBinaryData(String payload, String state, DevicePowerState power, String door,
            String remainingTime) throws LGThinqException {
        DishWasherSnapshot snapshot = builder.createFromBinary(Base64.getDecoder().decode(payload),
                capability.getMonitoringBinaryProtocol(), capability);

        assertEquals(state, snapshot.getState());
        assertEquals(power, snapshot.getPowerStatus());
        assertEquals(door, snapshot.getDoorLock());
        assertEquals(remainingTime, snapshot.getRemainingTime());
    }
}
