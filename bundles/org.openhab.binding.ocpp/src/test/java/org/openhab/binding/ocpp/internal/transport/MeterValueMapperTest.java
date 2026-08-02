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
package org.openhab.binding.ocpp.internal.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.ZonedDateTime;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

import eu.chargetime.ocpp.model.core.MeterValue;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.SampledValue;

/**
 * Tests the measurand/phase to channel routing and the unit parsing of {@link MeterValueMapper}.
 *
 * <p>
 * The unit-tolerance cases (unknown / mis-spelled units) go through the String-based
 * {@code toState} directly: the ChargeTime {@code SampledValue.setUnit} validates and would reject
 * exactly those wire values, whereas a real charger reaches them via gson field deserialization that
 * bypasses the setter.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class MeterValueMapperTest {

    @Test
    void routesCurrentImportToTheReportedPhase() {
        assertEquals("current-import-l1", MeterValueMapper.channelFor("Current.Import", "L1"));
        assertEquals("current-import-l2", MeterValueMapper.channelFor("Current.Import", "L2"));
        assertEquals("current-import-l3", MeterValueMapper.channelFor("Current.Import", "L3"));
        // Phase-to-neutral variants still resolve to the base phase channel.
        assertEquals("current-import-l2", MeterValueMapper.channelFor("Current.Import", "L2-N"));
        // No phase maps to the aggregate channel, not a phase.
        assertEquals("current-import", MeterValueMapper.channelFor("Current.Import", null));
        assertEquals("voltage", MeterValueMapper.channelFor("Voltage", null));
    }

    @Test
    void routesTheOtherModelledMeasurands() {
        assertEquals("voltage-l3", MeterValueMapper.channelFor("Voltage", "L3"));
        assertEquals("current-offered", MeterValueMapper.channelFor("Current.Offered", null));
        assertEquals("current-export", MeterValueMapper.channelFor("Current.Export", null));
        assertEquals("power-active-import", MeterValueMapper.channelFor("Power.Active.Import", null));
        assertEquals("power-active-export", MeterValueMapper.channelFor("Power.Active.Export", null));
        assertEquals("power-reactive-import", MeterValueMapper.channelFor("Power.Reactive.Import", null));
        assertEquals("power-factor", MeterValueMapper.channelFor("Power.Factor", null));
        assertEquals("power-offered", MeterValueMapper.channelFor("Power.Offered", null));
        assertEquals("energy-active-import", MeterValueMapper.channelFor("Energy.Active.Import.Register", null));
        assertEquals("energy-active-export", MeterValueMapper.channelFor("Energy.Active.Export.Register", null));
        assertEquals("energy-reactive-import", MeterValueMapper.channelFor("Energy.Reactive.Import.Register", null));
        assertEquals("energy-active-import-interval",
                MeterValueMapper.channelFor("Energy.Active.Import.Interval", null));
        assertEquals("frequency", MeterValueMapper.channelFor("Frequency", null));
        assertEquals("soc", MeterValueMapper.channelFor("SoC", null));
        assertEquals("temperature", MeterValueMapper.channelFor("Temperature", null));
        assertEquals("rpm", MeterValueMapper.channelFor("RPM", null));
        assertNull(MeterValueMapper.channelFor("Nonsense.Measurand", null));
    }

    @Test
    void omittedMeasurandDefaultsToEnergyRegister() {
        assertEquals("Energy.Active.Import.Register", MeterValueMapper.measurandOf(new SampledValue("0")));
    }

    @Test
    void parsesKnownUnitsAsQuantities() {
        QuantityType<?> current = assertInstanceOf(QuantityType.class, MeterValueMapper.toState("16.0", "A"));
        assertEquals(Units.AMPERE, current.getUnit());
        assertEquals(16.0, current.doubleValue());

        assertEquals(Units.VOLT, assertInstanceOf(QuantityType.class, MeterValueMapper.toState("230", "V")).getUnit());
        assertEquals(Units.WATT, assertInstanceOf(QuantityType.class, MeterValueMapper.toState("3600", "W")).getUnit());
        assertEquals(Units.KILOWATT_HOUR,
                assertInstanceOf(QuantityType.class, MeterValueMapper.toState("12.5", "kWh")).getUnit());
    }

    @Test
    void reactiveAndApparentUnitsKeepTheirDimension() {
        // These are routed to dimensioned energy/power channels, so they must carry a unit, not
        // degrade to a bare number.
        assertEquals(Units.VAR_HOUR,
                assertInstanceOf(QuantityType.class, MeterValueMapper.toState("42", "varh")).getUnit());
        assertEquals(Units.KILOVAR,
                assertInstanceOf(QuantityType.class, MeterValueMapper.toState("3", "kvar")).getUnit());
        assertEquals(Units.KILOVOLT_AMPERE,
                assertInstanceOf(QuantityType.class, MeterValueMapper.toState("7", "kVA")).getUnit());
    }

    @Test
    void omittedUnitDefaultsToWattHoursForEnergyMeasurands() {
        // OCPP 1.6 defines Wh as the default SampledValue unit — meaningful exactly for the energy
        // measurands (the default measurand is the energy register). A unitless Power.Factor must NOT
        // be stamped as Wh: that would fabricate a wrong dimension.
        assertEquals(Units.WATT_HOUR, assertInstanceOf(QuantityType.class,
                MeterValueMapper.toState("42", null, "Energy.Active.Import.Register")).getUnit());
        assertInstanceOf(DecimalType.class, MeterValueMapper.toState("0.98", null, "Power.Factor"));
    }

    @Test
    void aTrulyUnknownUnitDegradesToDecimalRatherThanThrowing() {
        // Not an OCPP unit at all — keep the number rather than dropping the sample or throwing.
        assertInstanceOf(DecimalType.class, MeterValueMapper.toState("42", "furlong"));
    }

    @Test
    void toleratesTheSpecMisspelledCelsius() {
        // 'Celcius' is the OCPP spec's own mis-spelling — mapped to Celsius, not rejected.
        assertInstanceOf(QuantityType.class, MeterValueMapper.toState("21.5", "Celcius"));
    }

    @Test
    void unparseableOrEmptyValueIsDropped() {
        assertNull(MeterValueMapper.toState("not-a-number", "A"));
        assertNull(MeterValueMapper.toState("", "A"));
        assertNull(MeterValueMapper.toState(null, "A"));
    }

    @Test
    void nonFiniteValuesAreDroppedNotFatal() {
        // NaN / Infinity parse as valid doubles but crash QuantityType/BigDecimal — must be dropped.
        assertNull(MeterValueMapper.toState("NaN", "Wh"));
        assertNull(MeterValueMapper.toState("NaN", "A"));
        assertNull(MeterValueMapper.toState("NaN", null));
        assertNull(MeterValueMapper.toState("Infinity", "V"));
        assertNull(MeterValueMapper.toState("-Infinity", "W"));
    }

    private static SampledValue sample(String measurand, @org.eclipse.jdt.annotation.Nullable String phase, String unit,
            String value) {
        SampledValue sampledValue = new SampledValue(value);
        sampledValue.setMeasurand(measurand);
        if (phase != null) {
            sampledValue.setPhase(phase);
        }
        sampledValue.setUnit(unit);
        return sampledValue;
    }

    private static MeterValuesRequest requestOf(SampledValue... samples) {
        MeterValuesRequest request = new MeterValuesRequest(1);
        request.setMeterValue(new MeterValue[] { new MeterValue(ZonedDateTime.now(), samples) });
        return request;
    }

    @Test
    void phasedPowerSamplesSumIntoTheAggregateChannel() {
        // Three per-phase Power.Active.Import samples are the phase contributions of ONE total; the
        // aggregate channel must carry their sum, not whichever phase happened to be listed last.
        Map<String, State> states = MeterValueMapper.toStates(requestOf(
                sample("Power.Active.Import", "L1", "W", "1000"), sample("Power.Active.Import", "L2", "W", "1100"),
                sample("Power.Active.Import", "L3", "W", "1200")));

        QuantityType<?> total = assertInstanceOf(QuantityType.class, states.get("power-active-import"));
        assertEquals(3300.0, total.doubleValue());
    }

    @Test
    void aChargerReportedTotalWinsOverThePhaseSum() {
        // If the charger reports its own unphased total alongside the phases, that total is
        // authoritative — the phases must not be double-counted on top of it.
        Map<String, State> states = MeterValueMapper.toStates(requestOf(
                sample("Power.Active.Import", "L1", "W", "1000"), sample("Power.Active.Import", "L2", "W", "1100"),
                sample("Power.Active.Import", "L3", "W", "1200"), sample("Power.Active.Import", null, "W", "3300")));

        assertEquals(3300.0, assertInstanceOf(QuantityType.class, states.get("power-active-import")).doubleValue());
    }

    @Test
    void phasedCurrentSamplesAreNotSummedIntoATotal() {
        // A charger offering 16 A on each of three conductors offers 16 A per phase — not 48 A.
        // Currents have no meaningful phase sum, so these samples are skipped, not aggregated.
        Map<String, State> states = MeterValueMapper.toStates(requestOf(sample("Current.Offered", "L1", "A", "16"),
                sample("Current.Offered", "L2", "A", "16"), sample("Current.Offered", "L3", "A", "16")));

        org.junit.jupiter.api.Assertions.assertFalse(states.containsKey("current-offered"),
                "phased currents must not be summed into the aggregate channel");
    }

    @Test
    void lineToLineVoltageDoesNotMasqueradeAsAPhaseVoltage() {
        // L1-N is conductor L1's voltage; L1-L2 is a line-to-line measurement with no per-phase
        // channel — it must be ignored, not published into (and overwrite) the L1 channel.
        Map<String, State> states = MeterValueMapper
                .toStates(requestOf(sample("Voltage", "L1-N", "V", "230"), sample("Voltage", "L1-L2", "V", "400")));

        assertEquals(230.0, assertInstanceOf(QuantityType.class, states.get("voltage-l1")).doubleValue());
        assertEquals(1, states.size(), "the line-to-line sample must not appear anywhere");
    }

    @Test
    void aPhasedPowerFactorIsNotPassedOffAsAnAggregate() {
        // Per-phase power factors have no meaningful sum or single representative value; publishing
        // one phase as "the" power factor would be silently wrong, so the sample is skipped.
        Map<String, State> states = MeterValueMapper.toStates(requestOf(sample("Power.Factor", "L1", "Percent", "98")));

        org.junit.jupiter.api.Assertions.assertFalse(states.containsKey("power-factor"),
                "a phased power factor must not populate the aggregate channel");
    }

    @Test
    void flattensAMeterValuesRequestToChannelStates() {
        SampledValue l1 = new SampledValue("14.2");
        l1.setMeasurand("Current.Import");
        l1.setPhase("L1");
        l1.setUnit("A");

        SampledValue voltage = new SampledValue("231.5");
        voltage.setMeasurand("Voltage");
        voltage.setPhase("L1");
        voltage.setUnit("V");

        MeterValuesRequest request = new MeterValuesRequest(1);
        request.setMeterValue(
                new MeterValue[] { new MeterValue(ZonedDateTime.now(), new SampledValue[] { l1, voltage }) });

        Map<String, State> states = MeterValueMapper.toStates(request);

        assertEquals(2, states.size());
        assertEquals(Units.AMPERE, assertInstanceOf(QuantityType.class, states.get("current-import-l1")).getUnit());
        assertEquals(Units.VOLT, assertInstanceOf(QuantityType.class, states.get("voltage-l1")).getUnit());
    }
}
