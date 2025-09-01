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
package org.openhab.binding.shelly.internal.discovery;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.BINDING_ID;
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
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link ShellyThingCreator}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ShellyThingCreatorTest {
    private static final String DEVICE_ID = "000000000000";

    @ParameterizedTest
    @MethodSource("provideTestCasesForGetThingUIDThrowsForInvalidServiceName")
    void getThingUIDThrowsForInvalidServiceName(String serviceName) {
        assertThrows(IllegalArgumentException.class, () -> {
            ShellyThingCreator.getThingUID(serviceName);
        });
    }

    private static Stream<Arguments> provideTestCasesForGetThingUIDThrowsForInvalidServiceName() {
        return Stream.of(Arguments.of(""), Arguments.of("-", Arguments.of("foo"), Arguments.of("shellypmmini")));
    }

    @Test
    void getThingUIDForUnknownReturnsThingUidForUnknown() {
        ThingUID actual = ShellyThingCreator.getThingUIDForUnknown("johndoe-" + DEVICE_ID, "", "");
        ThingUID expected = new ThingUID(BINDING_ID, THING_TYPE_SHELLYUNKNOWN_STR, DEVICE_ID);

        assertThat(actual, is(equalTo(expected)));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForGetThingUIDReturnsThingUidAccordingToRuleset")
    void getThingUIDReturnsThingUidAccordingToRuleset(String serviceName, String mode,
            ThingTypeUID expectedThingTypeUid) {
        ThingUID actual = ShellyThingCreator.getThingUID(serviceName, "", mode);
        ThingUID expected = new ThingUID(expectedThingTypeUid, DEVICE_ID);

        assertThat("serviceName: " + serviceName + "; mode: " + mode, actual, is(equalTo(expected)));
        assertThat(SUPPORTED_THING_TYPES, hasItem(expectedThingTypeUid));
    }

    private static Stream<Arguments> provideTestCasesForGetThingUIDReturnsThingUidAccordingToRuleset() {
        return Stream.of( //
                Arguments.of("johndoe-" + DEVICE_ID, "", THING_TYPE_SHELLYUNKNOWN), //
                Arguments.of("shellyplug-" + DEVICE_ID, "", THING_TYPE_SHELLYPLUG), //
                Arguments.of("shellyplug-s-" + DEVICE_ID, "", THING_TYPE_SHELLYPLUGS), //
                Arguments.of("shellyplug-u1-" + DEVICE_ID, "", THING_TYPE_SHELLYPLUG), //
                Arguments.of("shellyplugu1-" + DEVICE_ID, "", THING_TYPE_SHELLYPLUGU1), //
                Arguments.of("shellyplusplug-" + DEVICE_ID, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of("shellyplugsg3-" + DEVICE_ID, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of("shellyplugus-" + DEVICE_ID, "", THING_TYPE_SHELLYPLUSPLUGUS), //
                Arguments.of("shellydimmer-" + DEVICE_ID, "", THING_TYPE_SHELLYDIMMER), //
                Arguments.of("shellydimmer2-" + DEVICE_ID, "", THING_TYPE_SHELLYDIMMER2), //
                Arguments.of("shellyem-" + DEVICE_ID, "", THING_TYPE_SHELLYEM), //
                Arguments.of("shellyem3-" + DEVICE_ID, "", THING_TYPE_SHELLY3EM), //
                Arguments.of("shellyrgbw2-" + DEVICE_ID, "color", THING_TYPE_SHELLYRGBW2_COLOR), //
                Arguments.of("shellyrgbw2-" + DEVICE_ID, "white", THING_TYPE_SHELLYRGBW2_WHITE), //
                Arguments.of("shellycolorbulb-" + DEVICE_ID, "", THING_TYPE_SHELLYDUORGBW), //
                Arguments.of("shellyvintage-" + DEVICE_ID, "", THING_TYPE_SHELLYVINTAGE), //
                Arguments.of("shellybulb-" + DEVICE_ID, "", THING_TYPE_SHELLYBULB), //
                Arguments.of("shellybulbduo-" + DEVICE_ID, "", THING_TYPE_SHELLYDUO), //
                Arguments.of("shellymotion-" + DEVICE_ID, "", THING_TYPE_SHELLYMOTION), //
                Arguments.of("shellymotion2-" + DEVICE_ID, "", THING_TYPE_SHELLYMOTION), //
                Arguments.of("shellymotionsensor-" + DEVICE_ID, "", THING_TYPE_SHELLYMOTION),
                Arguments.of("shellyflood-" + DEVICE_ID, "", THING_TYPE_SHELLYFLOOD),
                Arguments.of("shellyht-" + DEVICE_ID, "", THING_TYPE_SHELLYHT),
                Arguments.of("shellydw-" + DEVICE_ID, "", THING_TYPE_SHELLYDOORWIN),
                Arguments.of("shellydw2-" + DEVICE_ID, "", THING_TYPE_SHELLYDOORWIN2),
                Arguments.of("shellygas-" + DEVICE_ID, "", THING_TYPE_SHELLYGAS),
                Arguments.of("shellyuni-" + DEVICE_ID, "", THING_TYPE_SHELLYUNI),
                Arguments.of("shellyem-" + DEVICE_ID, "", THING_TYPE_SHELLYEM),
                Arguments.of("shellyem3-" + DEVICE_ID, "", THING_TYPE_SHELLY3EM),

                // Shelly Plus
                Arguments.of("shelly3em63g3-" + DEVICE_ID, "", THING_TYPE_SHELLYPLUS3EM63),

                // Shelly Pro
                Arguments.of("shellypro3em-" + DEVICE_ID, "", THING_TYPE_SHELLYPRO3EM),
                Arguments.of("shellypro3em3ct63-" + DEVICE_ID, "", THING_TYPE_SHELLYPRO3EM63),
                Arguments.of("shellypro3em400-" + DEVICE_ID, "", THING_TYPE_SHELLYPRO3EM400), //

                // Shelly BLU
                Arguments.of("shellyblubutton-" + DEVICE_ID, "", THING_TYPE_SHELLYBLUBUTTON), //
                Arguments.of("shellybluht-" + DEVICE_ID, "", THING_TYPE_SHELLYBLUHT), //
                Arguments.of("shellybludw-" + DEVICE_ID, "", THING_TYPE_SHELLYBLUDW), //
                Arguments.of("shellyblumotion-" + DEVICE_ID, "", THING_TYPE_SHELLYBLUMOTION) //
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForGetThingUIDReturnsThingUidByDeviceType")
    void getThingUIDReturnsThingUidByDeviceType(String deviceType, String mode, ThingTypeUID expectedThingTypeUid) {
        ThingUID actual = ShellyThingCreator.getThingUID("x-" + DEVICE_ID, deviceType, mode);
        ThingUID expected = new ThingUID(expectedThingTypeUid, DEVICE_ID);

        assertThat("deviceType: " + deviceType + "; mode: " + mode, actual, is(equalTo(expected)));
        assertThat(SUPPORTED_THING_TYPES, hasItem(expectedThingTypeUid));
    }

    private static Stream<Arguments> provideTestCasesForGetThingUIDReturnsThingUidByDeviceType() {
        return Stream.of( //
                Arguments.of(SHELLYDT_1PM, "", THING_TYPE_SHELLY1PM), //
                Arguments.of(SHELLYDT_1L, "", THING_TYPE_SHELLY1L), //
                Arguments.of(SHELLYDT_1, "", THING_TYPE_SHELLY1), //
                Arguments.of(SHELLYDT_SHELLY2, "relay", THING_TYPE_SHELLY2_RELAY), //
                Arguments.of(SHELLYDT_SHELLY2, "roller", THING_TYPE_SHELLY2_ROLLER), //
                Arguments.of(SHELLYDT_SHELLY25, "relay", THING_TYPE_SHELLY25_RELAY), //
                Arguments.of(SHELLYDT_SHELLY25, "roller", THING_TYPE_SHELLY25_ROLLER), //
                Arguments.of(SHELLYDT_SHPRO, "", THING_TYPE_SHELLY4PRO), //
                Arguments.of(SHELLYDT_PLUG, "", THING_TYPE_SHELLYPLUG), //
                Arguments.of(SHELLYDT_PLUGU1, "", THING_TYPE_SHELLYPLUGU1), //
                Arguments.of(SHELLYDT_3EM, "", THING_TYPE_SHELLY3EM), //
                Arguments.of(SHELLYDT_EM, "", THING_TYPE_SHELLYEM), //
                Arguments.of(SHELLYDT_PLUG, "", THING_TYPE_SHELLYPLUG), //
                Arguments.of(SHELLYDT_PLUGS, "", THING_TYPE_SHELLYPLUGS), //
                Arguments.of(SHELLYDT_PLUGU1, "", THING_TYPE_SHELLYPLUGU1), //
                Arguments.of(SHELLYDT_GAS, "", THING_TYPE_SHELLYGAS), //
                Arguments.of(SHELLYDT_DOORWINDOW, "", THING_TYPE_SHELLYDOORWIN), //
                Arguments.of(SHELLYDT_DOORWINDOW2, "", THING_TYPE_SHELLYDOORWIN2), //
                Arguments.of(SHELLYDT_DUO, "", THING_TYPE_SHELLYDUO), //
                Arguments.of(SHELLYDT_DUORGBW, "", THING_TYPE_SHELLYDUORGBW), //
                Arguments.of(SHELLYDT_BULB, "", THING_TYPE_SHELLYBULB), //
                Arguments.of(SHELLYDT_VINTAGE, "", THING_TYPE_SHELLYVINTAGE), //
                Arguments.of(SHELLYDT_DIMMER, "", THING_TYPE_SHELLYDIMMER), //
                Arguments.of(SHELLYDT_DIMMER2, "", THING_TYPE_SHELLYDIMMER2), //
                Arguments.of(SHELLYDT_IX3, "", THING_TYPE_SHELLYIX3), //
                Arguments.of(SHELLYDT_BUTTON1, "", THING_TYPE_SHELLYBUTTON1), //
                Arguments.of(SHELLYDT_BUTTON2, "", THING_TYPE_SHELLYBUTTON2), //
                Arguments.of(SHELLYDT_UNI, "", THING_TYPE_SHELLYUNI), //
                Arguments.of(SHELLYDT_HT, "", THING_TYPE_SHELLYHT), //
                Arguments.of(SHELLYDT_FLOOD, "", THING_TYPE_SHELLYFLOOD), //
                Arguments.of(SHELLYDT_SMOKE, "", THING_TYPE_SHELLYSMOKE), //
                Arguments.of(SHELLYDT_MOTION, "", THING_TYPE_SHELLYMOTION), //
                Arguments.of(SHELLYDT_EYE, "", THING_TYPE_SHELLYEYE), //
                Arguments.of(SHELLYDT_TRV, "", THING_TYPE_SHELLYTRV), //

                // Plus Series
                Arguments.of(SHELLYDT_PLUS1, "", THING_TYPE_SHELLYPLUS1), //
                Arguments.of(SHELLYDT_PLUS1G3, "", THING_TYPE_SHELLYPLUS1), //
                Arguments.of(SHELLYDT_PLUS1G4, "", THING_TYPE_SHELLYPLUS1), //
                Arguments.of(SHELLYDT_PLUS1PM, "", THING_TYPE_SHELLYPLUS1PM), //
                Arguments.of(SHELLYDT_PLUS1PMG3, "", THING_TYPE_SHELLYPLUS1PM), //
                Arguments.of(SHELLYDT_PLUS1PMG4, "", THING_TYPE_SHELLYPLUS1PM), //
                Arguments.of(SHELLYDT_PLUS1UL, "", THING_TYPE_SHELLYPLUS1), //
                Arguments.of(SHELLYDT_PLUS1PMUL, "", THING_TYPE_SHELLYPLUS1PM), //
                Arguments.of(SHELLYDT_PLUS1L, "", THING_TYPE_SHELLYPLUS1L), //
                Arguments.of(SHELLYDT_PLUS2PM, "relay", THING_TYPE_SHELLYPLUS2PM_RELAY), //
                Arguments.of(SHELLYDT_PLUS2PM, "roller", THING_TYPE_SHELLYPLUS2PM_ROLLER), //
                Arguments.of(SHELLYDT_PLUS2PM_2, "relay", THING_TYPE_SHELLYPLUS2PM_RELAY), //
                Arguments.of(SHELLYDT_PLUS2PM_2, "roller", THING_TYPE_SHELLYPLUS2PM_ROLLER), //
                Arguments.of(SHELLYDT_PLUS2PMG3, "relay", THING_TYPE_SHELLYPLUS2PM_RELAY), //
                Arguments.of(SHELLYDT_PLUS2PMG3, "roller", THING_TYPE_SHELLYPLUS2PM_ROLLER), //
                Arguments.of(SHELLYDT_PLUS2PMG4, "relay", THING_TYPE_SHELLYPLUS2PM_RELAY), //
                Arguments.of(SHELLYDT_PLUS2PMG4, "roller", THING_TYPE_SHELLYPLUS2PM_ROLLER), //
                Arguments.of(SHELLYDT_PLUS2L, "", THING_TYPE_SHELLYPLUS2L), //
                Arguments.of(SHELLYDT_PLUSSHUTTER, "", THING_TYPE_SHELLYPLUSSHUTTER), //
                Arguments.of(SHELLYDT_PLUSPLUGS, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSPLUGSG3, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSPLUGSAZ, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSPLUGIT, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSPLUGUK, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSPLUGUS, "", THING_TYPE_SHELLYPLUSPLUGUS), //
                Arguments.of(SHELLYDT_PLUSPLUGOUTDOORSG3, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSSTRIP, "", THING_TYPE_SHELLYPLUSSTRIP), //
                Arguments.of(SHELLYDT_PLUSI4DC, "", THING_TYPE_SHELLYPLUSI4DC), //
                Arguments.of(SHELLYDT_PLUSI4, "", THING_TYPE_SHELLYPLUSI4), //
                Arguments.of(SHELLYDT_PLUSI4G3, "", THING_TYPE_SHELLYPLUSI4), //
                Arguments.of(SHELLYDT_PLUSHT, "", THING_TYPE_SHELLYPLUSHT), //
                Arguments.of(SHELLYDT_PLUSHTG3, "", THING_TYPE_SHELLYPLUSHT), //
                Arguments.of(SHELLYDT_PLUSSMOKE, "", THING_TYPE_SHELLYPLUSSMOKE), //
                Arguments.of(SHELLYDT_PLUSUNI, "", THING_TYPE_SHELLYPLUSUNI), //
                Arguments.of(SHELLYDT_PLUSDIMMERUS, "", THING_TYPE_SHELLYPLUSDIMMERUS), //
                Arguments.of(SHELLYDT_PLUSDIMMER10V, "", THING_TYPE_SHELLYPLUSDIMMER10V), //
                Arguments.of(SHELLYDT_PLUSDIMMER0110VG3, "", THING_TYPE_SHELLYPLUSDIMMER10V), //
                Arguments.of(SHELLYDT_PLUSDIMMERG3, "", THING_TYPE_SHELLYPLUSDIMMER), //
                Arguments.of(SHELLYDT_PLUSEM, "", THING_TYPE_SHELLYPLUSEM), //
                Arguments.of(SHELLYDT_PLUS3EM63, "", THING_TYPE_SHELLYPLUS3EM63), //
                Arguments.of(SHELLYDT_PLUSRGBWPM, "", THING_TYPE_SHELLYPLUSRGBWPM),
                Arguments.of(SHELLYDT_PLUSBLUGW, "", THING_TYPE_SHELLYPLUSBLUGW), //

                // Plus Mini Series
                Arguments.of(SHELLYDT_MINI_1, "", THING_TYPE_SHELLYMINI_1), //
                Arguments.of(SHELLYDT_MINI_1G3, "", THING_TYPE_SHELLYMINI_1), //
                Arguments.of(SHELLYDT_MINI_1G4, "", THING_TYPE_SHELLYMINI_1), //
                Arguments.of(SHELLYDT_MINI_PM, "", THING_TYPE_SHELLYMINI_PM), //
                Arguments.of(SHELLYDT_MINI_PMG3, "", THING_TYPE_SHELLYMINI_PM), //
                Arguments.of(SHELLYDT_MINI_EM, "", THING_TYPE_SHELLYMINI_EM), //
                Arguments.of(SHELLYDT_MINI_1PM, "", THING_TYPE_SHELLYMINI_1PM), //
                Arguments.of(SHELLYDT_MINI_1PMG3, "", THING_TYPE_SHELLYMINI_1PM), //
                Arguments.of(SHELLYDT_MINI_1PMG4, "", THING_TYPE_SHELLYMINI_1PM), //

                // Pro Series
                Arguments.of(SHELLYDT_PRO1, "", THING_TYPE_SHELLYPRO1), //
                Arguments.of(SHELLYDT_PRO1_2, "", THING_TYPE_SHELLYPRO1), //
                Arguments.of(SHELLYDT_PRO1_3, "", THING_TYPE_SHELLYPRO1), //
                Arguments.of(SHELLYDT_PRO1UL, "", THING_TYPE_SHELLYPRO1), //
                Arguments.of(SHELLYDT_PRO1PM, "", THING_TYPE_SHELLYPRO1PM), //
                Arguments.of(SHELLYDT_PRO1PM_2, "", THING_TYPE_SHELLYPRO1PM), //
                Arguments.of(SHELLYDT_PRO1PM_3, "", THING_TYPE_SHELLYPRO1PM), //
                Arguments.of(SHELLYDT_PRO1PMUL, "", THING_TYPE_SHELLYPRO1PM), //
                Arguments.of(SHELLYDT_PRO1CB, "", THING_TYPE_SHELLYPRO1CB), //
                Arguments.of(SHELLYDT_PRO2, "", THING_TYPE_SHELLYPRO2), //
                Arguments.of(SHELLYDT_PRO2_2, "", THING_TYPE_SHELLYPRO2), //
                Arguments.of(SHELLYDT_PRO2_3, "", THING_TYPE_SHELLYPRO2), //
                Arguments.of(SHELLYDT_PRO2PM, "relay", THING_TYPE_SHELLYPRO2PM_RELAY), //
                Arguments.of(SHELLYDT_PRO2PM_2, "relay", THING_TYPE_SHELLYPRO2PM_RELAY), //
                Arguments.of(SHELLYDT_PRO2PM_3, "relay", THING_TYPE_SHELLYPRO2PM_RELAY), //
                Arguments.of(SHELLYDT_PRO2PM, "roller", THING_TYPE_SHELLYPRO2PM_ROLLER), //
                Arguments.of(SHELLYDT_PRO2PM_2, "roller", THING_TYPE_SHELLYPRO2PM_ROLLER), //
                Arguments.of(SHELLYDT_PRO2PM_3, "roller", THING_TYPE_SHELLYPRO2PM_ROLLER), //
                Arguments.of(SHELLYDT_PRO3, "", THING_TYPE_SHELLYPRO3), //
                Arguments.of(SHELLYDT_PRO4PM, "", THING_TYPE_SHELLYPRO4PM), //
                Arguments.of(SHELLYDT_PRO4PM_2, "", THING_TYPE_SHELLYPRO4PM), //
                Arguments.of(SHELLYDT_4PRO, "", THING_TYPE_SHELLYPRO4PM), //
                Arguments.of(SHELLYDT_PROEM50, "", THING_TYPE_SHELLYPROEM50), //
                Arguments.of(SHELLYDT_PRO3EM, "", THING_TYPE_SHELLYPRO3EM), //
                Arguments.of(SHELLYDT_PRO3EM3CT63, "", THING_TYPE_SHELLYPRO3EM63), //
                Arguments.of(SHELLYDT_PRO3EM400, "", THING_TYPE_SHELLYPRO3EM400), //

                // BLU Series
                Arguments.of(SHELLYDT_BLUBUTTON1, "", THING_TYPE_SHELLYBLUBUTTON), //
                Arguments.of(SHELLYDT_BLUHT, "", THING_TYPE_SHELLYBLUHT), //
                Arguments.of(SHELLYDT_BLUDW, "", THING_TYPE_SHELLYBLUDW), //
                Arguments.of(SHELLYDT_BLUMOTION, "", THING_TYPE_SHELLYBLUMOTION), //

                Arguments.of(SHELLYDT_BLUCLASS_BUTTON, "", THING_TYPE_SHELLYBLUBUTTON), //
                Arguments.of(SHELLYDT_BLUCLASS_HT, "", THING_TYPE_SHELLYBLUHT), //
                Arguments.of(SHELLYDT_BLUCLASS_DW, "", THING_TYPE_SHELLYBLUDW), //
                Arguments.of(SHELLYDT_BLUCLASS_MOTION, "", THING_TYPE_SHELLYBLUMOTION), //

                // Wall displays
                Arguments.of(SHELLYDT_PLUSWALLDISPLAY, "", THING_TYPE_SHELLYPLUSWALLDISPLAY));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForgetThingUIDDeviceTypeTakesPrecedence")
    void getThingUIDDeviceTypeTakesPrecedence(String serviceName, String deviceType,
            ThingTypeUID expectedThingTypeUid) {
        ThingUID actual = ShellyThingCreator.getThingUID(serviceName, deviceType, "");
        ThingUID expected = new ThingUID(expectedThingTypeUid, DEVICE_ID);

        assertThat(actual, is(equalTo(expected)));
    }

    private static Stream<Arguments> provideTestCasesForgetThingUIDDeviceTypeTakesPrecedence() {
        return Stream.of( //
                Arguments.of("shellyplusshutter-" + DEVICE_ID, SHELLYDT_PLUSSMOKE, THING_TYPE_SHELLYPLUSSMOKE), //
                Arguments.of("notfound-" + DEVICE_ID, SHELLYDT_PLUSSMOKE, THING_TYPE_SHELLYPLUSSMOKE));
    }

    @Test
    void getThingUIDReturnsThingTypeMatchingServiceName() {
        Set<ThingTypeUID> excludedThingTypeUids = new HashSet<>();
        excludedThingTypeUids.addAll(RELAY_THING_TYPE_BY_DEVICE_TYPE.values());
        excludedThingTypeUids.addAll(ROLLER_THING_TYPE_BY_DEVICE_TYPE.values());
        excludedThingTypeUids.addAll(GROUP_RGBW2_THING_TYPES);

        for (ThingTypeUID supportedThingTypeUid : SUPPORTED_THING_TYPES.stream()
                .filter(uid -> !excludedThingTypeUids.contains(uid)).toList()) {
            String thingTypeId = supportedThingTypeUid.getId();
            ThingUID actualThingUid = ShellyThingCreator.getThingUID(thingTypeId + "-" + DEVICE_ID);
            ThingUID expectedThingUid = new ThingUID(BINDING_ID, thingTypeId, DEVICE_ID);
            assertThat(actualThingUid, is(equalTo(expectedThingUid)));
        }
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForGetBluServiceName")
    void getBluServiceName(String name, String mac, String expectedServiceName) {
        String actualServiceName = ShellyThingCreator.getBluServiceName(name, mac);
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
            ShellyThingCreator.getBluServiceName("sbbt", "001A2B3C4D5E");
        });
    }
}
