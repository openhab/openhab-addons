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
package org.openhab.binding.shelly.internal.api;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsGlobal;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.Gson;

/**
 * Tests for {@link ShellyDeviceProfile}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ShellyDeviceProfileTest {

    private final Gson gson = new Gson();

    @ParameterizedTest
    @MethodSource("provideTestCasesForApiDetermination")
    void determineApi(ThingTypeUID thingTypeUid, boolean expectedIsGeneration2, boolean expectedIsBlu) {
        boolean actualIsGeneration2 = ShellyDeviceProfile.isGeneration2(thingTypeUid);
        assertThat(actualIsGeneration2, is(equalTo(expectedIsGeneration2)));

        boolean actualIsBlue = ShellyDeviceProfile.isBluSeries(thingTypeUid);
        assertThat(actualIsBlue, is(equalTo(expectedIsBlu)));
    }

    private static Stream<Arguments> provideTestCasesForApiDetermination() {
        return Stream.of( //
                // Shelly BLU
                Arguments.of(THING_TYPE_SHELLYBLUBUTTON, true, true), //
                Arguments.of(THING_TYPE_SHELLYBLUDW, true, true), //
                Arguments.of(THING_TYPE_SHELLYBLUMOTION, true, true), //
                Arguments.of(THING_TYPE_SHELLYBLUHT, true, true), //
                Arguments.of(THING_TYPE_SHELLYPLUSBLUGW, true, false), //
                // Shelly Bulb
                Arguments.of(THING_TYPE_SHELLYBULB, false, false), //
                // Generation 1
                Arguments.of(THING_TYPE_SHELLYDUO, false, false), //
                Arguments.of(THING_TYPE_SHELLYDUORGBW, false, false), //
                Arguments.of(THING_TYPE_SHELLYVINTAGE, false, false), //
                Arguments.of(THING_TYPE_SHELLYRGBW2_COLOR, false, false), //
                Arguments.of(THING_TYPE_SHELLYRGBW2_WHITE, false, false), //
                Arguments.of(THING_TYPE_SHELLY1, false, false), //
                Arguments.of(THING_TYPE_SHELLY1L, false, false), //
                Arguments.of(THING_TYPE_SHELLY1PM, false, false), //
                Arguments.of(THING_TYPE_SHELLYEM, false, false), //
                Arguments.of(THING_TYPE_SHELLY3EM, false, false), //
                Arguments.of(THING_TYPE_SHELLY2_RELAY, false, false), //
                Arguments.of(THING_TYPE_SHELLY2_ROLLER, false, false), //
                Arguments.of(THING_TYPE_SHELLY25_RELAY, false, false), //
                Arguments.of(THING_TYPE_SHELLY25_ROLLER, false, false), //
                Arguments.of(THING_TYPE_SHELLY4PRO, false, false), //
                Arguments.of(THING_TYPE_SHELLYPLUG, false, false), //
                Arguments.of(THING_TYPE_SHELLYPLUGS, false, false), //
                Arguments.of(THING_TYPE_SHELLYPLUGU1, false, false), //
                Arguments.of(THING_TYPE_SHELLYUNI, false, false), //
                Arguments.of(THING_TYPE_SHELLYDIMMER, false, false), //
                Arguments.of(THING_TYPE_SHELLYDIMMER2, false, false), //
                Arguments.of(THING_TYPE_SHELLYIX3, false, false), //
                Arguments.of(THING_TYPE_SHELLYHT, false, false), //
                Arguments.of(THING_TYPE_SHELLYSMOKE, false, false), //
                Arguments.of(THING_TYPE_SHELLYGAS, false, false), //
                Arguments.of(THING_TYPE_SHELLYFLOOD, false, false), //
                Arguments.of(THING_TYPE_SHELLYDOORWIN, false, false), //
                Arguments.of(THING_TYPE_SHELLYDOORWIN2, false, false), //
                Arguments.of(THING_TYPE_SHELLYSENSE, false, false), //
                Arguments.of(THING_TYPE_SHELLYBUTTON1, false, false), //
                Arguments.of(THING_TYPE_SHELLYBUTTON2, false, false), //
                Arguments.of(THING_TYPE_SHELLYMOTION, false, false), //
                Arguments.of(THING_TYPE_SHELLYTRV, false, false), //

                // Shelly Plus
                Arguments.of(THING_TYPE_SHELLYPLUS1, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUS1PM, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUS2PM_RELAY, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUS2PM_ROLLER, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSPLUGS, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSPLUGUS, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSI4, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSI4DC, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSEM, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUS3EM63, true, false), //

                // Shelly Mini series
                Arguments.of(THING_TYPE_SHELLYMINI_1, true, false), //
                Arguments.of(THING_TYPE_SHELLYMINI_1PM, true, false), //
                Arguments.of(THING_TYPE_SHELLYMINI_PM, true, false), //
                Arguments.of(THING_TYPE_SHELLYMINI_EM, true, false), //

                // Shelly Pro series
                Arguments.of(THING_TYPE_SHELLYPRO1, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO1PM, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO2, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO2PM_RELAY, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO2PM_ROLLER, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO3, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO3EM, true, false), //
                Arguments.of(THING_TYPE_SHELLYPROEM50, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO4PM, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSDIMMER, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSDIMMERUS, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSDIMMER10V, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSHT, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSSMOKE, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSWALLDISPLAY, true, false), //

                Arguments.of(THING_TYPE_SHELLYPROTECTED, false, false), // password protected device
                Arguments.of(THING_TYPE_SHELLYUNKNOWN, false, false)); // unknown device
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForBuildBluServiceName")
    void buildBluServiceName(String name, String mac, String expectedServiceName) {
        String actualServiceName = ShellyDeviceProfile.buildBluServiceName(name, mac);
        assertThat(actualServiceName, is(equalTo(expectedServiceName)));
    }

    private static Stream<Arguments> provideTestCasesForBuildBluServiceName() {
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
    void buildBluServiceNameWhenNameUnknownThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ShellyDeviceProfile.buildBluServiceName("sbbt", "001A2B3C4D5E");
        });
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForGetControlGroup")
    void getControlGroup(ThingTypeUID thingTypeUID, String mode, int numRollers, int numOutputs, int numLights,
            int index, String expectedControlGroup) throws ShellyApiException {
        ShellyDeviceProfile deviceProfile = new ShellyDeviceProfile();
        ShellySettingsGlobal settingsGlobal = new ShellySettingsGlobal();
        ShellySettingsDevice settingsDevice = new ShellySettingsDevice();

        settingsGlobal.mode = mode;
        settingsGlobal.relays = new ArrayList<>();
        settingsGlobal.lights = IntStream.range(0, numLights).mapToObj(i -> new ShellySettingsRgbwLight())
                .collect(Collectors.toCollection(ArrayList::new));
        settingsDevice.numRollers = numRollers;
        settingsDevice.numOutputs = numOutputs;
        deviceProfile.initialize(thingTypeUID, gson.toJson(settingsGlobal), settingsDevice);

        String actualControlGroup = deviceProfile.getControlGroup(index);
        assertThat("Thing type: " + thingTypeUID + ", mode: " + mode + ", numRollers: " + numRollers + ", numOutputs: "
                + numOutputs, actualControlGroup, is(equalTo(expectedControlGroup)));
    }

    private static Stream<Arguments> provideTestCasesForGetControlGroup() {
        return Stream.of( //
                Arguments.of(THING_TYPE_SHELLYPLUSDIMMERUS, "", 0, 0, 0, 0, CHANNEL_GROUP_DIMMER_CONTROL),
                Arguments.of(THING_TYPE_SHELLYPLUSDIMMER10V, "", 0, 0, 0, 0, CHANNEL_GROUP_DIMMER_CONTROL),
                Arguments.of(THING_TYPE_SHELLYDIMMER, "", 0, 0, 0, 0, CHANNEL_GROUP_DIMMER_CONTROL),
                Arguments.of(THING_TYPE_SHELLYDIMMER2, "", 0, 0, 0, 1, CHANNEL_GROUP_DIMMER_CONTROL),
                Arguments.of(THING_TYPE_SHELLY2_ROLLER, "roller", 0, 0, 0, 3, CHANNEL_GROUP_ROL_CONTROL),
                Arguments.of(THING_TYPE_SHELLY2_ROLLER, "Roller", 1, 0, 0, 3, CHANNEL_GROUP_ROL_CONTROL),
                Arguments.of(THING_TYPE_SHELLY2_ROLLER, "roller", 2, 0, 0, 3, CHANNEL_GROUP_ROL_CONTROL + "4"),
                Arguments.of(THING_TYPE_SHELLY25_RELAY, "", 0, 0, 0, 3, CHANNEL_GROUP_STATUS + "4"),
                Arguments.of(THING_TYPE_SHELLY25_RELAY, "", 0, 1, 0, 3, CHANNEL_GROUP_RELAY_CONTROL),
                Arguments.of(THING_TYPE_SHELLY25_RELAY, "", 0, 2, 0, 3, CHANNEL_GROUP_RELAY_CONTROL + "4"),
                Arguments.of(THING_TYPE_SHELLYRGBW2_COLOR, "", 0, 0, 0, 3, CHANNEL_GROUP_LIGHT_CONTROL),
                Arguments.of(THING_TYPE_SHELLYRGBW2_WHITE, "", 0, 0, 0, 3, CHANNEL_GROUP_LIGHT_CONTROL),
                Arguments.of(THING_TYPE_SHELLYPLUSRGBWPM, "", 0, 0, 1, 3, CHANNEL_GROUP_LIGHT_CONTROL),
                Arguments.of(THING_TYPE_SHELLYPLUSRGBWPM, "", 0, 0, 2, 3, CHANNEL_GROUP_LIGHT_CHANNEL + "4"),
                Arguments.of(THING_TYPE_SHELLYBULB, "", 0, 0, 2, 3, CHANNEL_GROUP_LIGHT_CONTROL),
                Arguments.of(THING_TYPE_SHELLYBUTTON1, "", 0, 0, 0, 5, CHANNEL_GROUP_STATUS),
                Arguments.of(THING_TYPE_SHELLYBUTTON2, "", 0, 0, 0, 5, CHANNEL_GROUP_STATUS),
                Arguments.of(THING_TYPE_SHELLYBLUBUTTON, "", 0, 0, 0, 5, CHANNEL_GROUP_STATUS),
                Arguments.of(THING_TYPE_SHELLYHT, "", 0, 0, 0, 5, CHANNEL_GROUP_SENSOR),
                Arguments.of(THING_TYPE_SHELLYFLOOD, "", 0, 0, 0, 5, CHANNEL_GROUP_SENSOR),
                Arguments.of(THING_TYPE_SHELLYDOORWIN, "", 0, 0, 0, 5, CHANNEL_GROUP_SENSOR),
                Arguments.of(THING_TYPE_SHELLYSMOKE, "", 0, 0, 0, 5, CHANNEL_GROUP_SENSOR),
                Arguments.of(THING_TYPE_SHELLYGAS, "", 0, 0, 0, 5, CHANNEL_GROUP_SENSOR),
                Arguments.of(THING_TYPE_SHELLYUNI, "", 0, 0, 0, 5, CHANNEL_GROUP_SENSOR),
                Arguments.of(THING_TYPE_SHELLYMOTION, "", 0, 0, 0, 5, CHANNEL_GROUP_SENSOR),
                Arguments.of(THING_TYPE_SHELLYSENSE, "", 0, 0, 0, 5, CHANNEL_GROUP_SENSOR),
                Arguments.of(THING_TYPE_SHELLYTRV, "", 0, 0, 0, 5, CHANNEL_GROUP_SENSOR),
                Arguments.of(THING_TYPE_SHELLYPLUSWALLDISPLAY, "", 0, 0, 0, 5, CHANNEL_GROUP_SENSOR));
    }
}
