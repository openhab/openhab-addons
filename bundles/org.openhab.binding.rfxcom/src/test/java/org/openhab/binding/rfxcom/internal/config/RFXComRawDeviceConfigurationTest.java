/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.rfxcom.internal.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidParameterException;

/**
 * Configuration class for Raw devices.
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class RFXComRawDeviceConfigurationTest {

    private RFXComRawDeviceConfiguration config;

    @BeforeEach
    public void before() {
        config = new RFXComRawDeviceConfiguration();
        config.deviceId = "RAW";
        config.subType = "RAW_PACKET1";
    }

    @Test
    public void testConfigWithoutPulses() {
        assertDoesNotThrow(() -> config.parseAndValidate());
    }

    @Test
    public void testConfigWithUnevenPulses() {
        config.onPulses = "100 200 300";
        assertThrows(RFXComInvalidParameterException.class, () -> config.parseAndValidate());
    }

    @Test
    public void testConfigWithTooManyPulses() {
        String pulses = IntStream.range(1, 126).mapToObj(Integer::toString).collect(Collectors.joining(" "));
        config.offPulses = pulses;
        assertThrows(RFXComInvalidParameterException.class, () -> config.parseAndValidate());
    }

    @Test
    public void testConfigWithTooLargePulse() {
        config.openPulses = "100000 200000";
        assertThrows(RFXComInvalidParameterException.class, () -> config.parseAndValidate());
    }

    @Test
    public void testConfigWithNaN() {
        config.closedPulses = "abc def";
        assertThrows(RFXComInvalidParameterException.class, () -> config.parseAndValidate());
    }

    @Test
    public void testConfigWithValidPulses() {
        config.onPulses = "100 200 300 400";
        config.offPulses = "500 600 700 800";
        assertDoesNotThrow(() -> config.parseAndValidate());
    }
}
