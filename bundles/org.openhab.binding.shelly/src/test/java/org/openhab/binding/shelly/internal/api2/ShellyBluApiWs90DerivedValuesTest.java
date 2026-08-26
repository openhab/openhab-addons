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
package org.openhab.binding.shelly.internal.api2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.number.IsCloseTo.closeTo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ShellyBluApi}'s WS90 derived-value helpers: {@code windDirectionLabel},
 * {@code apparentTemperature} and {@code seaLevelPressure}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBluApiWs90DerivedValuesTest {

    private static final double DELTA = 0.05;

    @Test
    void windDirectionLabelMapsAllSixteenCompassPoints() {
        assertThat(ShellyBluApi.windDirectionLabel(0.0), is(equalTo("N")));
        assertThat(ShellyBluApi.windDirectionLabel(22.5), is(equalTo("NNE")));
        assertThat(ShellyBluApi.windDirectionLabel(45.0), is(equalTo("NE")));
        assertThat(ShellyBluApi.windDirectionLabel(90.0), is(equalTo("E")));
        assertThat(ShellyBluApi.windDirectionLabel(135.0), is(equalTo("SE")));
        assertThat(ShellyBluApi.windDirectionLabel(180.0), is(equalTo("S")));
        assertThat(ShellyBluApi.windDirectionLabel(270.0), is(equalTo("W")));
        assertThat(ShellyBluApi.windDirectionLabel(315.0), is(equalTo("NW")));
    }

    @Test
    void windDirectionLabelWrapsAroundThreeSixtyDegrees() {
        assertThat("358 degrees rounds up into the N sector", ShellyBluApi.windDirectionLabel(358.0), is(equalTo("N")));
        assertThat("360 degrees normalizes to N", ShellyBluApi.windDirectionLabel(360.0), is(equalTo("N")));
        assertThat("negative degrees normalize into range", ShellyBluApi.windDirectionLabel(-90.0), is(equalTo("W")));
    }

    @Test
    void apparentTemperatureLowersPerceivedTemperatureWhenColdAndWindy() {
        double result = ShellyBluApi.apparentTemperature(0.0, 50.0, 20.0);
        assertThat("wind lowers the perceived temperature below the actual air temperature", result,
                closeTo(-6.88, DELTA));
    }

    @Test
    void apparentTemperatureRaisesPerceivedTemperatureWhenHotAndHumid() {
        double result = ShellyBluApi.apparentTemperature(32.0, 70.0, 5.0);
        assertThat("humidity raises the perceived temperature above the actual air temperature", result,
                closeTo(37.97, DELTA));
    }

    @Test
    void apparentTemperatureStaysCloseToAirTemperatureForMildConditions() {
        double result = ShellyBluApi.apparentTemperature(20.0, 50.0, 10.0);
        assertThat("mild conditions stay close to the actual air temperature", result, closeTo(17.90, DELTA));
    }

    @Test
    void apparentTemperatureHasNoDiscontinuityAcrossFormerWindChillThreshold() {
        double justBelow = ShellyBluApi.apparentTemperature(9.9, 50.0, 5.0);
        double justAbove = ShellyBluApi.apparentTemperature(10.1, 50.0, 5.0);
        assertThat("crossing the former wind-chill threshold changes the result smoothly", justAbove - justBelow,
                closeTo(0.2, 0.5));
    }

    @Test
    void apparentTemperatureHasNoDiscontinuityAcrossFormerHeatIndexThreshold() {
        double justBelow = ShellyBluApi.apparentTemperature(26.9, 45.0, 3.0);
        double justAbove = ShellyBluApi.apparentTemperature(27.1, 45.0, 3.0);
        assertThat("crossing the former heat-index threshold changes the result smoothly", justAbove - justBelow,
                closeTo(0.2, 0.5));
    }

    @Test
    void seaLevelPressureReturnsStationPressureUnchangedAtZeroAltitude() {
        double result = ShellyBluApi.seaLevelPressure(1008.5, 18.0, 0);
        assertThat(result, is(equalTo(1008.5)));
    }

    @Test
    void seaLevelPressureIncreasesWithAltitude() {
        double result = ShellyBluApi.seaLevelPressure(950.0, 15.0, 500);
        assertThat("reducing a station reading to sea level increases the pressure", result, is(greaterThan(950.0)));
    }

    @Test
    void seaLevelPressureFallsBackToDefaultTemperatureWhenUnavailable() {
        double withTemp = ShellyBluApi.seaLevelPressure(950.0, 15.0, 500);
        double withoutTemp = ShellyBluApi.seaLevelPressure(950.0, null, 500);
        assertThat("missing temperature falls back to the 15C standard atmosphere default", withoutTemp,
                is(equalTo(withTemp)));
    }
}
