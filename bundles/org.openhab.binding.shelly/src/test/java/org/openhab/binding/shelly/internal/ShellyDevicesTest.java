/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Tests for {@link ShellyDevices}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ShellyDevicesTest {
    @ParameterizedTest
    @MethodSource("provideTestCasesForGetBluServiceName")
    void getBluServiceName(String name, String mac, String expectedServiceName) {
        String actualServiceName = ShellyDevices.getBluServiceName(name, mac);
        assertThat(actualServiceName, is(equalTo(expectedServiceName)));
    }

    private static Stream<Arguments> provideTestCasesForGetBluServiceName() {
        return Stream.of( //
                Arguments.of("SBBT", "001A2B3C4D5E", "shellyblubutton-001a2b3c4d5e"), //
                Arguments.of("SBBT-02C", "001A2B3C4D5E", "shellyblubutton-001a2b3c4d5e"), //
                Arguments.of("SBBT-02C-03D", "001A2B3C4D5E", "shellyblubutton-001a2b3c4d5e"), //
                Arguments.of("SBDW", "001A2B3C4D5E", "shellybludw-001a2b3c4d5e"), //
                Arguments.of("SBMO", "001A2B3C4D5E", "shellyblumotion-001a2b3c4d5e"), //
                Arguments.of("SBHT", "001A2B3C4D5E", "shellybluht-001a2b3c4d5e"), //
                Arguments.of("SBHT", "00:1A:2B:3C:4D:5E", "shellybluht-001a2b3c4d5e"));
    }

    @Test
    void getBluServiceNameWhenNameUnknownThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ShellyDevices.getBluServiceName("sbbt", "001A2B3C4D5E");
        });
    }

    @Test
    void thingTypesByDeviceTypeAreSupported() {
        Set<ThingTypeUID> missingThingTypes = new HashSet<>(THING_TYPE_BY_DEVICE_TYPE.values());
        missingThingTypes.removeAll(SUPPORTED_THING_TYPES);
        assertThat("SUPPORTED_THING_TYPES must include values of THING_TYPE_BY_DEVICE_TYPE", missingThingTypes,
                is(empty()));
    }

    @Test
    void relayThingTypesByDeviceTypeAreSupported() {
        Set<ThingTypeUID> missingThingTypes = new HashSet<>(RELAY_THING_TYPE_BY_DEVICE_TYPE.values());
        missingThingTypes.removeAll(SUPPORTED_THING_TYPES);
        assertThat("SUPPORTED_THING_TYPES must include values of RELAY_THING_TYPE_BY_DEVICE_TYPE", missingThingTypes,
                is(empty()));
    }

    @Test
    void rollerThingTypesByDeviceTypeAreSupported() {
        Set<ThingTypeUID> missingThingTypes = new HashSet<>(ROLLER_THING_TYPE_BY_DEVICE_TYPE.values());
        missingThingTypes.removeAll(SUPPORTED_THING_TYPES);
        assertThat("SUPPORTED_THING_TYPES must include values of ROLLER_THING_TYPE_BY_DEVICE_TYPE", missingThingTypes,
                is(empty()));
    }

    @Test
    void thingTypesByServiceNameAreSupported() {
        Set<ThingTypeUID> missingThingTypes = new HashSet<>(THING_TYPE_BY_SERVICE_NAME.values());
        missingThingTypes.removeAll(SUPPORTED_THING_TYPES);
        assertThat("SUPPORTED_THING_TYPES must include values of THING_TYPE_BY_SERVICE_NAME", missingThingTypes,
                is(empty()));
    }

    @Test
    void numMetersByThingTypeAreSupported() {
        Set<ThingTypeUID> missingThingTypes = new HashSet<>(THING_TYPE_CAP_NUM_METERS.keySet());
        missingThingTypes.removeAll(SUPPORTED_THING_TYPES);
        assertThat("SUPPORTED_THING_TYPES must include keys of THING_TYPE_CAP_NUM_METERS", missingThingTypes,
                is(empty()));
    }
}
