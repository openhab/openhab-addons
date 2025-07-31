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
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.*;

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
        return Stream.of(Arguments.of(""), Arguments.of("-", Arguments.of("foo")));
    }

    @Test
    void getThingUIDForUnknownReturnsThingUidForUnknown() {
        ThingUID actual = ShellyThingCreator.getThingUIDForUnknown("johndoe-" + DEVICE_ID, "", "");
        ThingUID expected = new ThingUID(BINDING_ID, THING_TYPE_SHELLYPROTECTED_STR, DEVICE_ID);

        assertThat(actual, is(equalTo(expected)));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForGetThingUIDReturnsThingUidAccordingToRuleset")
    void getThingUIDReturnsThingUidAccordingToRuleset(String serviceName, String deviceType, String mode,
            ThingTypeUID expectedThingTypeUid) {
        ThingUID actual = ShellyThingCreator.getThingUID(serviceName, deviceType, mode);
        ThingUID expected = new ThingUID(expectedThingTypeUid, DEVICE_ID);

        assertThat("serviceName: " + serviceName + "; deviceType: " + deviceType + "; mode: " + mode, actual,
                is(equalTo(expected)));
        assertThat(SUPPORTED_THING_TYPES, hasItem(expectedThingTypeUid));
    }

    private static Stream<Arguments> provideTestCasesForGetThingUIDReturnsThingUidAccordingToRuleset() {
        return Stream.of( //
                Arguments.of("johndoe-" + DEVICE_ID, "", "", THING_TYPE_SHELLYUNKNOWN), //
                Arguments.of("shellyazplug-" + DEVICE_ID, SHELLYDT_PLUSPLUGSG3, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of("shellyoutdoorsg3-" + DEVICE_ID, SHELLYDT_PLUSPLUGOUTDOORSG3, "",
                        THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of("shellyswitch25-" + DEVICE_ID, "", "relay", THING_TYPE_SHELLY25_RELAY), //
                Arguments.of("shellyswitch25xx-" + DEVICE_ID, "", "relay", THING_TYPE_SHELLY25_RELAY), //
                Arguments.of("shellyswitch25xx-" + DEVICE_ID, "", "", THING_TYPE_SHELLY25_ROLLER), //
                Arguments.of("shellyswitch25xx-" + DEVICE_ID, "", "relax", THING_TYPE_SHELLY25_ROLLER), //
                Arguments.of("shellyswitch26xx-" + DEVICE_ID, "", "relay", THING_TYPE_SHELLY2_RELAY), //
                Arguments.of("shellyswitch-" + DEVICE_ID, "", "relay", THING_TYPE_SHELLY2_RELAY), //
                Arguments.of("shellyswitch-" + DEVICE_ID, "", "", THING_TYPE_SHELLY2_ROLLER), //
                Arguments.of("shellyplug-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUG), //
                Arguments.of("shellyplug-u1-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUG), //
                Arguments.of("shellyplugs-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUGS), //
                Arguments.of("shellyplug-s-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUGS), //
                Arguments.of("shellyplug-su1-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUGS), //
                Arguments.of("shellyplugu1-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUGU1), //
                Arguments.of("shellyplugu12-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUGU1), //
                Arguments.of("shellyplugus-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUSPLUGUS), //
                Arguments.of("shellyrgbw2-" + DEVICE_ID, "", "color", THING_TYPE_SHELLYRGBW2_COLOR), //
                Arguments.of("shellyrgbw2-" + DEVICE_ID, "", "", THING_TYPE_SHELLYRGBW2_WHITE), //
                Arguments.of("shellyrgbw2-" + DEVICE_ID, "", "colour", THING_TYPE_SHELLYRGBW2_WHITE), //
                Arguments.of("shellymotion-" + DEVICE_ID, "", "", THING_TYPE_SHELLYMOTION), //
                Arguments.of("shellymotionsensor-" + DEVICE_ID, "", "", THING_TYPE_SHELLYMOTION));
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
                Arguments.of(SHELLYDT_SHPRO, "", THING_TYPE_SHELLY4PRO), //
                Arguments.of(SHELLYDT_4PRO, "", THING_TYPE_SHELLY4PRO), //
                Arguments.of(SHELLYDT_3EM, "", THING_TYPE_SHELLY3EM), //
                Arguments.of(SHELLYDT_EM, "", THING_TYPE_SHELLYEM), //
                Arguments.of(SHELLYDT_SHPLG_S, "", THING_TYPE_SHELLYPLUGS), //
                Arguments.of(SHELLYDT_SHPLG_U1, "", THING_TYPE_SHELLYPLUGU1), //
                Arguments.of(SHELLYDT_GAS, "", THING_TYPE_SHELLYGAS), //
                Arguments.of(SHELLYDT_DW, "", THING_TYPE_SHELLYDOORWIN), //
                Arguments.of(SHELLYDT_DW2, "", THING_TYPE_SHELLYDOORWIN2), //
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
                Arguments.of(SHELLYDT_TRV, "", THING_TYPE_SHELLYTRV), //
                Arguments.of(SHELLYDT_MOTION, "", THING_TYPE_SHELLYMOTION), //
                // Plus Series
                Arguments.of(SHELLYDT_PLUS1, "", THING_TYPE_SHELLYPLUS1), //
                Arguments.of(SHELLYDT_PLUS1G3, "", THING_TYPE_SHELLYPLUS1), //
                Arguments.of(SHELLYDT_PLUS1PM, "", THING_TYPE_SHELLYPLUS1PM), //
                Arguments.of(SHELLYDT_PLUS1PMG3, "", THING_TYPE_SHELLYPLUS1PM), //
                Arguments.of(SHELLYDT_PLUS1UL, "", THING_TYPE_SHELLYPLUS1), //
                Arguments.of(SHELLYDT_PLUS1PMUL, "", THING_TYPE_SHELLYPLUS1PM), //
                Arguments.of("SNSW-002P16EU", "relay", THING_TYPE_SHELLYPLUS2PM_RELAY), //
                Arguments.of("SNSW-002P16EU", "roller", THING_TYPE_SHELLYPLUS2PM_ROLLER), //
                Arguments.of("SNSW-102P16EU", "relay", THING_TYPE_SHELLYPLUS2PM_RELAY), //
                Arguments.of("SNSW-102P16EU", "roller", THING_TYPE_SHELLYPLUS2PM_ROLLER), //
                Arguments.of(SHELLYDT_PLUSPLUGS, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSPLUGSG3, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSPLUGIT, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSPLUGOUTDOORSG3, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSPLUGUK, "", THING_TYPE_SHELLYPLUSPLUGS), //
                Arguments.of(SHELLYDT_PLUSPLUGUS, "", THING_TYPE_SHELLYPLUSPLUGUS), //
                Arguments.of(SHELLYDT_PLUSI4DC, "", THING_TYPE_SHELLYPLUSI4DC), //
                Arguments.of(SHELLYDT_PLUSI4, "", THING_TYPE_SHELLYPLUSI4), //
                Arguments.of(SHELLYDT_PLUSI4G3, "", THING_TYPE_SHELLYPLUSI4), //
                Arguments.of(SHELLYDT_PLUSHT, "", THING_TYPE_SHELLYPLUSHT), //
                Arguments.of(SHELLYDT_PLUSHTG3, "", THING_TYPE_SHELLYPLUSHTG3), //
                Arguments.of(SHELLYDT_PLUSSMOKE, "", THING_TYPE_SHELLYPLUSSMOKE), //
                Arguments.of(SHELLYDT_PLUSUNI, "", THING_TYPE_SHELLYPLUSUNI), //
                Arguments.of(SHELLYDT_PLUSDIMMERUS, "", THING_TYPE_SHELLYPLUSDIMMERUS), //
                Arguments.of(SHELLYDT_PLUSDIMMER10V, "", THING_TYPE_SHELLYPLUSDIMMER10V), //
                Arguments.of(SHELLYDT_PLUSDIMMER0110VG3, "", THING_TYPE_SHELLYPLUSDIMMER10V), //

                // Plus Mini Series
                Arguments.of(SHELLYDT_MINI1, "", THING_TYPE_SHELLY1MINI), //
                Arguments.of(SHELLYDT_MINIPM, "", THING_TYPE_SHELLYPMMINI), //
                Arguments.of(SHELLYDT_MINI1PM, "", THING_TYPE_SHELLY1PMMINI), //
                Arguments.of(SHELLYDT_MINIG3_1, "", THING_TYPE_SHELLY1MINI), //
                Arguments.of(SHELLYDT_MINIG3_PM, "", THING_TYPE_SHELLYPMMINI), //
                Arguments.of(SHELLYDT_MINIG3_1PM, "", THING_TYPE_SHELLY1PMMINI), //
                // Pro Series
                Arguments.of(SHELLYDT_PRO1, "", THING_TYPE_SHELLYPRO1), //
                Arguments.of(SHELLYDT_PRO1_2, "", THING_TYPE_SHELLYPRO1), //
                Arguments.of(SHELLYDT_PRO1_3, "", THING_TYPE_SHELLYPRO1), //
                Arguments.of(SHELLYDT_PRO1PM, "", THING_TYPE_SHELLYPRO1PM), //
                Arguments.of(SHELLYDT_PRO1PM_2, "", THING_TYPE_SHELLYPRO1PM), //
                Arguments.of(SHELLYDT_PRO1PM_3, "", THING_TYPE_SHELLYPRO1PM), //
                Arguments.of(SHELLYDT_PRO1CB, "", THING_TYPE_SHELLYPRO1CB), //
                Arguments.of("SPSW-002XE16EU", "", THING_TYPE_SHELLYPRO2_RELAY), //
                Arguments.of("SPSW-102XE16EU", "", THING_TYPE_SHELLYPRO2_RELAY), //
                Arguments.of("SPSW-202XE16EU", "", THING_TYPE_SHELLYPRO2_RELAY), //
                Arguments.of("SPSW-002PE16EU", "relay", THING_TYPE_SHELLYPRO2PM_RELAY), //
                Arguments.of("SPSW-102PE16EU", "relay", THING_TYPE_SHELLYPRO2PM_RELAY), //
                Arguments.of("SPSW-202PE16EU", "relay", THING_TYPE_SHELLYPRO2PM_RELAY), //
                Arguments.of("SPSW-002PE16EU", "roller", THING_TYPE_SHELLYPRO2PM_ROLLER), //
                Arguments.of("SPSW-102PE16EU", "roller", THING_TYPE_SHELLYPRO2PM_ROLLER), //
                Arguments.of("SPSW-202PE16EU", "roller", THING_TYPE_SHELLYPRO2PM_ROLLER), //
                Arguments.of(SHELLYDT_PRO3, "", THING_TYPE_SHELLYPRO3), //
                Arguments.of(SHELLYDT_PROEM50, "", THING_TYPE_SHELLYPROEM50), //
                Arguments.of(SHELLYDT_PRO3EM, "", THING_TYPE_SHELLYPRO3EM), //
                Arguments.of(SHELLYDT_PRO4PM, "", THING_TYPE_SHELLYPRO4PM), //
                Arguments.of(SHELLYDT_PRO4PM_2, "", THING_TYPE_SHELLYPRO4PM), //
                // BLU Series
                Arguments.of(SHELLYDT_BLUBUTTON, "", THING_TYPE_SHELLYBLUBUTTON), //
                Arguments.of(SHELLYDT_BLUDW, "", THING_TYPE_SHELLYBLUDW), //
                Arguments.of(SHELLYDT_BLUMOTION, "", THING_TYPE_SHELLYBLUMOTION), //
                Arguments.of(SHELLYDT_BLUHT, "", THING_TYPE_SHELLYBLUHT), //
                Arguments.of(SHELLYDT_BLUGW, "", THING_TYPE_SHELLYBLUGW), //
                // Wall displays
                Arguments.of(SHELLYDT_PLUSWALLDISPLAY, "", THING_TYPE_SHELLYPLUSWALLDISPLAY));
    }

    @Test
    void getThingUIDReturnsThingTypeMatchingServiceName() {
        Set<ThingTypeUID> excludedThingTypeUids = Set.of(THING_TYPE_SHELLY2_RELAY, THING_TYPE_SHELLY2_ROLLER,
                THING_TYPE_SHELLY25_ROLLER, THING_TYPE_SHELLY25_RELAY, THING_TYPE_SHELLYPLUSHTG3,
                THING_TYPE_SHELLYPLUS2PM_RELAY, THING_TYPE_SHELLYPLUS2PM_ROLLER, THING_TYPE_SHELLYPRO2_RELAY,
                THING_TYPE_SHELLYPRO2PM_ROLLER, THING_TYPE_SHELLYPRO2PM_RELAY, THING_TYPE_SHELLYRGBW2_COLOR);

        for (ThingTypeUID supportedThingTypeUid : SUPPORTED_THING_TYPES.stream()
                .filter(uid -> !excludedThingTypeUids.contains(uid)).toList()) {
            String thingTypeId = supportedThingTypeUid.getId();
            ThingUID actualThingUid = ShellyThingCreator.getThingUID(thingTypeId + "-" + DEVICE_ID);
            ThingUID expectedThingUid = new ThingUID(BINDING_ID, thingTypeId, DEVICE_ID);
            assertThat(actualThingUid, is(equalTo(expectedThingUid)));
        }
    }
}
