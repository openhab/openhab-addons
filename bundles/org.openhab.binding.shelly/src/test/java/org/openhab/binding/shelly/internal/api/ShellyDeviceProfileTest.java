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
package org.openhab.binding.shelly.internal.api;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.*;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link ShellyDeviceProfile}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ShellyDeviceProfileTest {

    @ParameterizedTest
    @MethodSource("provideTestCasesForApiDetermination")
    void determineApi(String thingTypeId, boolean expectedIsGeneration2, boolean expectedIsBlu) {
        boolean actualIsGeneration2 = ShellyDeviceProfile.isGeneration2(thingTypeId);
        assertThat(actualIsGeneration2, is(equalTo(expectedIsGeneration2)));

        boolean actualIsBlue = ShellyDeviceProfile.isBluSeries(thingTypeId);
        assertThat(actualIsBlue, is(equalTo(expectedIsBlu)));
    }

    private static Stream<Arguments> provideTestCasesForApiDetermination() {
        return Stream.of( //
                // Shelly BLU
                Arguments.of(THING_TYPE_SHELLYBLUBUTTON_STR, true, true), //
                Arguments.of(THING_TYPE_SHELLYBLUDW_STR, true, true), //
                Arguments.of(THING_TYPE_SHELLYBLUMOTION_STR, true, true), //
                Arguments.of(THING_TYPE_SHELLYBLUHT_STR, true, true), //
                Arguments.of(THING_TYPE_SHELLYBLUGW_STR, false, false), //
                // Shelly Bulb
                Arguments.of(THING_TYPE_SHELLYBULB_STR, false, false), //
                // Generation 1
                Arguments.of(THING_TYPE_SHELLYDUO_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYDUORGBW_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYVINTAGE_STR, false, false), //
                Arguments.of("shellyrgbw2-color", false, false), //
                Arguments.of("shellyrgbw2-white", false, false), //
                Arguments.of(THING_TYPE_SHELLY1_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLY1L_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLY1PM_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYEM_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLY3EM_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLY2_RELAY_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLY2_ROLLER_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLY25_RELAY_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLY25_ROLLER_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLY4PRO_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYPLUG_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYPLUGS_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYPLUGU1_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYUNI_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYDIMMER_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYDIMMER2_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYIX3_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYHT_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYSMOKE_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYGAS_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYFLOOD_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYDOORWIN_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYDOORWIN2_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYSENSE_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYBUTTON1_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYBUTTON2_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYMOTION_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYTRV_STR, false, false), //
                // Generation 2 Plus series
                Arguments.of(THING_TYPE_SHELLYPLUS1_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUS1PM_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUS2PM_RELAY_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUS2PM_ROLLER_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSPLUGS_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSI4_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSI4DC_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLY1MINI_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPMMINI_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLY1PMMINI_STR, true, false), //
                // Generation 2 Pro series
                Arguments.of(THING_TYPE_SHELLYPRO1_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO1PM_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO2_RELAY_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO2PM_RELAY_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO2PM_ROLLER_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO3_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO3EM_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPROEM50_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPRO4PM_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSDIMMERUS_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSDIMMER10V_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSHTG3_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSSMOKE_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPLUSWALLDISPLAY_STR, true, false), //
                Arguments.of(THING_TYPE_SHELLYPROTECTED_STR, false, false), //
                Arguments.of(THING_TYPE_SHELLYUNKNOWN_STR, false, false));
    }
}
