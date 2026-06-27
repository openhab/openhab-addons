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
package org.openhab.binding.rachio.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * Tests command and state conversions for typed Quantity channels.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioQuantityTypesTest {
    @Test
    void durationCommandsAcceptPlainNumbersAndQuantityTime() {
        assertThat(RachioQuantityTypes.durationSeconds(new DecimalType(30)).orElseThrow(), is(30));
        assertThat(RachioQuantityTypes.durationSeconds(QuantityType.valueOf("30 s")).orElseThrow(), is(30));
        assertThat(RachioQuantityTypes.durationSeconds(QuantityType.valueOf("2 min")).orElseThrow(), is(120));
    }

    @Test
    void durationCommandsRejectIncompatibleQuantity() {
        assertThat(RachioQuantityTypes.durationSeconds(QuantityType.valueOf("12 mm")).isEmpty(), is(true));
    }

    @Test
    void moistureLevelCommandsPreservePlainMillimeterSemanticsAndAcceptLengthQuantities() {
        assertThat(RachioQuantityTypes.lengthMillimeters(new DecimalType("12.5")).orElseThrow(), closeTo(12.5, 0.001));
        assertThat(RachioQuantityTypes.lengthMillimeters(QuantityType.valueOf("1 in")).orElseThrow(),
                closeTo(25.4, 0.001));
    }

    @Test
    void dimensionlessCommandsPreservePlainFractionSemanticsAndAcceptPercentQuantities() {
        assertThat(RachioQuantityTypes.dimensionless(new DecimalType("0.5")).orElseThrow(), closeTo(0.5, 0.001));
        assertThat(RachioQuantityTypes.dimensionless(QuantityType.valueOf("50 %")).orElseThrow(), closeTo(0.5, 0.001));
    }

    @Test
    void typedStateHelpersPublishExpectedUnits() {
        assertQuantityUnit(RachioQuantityTypes.seconds(30), Units.SECOND);
        assertQuantityUnit(RachioQuantityTypes.days(3), Units.DAY);
        assertQuantityUnit(RachioQuantityTypes.fractionOrUndef(0.7), Units.ONE);
        assertQuantityUnit(RachioQuantityTypes.temperatureOrUndef(72, "US"), ImperialUnits.FAHRENHEIT);
        assertQuantityUnit(RachioQuantityTypes.windSpeedOrUndef(5, "US"), ImperialUnits.MILES_PER_HOUR);
        assertQuantityUnit(RachioQuantityTypes.precipitationOrUndef(5, "METRIC"), MetricPrefix.MILLI(SIUnits.METRE));
        assertQuantityUnit(RachioQuantityTypes.precipitationOrUndef(0.25, "US"), ImperialUnits.INCH);
    }

    private void assertQuantityUnit(State state, Object unit) {
        assertThat(state, instanceOf(QuantityType.class));
        QuantityType<?> quantity = (QuantityType<?>) state;
        assertThat(quantity.getUnit(), is(unit));
    }
}
