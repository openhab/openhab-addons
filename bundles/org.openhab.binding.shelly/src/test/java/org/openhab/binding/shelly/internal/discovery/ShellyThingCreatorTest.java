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
package org.openhab.binding.shelly.internal.discovery;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.*;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.ShellyBindingConstants;
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
            ShellyThingCreator.getThingUID(serviceName, "", "", false);
        });
    }

    private static Stream<Arguments> provideTestCasesForGetThingUIDThrowsForInvalidServiceName() {
        return Stream.of(Arguments.of(""), Arguments.of("-", Arguments.of("foo")));
    }

    @Test
    void getThingUIDReturnsThingUidForUnknown() {
        ThingUID actual = ShellyThingCreator.getThingUID("johndoe-" + DEVICE_ID, "", "", true);
        ThingUID expected = new ThingUID(ShellyBindingConstants.BINDING_ID, THING_TYPE_SHELLYPROTECTED_STR, DEVICE_ID);

        assertThat(actual, is(equalTo(expected)));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForGetThingUIDReturnsThingUidAccordingToRuleset")
    void getThingUIDReturnsThingUidAccordingToRuleset(String serviceName, String deviceType, String mode,
            String expectedThingTypeId) {
        ThingUID actual = ShellyThingCreator.getThingUID(serviceName, deviceType, mode, false);
        ThingUID expected = new ThingUID(ShellyBindingConstants.BINDING_ID, expectedThingTypeId, DEVICE_ID);
        ThingTypeUID expectedThingTypeUid = new ThingTypeUID(ShellyBindingConstants.BINDING_ID, expectedThingTypeId);

        assertThat("serviceName: " + serviceName + "; deviceType: " + deviceType + "; mode: " + mode, actual,
                is(equalTo(expected)));
        assertThat(ShellyBindingConstants.SUPPORTED_THING_TYPES_UIDS, hasItem(expectedThingTypeUid));
    }

    private static Stream<Arguments> provideTestCasesForGetThingUIDReturnsThingUidAccordingToRuleset() {
        return Stream.of( //
                Arguments.of("johndoe-" + DEVICE_ID, "", "", "shellyunknown"), //
                Arguments.of("shellyswitch25-" + DEVICE_ID, "", "relay", THING_TYPE_SHELLY25_RELAY_STR), //
                Arguments.of("shellyswitch25xx-" + DEVICE_ID, "", "relay", THING_TYPE_SHELLY25_RELAY_STR), //
                Arguments.of("shellyswitch25xx-" + DEVICE_ID, "", "", THING_TYPE_SHELLY25_ROLLER_STR), //
                Arguments.of("shellyswitch25xx-" + DEVICE_ID, "", "relax", THING_TYPE_SHELLY25_ROLLER_STR), //
                Arguments.of("shellyswitch26xx-" + DEVICE_ID, "", "relay", THING_TYPE_SHELLY2_RELAY_STR), //
                Arguments.of("shellyswitch-" + DEVICE_ID, "", "relay", THING_TYPE_SHELLY2_RELAY_STR), //
                Arguments.of("shellyswitch-" + DEVICE_ID, "", "", THING_TYPE_SHELLY2_ROLLER_STR), //
                Arguments.of("shellyplug-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUG_STR), //
                Arguments.of("shellyplug-u1-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUG_STR), //
                Arguments.of("shellyplugs-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUGS_STR), //
                Arguments.of("shellyplug-s-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUGS_STR), //
                Arguments.of("shellyplug-su1-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUGS_STR), //
                Arguments.of("shellyplugu1-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUGU1_STR), //
                Arguments.of("shellyplugu12-" + DEVICE_ID, "", "", THING_TYPE_SHELLYPLUGU1_STR), //
                Arguments.of("shellyrgbw2-" + DEVICE_ID, "", "color", THING_TYPE_SHELLYRGBW2_COLOR_STR), //
                Arguments.of("shellyrgbw2-" + DEVICE_ID, "", "", THING_TYPE_SHELLYRGBW2_WHITE_STR), //
                Arguments.of("shellyrgbw2-" + DEVICE_ID, "", "colour", THING_TYPE_SHELLYRGBW2_WHITE_STR), //
                Arguments.of("shellymotion-" + DEVICE_ID, "", "", THING_TYPE_SHELLYMOTION_STR), //
                Arguments.of("shellymotionsensor-" + DEVICE_ID, "", "", THING_TYPE_SHELLYMOTION_STR));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForGetThingUIDReturnsThingUidByDeviceType")
    void getThingUIDReturnsThingUidByDeviceType(String deviceType, String mode, String expectedThingTypeId) {
        ThingUID actual = ShellyThingCreator.getThingUID("x-" + DEVICE_ID, deviceType, mode, false);
        ThingUID expected = new ThingUID(ShellyBindingConstants.BINDING_ID, expectedThingTypeId, DEVICE_ID);
        ThingTypeUID expectedThingTypeUid = new ThingTypeUID(ShellyBindingConstants.BINDING_ID, expectedThingTypeId);

        assertThat("deviceType: " + deviceType + "; mode: " + mode, actual, is(equalTo(expected)));
        assertThat(ShellyBindingConstants.SUPPORTED_THING_TYPES_UIDS, hasItem(expectedThingTypeUid));
    }

    private static Stream<Arguments> provideTestCasesForGetThingUIDReturnsThingUidByDeviceType() {
        return Stream.of( //
                Arguments.of(SHELLYDT_1PM, "", THING_TYPE_SHELLY1PM_STR), //
                Arguments.of(SHELLYDT_1L, "", THING_TYPE_SHELLY1L_STR), //
                Arguments.of(SHELLYDT_1, "", THING_TYPE_SHELLY1_STR), //
                Arguments.of(SHELLYDT_SHPRO, "", THING_TYPE_SHELLY4PRO_STR), //
                Arguments.of(SHELLYDT_4PRO, "", THING_TYPE_SHELLY4PRO_STR), //
                Arguments.of(SHELLYDT_3EM, "", THING_TYPE_SHELLY3EM_STR), //
                Arguments.of(SHELLYDT_EM, "", THING_TYPE_SHELLYEM_STR), //
                Arguments.of(SHELLYDT_SHPLG_S, "", THING_TYPE_SHELLYPLUGS_STR), //
                Arguments.of(SHELLYDT_SHPLG_U1, "", THING_TYPE_SHELLYPLUGU1_STR), //
                Arguments.of(SHELLYDT_GAS, "", THING_TYPE_SHELLYGAS_STR), //
                Arguments.of(SHELLYDT_DW, "", THING_TYPE_SHELLYDOORWIN_STR), //
                Arguments.of(SHELLYDT_DW2, "", THING_TYPE_SHELLYDOORWIN2_STR), //
                Arguments.of(SHELLYDT_DUO, "", THING_TYPE_SHELLYDUO_STR), //
                Arguments.of(SHELLYDT_DUORGBW, "", THING_TYPE_SHELLYDUORGBW_STR), //
                Arguments.of(SHELLYDT_BULB, "", THING_TYPE_SHELLYBULB_STR), //
                Arguments.of(SHELLYDT_VINTAGE, "", THING_TYPE_SHELLYVINTAGE_STR), //
                Arguments.of(SHELLYDT_DIMMER, "", THING_TYPE_SHELLYDIMMER_STR), //
                Arguments.of(SHELLYDT_DIMMER2, "", THING_TYPE_SHELLYDIMMER2_STR), //
                Arguments.of(SHELLYDT_IX3, "", THING_TYPE_SHELLYIX3_STR), //
                Arguments.of(SHELLYDT_BUTTON1, "", THING_TYPE_SHELLYBUTTON1_STR), //
                Arguments.of(SHELLYDT_BUTTON2, "", THING_TYPE_SHELLYBUTTON2_STR), //
                Arguments.of(SHELLYDT_UNI, "", THING_TYPE_SHELLYUNI_STR), //
                Arguments.of(SHELLYDT_HT, "", THING_TYPE_SHELLYHT_STR), //
                Arguments.of(SHELLYDT_TRV, "", THING_TYPE_SHELLYTRV_STR), //
                Arguments.of(SHELLYDT_MOTION, "", THING_TYPE_SHELLYMOTION_STR), //
                // Plus Series
                Arguments.of(SHELLYDT_PLUS1, "", THING_TYPE_SHELLYPLUS1_STR), //
                Arguments.of(SHELLYDT_PLUS1PM, "", THING_TYPE_SHELLYPLUS1PM_STR), //
                Arguments.of(SHELLYDT_PLUS1UL, "", THING_TYPE_SHELLYPLUS1_STR), //
                Arguments.of(SHELLYDT_PLUS1PMUL, "", THING_TYPE_SHELLYPLUS1PM_STR), //
                Arguments.of("SNSW-002P16EU", "relay", THING_TYPE_SHELLYPLUS2PM_RELAY_STR), //
                Arguments.of("SNSW-002P16EU", "roller", THING_TYPE_SHELLYPLUS2PM_ROLLER_STR), //
                Arguments.of("SNSW-102P16EU", "relay", THING_TYPE_SHELLYPLUS2PM_RELAY_STR), //
                Arguments.of("SNSW-102P16EU", "roller", THING_TYPE_SHELLYPLUS2PM_ROLLER_STR), //
                Arguments.of(SHELLYDT_PLUSPLUGS, "", THING_TYPE_SHELLYPLUSPLUGS_STR), //
                Arguments.of(SHELLYDT_PLUSPLUGIT, "", THING_TYPE_SHELLYPLUSPLUGS_STR), //
                Arguments.of(SHELLYDT_PLUSPLUGUK, "", THING_TYPE_SHELLYPLUSPLUGS_STR), //
                Arguments.of(SHELLYDT_PLUSPLUGUS, "", THING_TYPE_SHELLYPLUSPLUGUS_STR), //
                Arguments.of(SHELLYDT_PLUSI4DC, "", THING_TYPE_SHELLYPLUSI4DC_STR), //
                Arguments.of(SHELLYDT_PLUSI4, "", THING_TYPE_SHELLYPLUSI4_STR), //
                Arguments.of(SHELLYDT_PLUSHT, "", THING_TYPE_SHELLYPLUSHT_STR), //
                Arguments.of(SHELLYDT_PLUSHTG3, "", THING_TYPE_SHELLYPLUSHTG3_STR), //
                Arguments.of(SHELLYDT_PLUSSMOKE, "", THING_TYPE_SHELLYPLUSSMOKE_STR), //
                Arguments.of(SHELLYDT_PLUSUNI, "", THING_TYPE_SHELLYUNI_STR), //
                Arguments.of(SHELLYDT_PLUSDIMMERUS, "", THING_TYPE_SHELLYPLUSDIMMERUS_STR), //
                Arguments.of(SHELLYDT_PLUSDIMMER10V, "", THING_TYPE_SHELLYPLUSDIMMER10V_STR), //
                // Plus Mini Series
                Arguments.of(SHELLYDT_MINI1, "", THING_TYPE_SHELLY1MINI_STR), //
                Arguments.of(SHELLYDT_MINIPM, "", THING_TYPE_SHELLYPMMINI_STR), //
                Arguments.of(SHELLYDT_MINI1PM, "", THING_TYPE_SHELLY1PMMINI_STR), //
                Arguments.of(SHELLYDT_MINI1G3_1, "", THING_TYPE_SHELLY1MINI_STR), //
                Arguments.of(SHELLYDT_MINIG3_PM, "", THING_TYPE_SHELLYPMMINI_STR), //
                Arguments.of(SHELLYDT_MINIG3_1PM, "", THING_TYPE_SHELLY1PMMINI_STR), //
                // Pro Series
                Arguments.of(SHELLYDT_PRO1, "", THING_TYPE_SHELLYPRO1_STR), //
                Arguments.of(SHELLYDT_PRO1_2, "", THING_TYPE_SHELLYPRO1_STR), //
                Arguments.of(SHELLYDT_PRO1_3, "", THING_TYPE_SHELLYPRO1_STR), //
                Arguments.of(SHELLYDT_PRO1PM, "", THING_TYPE_SHELLYPRO1PM_STR), //
                Arguments.of(SHELLYDT_PRO1PM_2, "", THING_TYPE_SHELLYPRO1PM_STR), //
                Arguments.of(SHELLYDT_PRO1PM_3, "", THING_TYPE_SHELLYPRO1PM_STR), //
                Arguments.of("SPSW-002XE16EU", "relay", THING_TYPE_SHELLYPRO2_RELAY_STR), //
                Arguments.of("SPSW-102XE16EU", "relay", THING_TYPE_SHELLYPRO2_RELAY_STR), //
                Arguments.of("SPSW-202XE16EU", "relay", THING_TYPE_SHELLYPRO2_RELAY_STR), //
                Arguments.of("SPSW-002PE16EU", "relay", THING_TYPE_SHELLYPRO2PM_RELAY_STR), //
                Arguments.of("SPSW-102PE16EU", "relay", THING_TYPE_SHELLYPRO2PM_RELAY_STR), //
                Arguments.of("SPSW-202PE16EU", "relay", THING_TYPE_SHELLYPRO2PM_RELAY_STR), //
                Arguments.of("SPSW-002PE16EU", "roller", THING_TYPE_SHELLYPRO2PM_ROLLER_STR), //
                Arguments.of("SPSW-102PE16EU", "roller", THING_TYPE_SHELLYPRO2PM_ROLLER_STR), //
                Arguments.of("SPSW-202PE16EU", "roller", THING_TYPE_SHELLYPRO2PM_ROLLER_STR), //
                Arguments.of(SHELLYDT_PRO3, "", THING_TYPE_SHELLYPRO3_STR), //
                Arguments.of(SHELLYDT_PROEM50, "", THING_TYPE_SHELLYPROEM50_STR), //
                Arguments.of(SHELLYDT_PRO3EM, "", THING_TYPE_SHELLYPRO3EM_STR), //
                Arguments.of(SHELLYDT_PRO4PM, "", THING_TYPE_SHELLYPRO4PM_STR), //
                Arguments.of(SHELLYDT_PRO4PM_2, "", THING_TYPE_SHELLYPRO4PM_STR), //
                // BLU Series
                Arguments.of(SHELLYDT_BLUBUTTON, "", THING_TYPE_SHELLYBLUBUTTON_STR), //
                Arguments.of(SHELLYDT_BLUDW, "", THING_TYPE_SHELLYBLUDW_STR), //
                Arguments.of(SHELLYDT_BLUMOTION, "", THING_TYPE_SHELLYBLUMOTION_STR), //
                Arguments.of(SHELLYDT_BLUHT, "", THING_TYPE_SHELLYBLUHT_STR), //
                Arguments.of(SHELLYDT_BLUGW, "", THING_TYPE_SHELLYBLUGW_STR), //
                // Wall displays
                Arguments.of(SHELLYDT_PLUSWALLDISPLAY, "", THING_TYPE_SHELLYPLUSWALLDISPLAY_STR));
    }

    @Test
    void getThingUIDReturnsThingTypeMatchingServiceName() {
        Set<ThingTypeUID> excludedThingTypeUids = Set.of(THING_TYPE_SHELLYBLUDW, THING_TYPE_SHELLYBLUMOTION,
                THING_TYPE_SHELLYBLUHT, THING_TYPE_SHELLYBLUGW, THING_TYPE_SHELLYBLUBUTTON, THING_TYPE_SHELLY2_RELAY,
                THING_TYPE_SHELLY2_ROLLER, THING_TYPE_SHELLY25_ROLLER, THING_TYPE_SHELLY25_RELAY,
                new ThingTypeUID(ShellyBindingConstants.BINDING_ID, THING_TYPE_SHELLYPLUSHTG3_STR),
                new ThingTypeUID(ShellyBindingConstants.BINDING_ID, THING_TYPE_SHELLYPLUS2PM_RELAY_STR),
                new ThingTypeUID(ShellyBindingConstants.BINDING_ID, THING_TYPE_SHELLYPLUS2PM_ROLLER_STR),
                new ThingTypeUID(ShellyBindingConstants.BINDING_ID, THING_TYPE_SHELLYPRO2_RELAY_STR),
                new ThingTypeUID(ShellyBindingConstants.BINDING_ID, THING_TYPE_SHELLYPRO2PM_ROLLER_STR),
                new ThingTypeUID(ShellyBindingConstants.BINDING_ID, THING_TYPE_SHELLYPRO2PM_RELAY_STR),
                new ThingTypeUID(ShellyBindingConstants.BINDING_ID, THING_TYPE_SHELLYRGBW2_COLOR_STR));

        for (ThingTypeUID supportedThingTypeUid : ShellyBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream()
                .filter(uid -> !excludedThingTypeUids.contains(uid)).toList()) {
            String thingTypeId = supportedThingTypeUid.getId();
            ThingUID actualThingUid = ShellyThingCreator.getThingUID(thingTypeId + "-" + DEVICE_ID, "", "", false);
            ThingUID expectedThingUid = new ThingUID(ShellyBindingConstants.BINDING_ID, thingTypeId, DEVICE_ID);
            assertThat(actualThingUid, is(equalTo(expectedThingUid)));
        }
    }
}
