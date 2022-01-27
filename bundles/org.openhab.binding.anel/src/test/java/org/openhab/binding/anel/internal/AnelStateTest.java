/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.anel.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.anel.internal.state.AnelState;

/**
 * This class tests {@link AnelState}.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelStateTest implements IAnelTestStatus {

    @Test
    public void parseHomeV46Status() {
        final AnelState state = AnelState.of(STATUS_HOME_V46);
        assertThat(state.name, equalTo("NET-CONTROL"));
        assertThat(state.ip, equalTo("192.168.0.63"));
        assertThat(state.mac, equalTo("0.5.163.21.4.71"));
        assertNull(state.temperature);
        for (int i = 1; i <= 8; i++) {
            assertThat(state.relayName[i - 1], equalTo("Nr. " + i));
            assertThat(state.relayState[i - 1], is(i % 2 == 1));
            assertThat(state.relayLocked[i - 1], is(i > 3)); // 248 is binary for: 11111000, so first 3 are not locked
        }
        for (int i = 1; i <= 8; i++) {
            assertNull(state.ioName[i - 1]);
            assertNull(state.ioState[i - 1]);
            assertNull(state.ioIsInput[i - 1]);
        }
        assertNull(state.sensorTemperature);
        assertNull(state.sensorBrightness);
        assertNull(state.sensorHumidity);
    }

    @Test
    public void parseLockedStates() {
        final AnelState state = AnelState.of(STATUS_HOME_V46.replaceAll(":\\d+:80:", ":236:80:"));
        assertThat(state.relayLocked[0], is(false));
        assertThat(state.relayLocked[1], is(false));
        assertThat(state.relayLocked[2], is(true));
        assertThat(state.relayLocked[3], is(true));
        assertThat(state.relayLocked[4], is(false));
        assertThat(state.relayLocked[5], is(true));
        assertThat(state.relayLocked[6], is(true));
        assertThat(state.relayLocked[7], is(true));
    }

    @Test
    public void parseHutV65Status() {
        final AnelState state = AnelState.of(STATUS_HUT_V65);
        assertThat(state.name, equalTo("NET-CONTROL"));
        assertThat(state.ip, equalTo("192.168.0.64"));
        assertThat(state.mac, equalTo("0.5.163.17.9.116"));
        assertThat(state.temperature, equalTo("27.0"));
        for (int i = 1; i <= 8; i++) {
            assertThat(state.relayName[i - 1], equalTo("Nr." + i));
            assertThat(state.relayState[i - 1], is(i % 2 == 0));
            assertThat(state.relayLocked[i - 1], is(i > 3)); // 248 is binary for: 11111000, so first 3 are not locked
        }
        for (int i = 1; i <= 8; i++) {
            assertThat(state.ioName[i - 1], equalTo("IO-" + i));
            assertThat(state.ioState[i - 1], is(false));
            assertThat(state.ioIsInput[i - 1], is(i >= 5));
        }
        assertNull(state.sensorTemperature);
        assertNull(state.sensorBrightness);
        assertNull(state.sensorHumidity);
    }

    @Test
    public void parseHutV5Status() {
        final AnelState state = AnelState.of(STATUS_HUT_V5);
        assertThat(state.name, equalTo("ANEL1"));
        assertThat(state.ip, equalTo("192.168.0.244"));
        assertThat(state.mac, equalTo("0.5.163.14.7.91"));
        assertThat(state.temperature, equalTo("27.3"));
        for (int i = 1; i <= 8; i++) {
            assertThat(state.relayName[i - 1], matchesPattern(".+"));
            assertThat(state.relayState[i - 1], is(false));
            assertThat(state.relayLocked[i - 1], is(false));
        }
        for (int i = 1; i <= 8; i++) {
            assertThat(state.ioName[i - 1], matchesPattern(".+"));
            assertThat(state.ioState[i - 1], is(true));
            assertThat(state.ioIsInput[i - 1], is(true));
        }
        assertNull(state.sensorTemperature);
        assertNull(state.sensorBrightness);
        assertNull(state.sensorHumidity);
    }

    @Test
    public void parseHutV61StatusAndSensor() {
        final AnelState state = AnelState.of(STATUS_HUT_V61_POW_SENSOR);
        assertThat(state.name, equalTo("NET-CONTROL"));
        assertThat(state.ip, equalTo("192.168.178.148"));
        assertThat(state.mac, equalTo("0.4.163.10.9.107"));
        assertThat(state.temperature, equalTo("27.7"));
        for (int i = 1; i <= 8; i++) {
            assertThat(state.relayName[i - 1], equalTo("Nr. " + i));
            assertThat(state.relayState[i - 1], is(i <= 3 || i >= 7));
            assertThat(state.relayLocked[i - 1], is(false));
        }
        for (int i = 1; i <= 8; i++) {
            assertThat(state.ioName[i - 1], equalTo("IO-" + i));
            assertThat(state.ioState[i - 1], is(false));
            assertThat(state.ioIsInput[i - 1], is(false));
        }
        assertThat(state.sensorTemperature, equalTo("20.61"));
        assertThat(state.sensorHumidity, equalTo("40.7"));
        assertThat(state.sensorBrightness, equalTo("7.0"));
    }

    @Test
    public void parseHutV61StatusWithSensor() {
        final AnelState state = AnelState.of(STATUS_HUT_V61_SENSOR);
        assertThat(state.name, equalTo("NET-CONTROL"));
        assertThat(state.ip, equalTo("192.168.178.148"));
        assertThat(state.mac, equalTo("0.4.163.10.9.107"));
        assertThat(state.temperature, equalTo("27.7"));
        for (int i = 1; i <= 8; i++) {
            assertThat(state.relayName[i - 1], equalTo("Nr. " + i));
            assertThat(state.relayState[i - 1], is(i <= 3 || i >= 7));
            assertThat(state.relayLocked[i - 1], is(false));
        }
        for (int i = 1; i <= 8; i++) {
            assertThat(state.ioName[i - 1], equalTo("IO-" + i));
            assertThat(state.ioState[i - 1], is(false));
            assertThat(state.ioIsInput[i - 1], is(false));
        }
        assertThat(state.sensorTemperature, equalTo("20.61"));
        assertThat(state.sensorHumidity, equalTo("40.7"));
        assertThat(state.sensorBrightness, equalTo("7.0"));
    }

    @Test
    public void parseHutV61StatusWithoutSensor() {
        final AnelState state = AnelState.of(STATUS_HUT_V61_POW);
        assertThat(state.name, equalTo("NET-CONTROL"));
        assertThat(state.ip, equalTo("192.168.178.148"));
        assertThat(state.mac, equalTo("0.4.163.10.9.107"));
        assertThat(state.temperature, equalTo("27.7"));
        for (int i = 1; i <= 8; i++) {
            assertThat(state.relayName[i - 1], equalTo("Nr. " + i));
            assertThat(state.relayState[i - 1], is(i <= 3 || i >= 7));
            assertThat(state.relayLocked[i - 1], is(false));
        }
        for (int i = 1; i <= 8; i++) {
            assertThat(state.ioName[i - 1], equalTo("IO-" + i));
            assertThat(state.ioState[i - 1], is(false));
            assertThat(state.ioIsInput[i - 1], is(false));
        }
        assertNull(state.sensorTemperature);
        assertNull(state.sensorBrightness);
        assertNull(state.sensorHumidity);
    }

    @Test
    public void colonSeparatorInSwitchNameThrowsException() {
        try {
            AnelState.of(STATUS_INVALID_NAME);
            fail("Status format exception expected because of colon separator in name 'Nr: 3'");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("is expected to be a number but it's not"));
        }
    }
}
