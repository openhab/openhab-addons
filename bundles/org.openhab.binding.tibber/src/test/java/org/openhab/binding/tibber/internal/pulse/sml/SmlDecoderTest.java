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
package org.openhab.binding.tibber.internal.pulse.sml;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibber.internal.pulse.sml.SmlFrameBuilder.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibber.internal.pulse.Fixtures;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Tests the scenarios of the {@code channels} spec, "SML frame validity", "SML entry decoding", and "Net power
 * derivation", at the level of the decoder.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class SmlDecoderTest {

    private static final byte[] VOLTAGE_L2 = obis(0x34, 0x07);
    private static final byte[] TARIFF_1 = { 0x01, 0x00, 0x01, 0x08, 0x01, (byte) 0xff };
    private static final byte[] ANGLE = { 0x01, 0x00, 0x51, 0x07, 0x01, (byte) 0xff };
    private static final byte[] VENDOR = { 0x01, 0x00, 0x60, 0x32, 0x01, 0x01 };

    private static @Nullable SmlReading single(byte[] payload) {
        List<SmlReading> readings = SmlDecoder.decode(payload, 0);
        assertTrue(readings.size() <= 1);
        return readings.isEmpty() ? null : readings.get(0);
    }

    private static SmlReading reading(SmlFrameBuilder builder) {
        SmlReading reading = single(builder.build());
        assertNotNull(reading);
        return reading;
    }

    private static void assertValue(String expected, Unit<?> unit, @Nullable QuantityType<?> actual) {
        assertNotNull(actual);
        assertEquals(unit, actual.getUnit());
        BigDecimal value = actual.toBigDecimal();
        assertEquals(0, new BigDecimal(expected).compareTo(value), "expected " + expected + " but was " + value);
    }

    @Test
    void whenObservedFrameIsDecodedThenAllChannelsCarryTheDocumentedValues() {
        SmlReading reading = single(Fixtures.observedFrame());

        assertNotNull(reading);
        Map<String, QuantityType<?>> v = reading.values();
        assertValue("1523", Units.WATT, v.get("power"));
        assertValue("136", Units.WATT, v.get("power-l1"));
        assertValue("1332", Units.WATT, v.get("power-l2"));
        assertValue("54", Units.WATT, v.get("power-l3"));
        assertValue("225.5", Units.VOLT, v.get("voltage-l1"));
        assertValue("225.6", Units.VOLT, v.get("voltage-l2"));
        assertValue("225.1", Units.VOLT, v.get("voltage-l3"));
        assertValue("0.62", Units.AMPERE, v.get("current-l1"));
        assertValue("6.55", Units.AMPERE, v.get("current-l2"));
        assertValue("0.71", Units.AMPERE, v.get("current-l3"));
        assertValue("49.9", Units.HERTZ, v.get("frequency"));
        assertValue("1234.5678", Units.KILOWATT_HOUR, v.get("energy-import"));
        assertValue("0", Units.KILOWATT_HOUR, v.get("energy-export"));
        assertEquals(13, v.size(), "the angle entries and firmware strings are not channels");
        assertEquals(Fixtures.METER_ID, reading.meterId());
    }

    @Test
    void whenFrameIsPrefixedByAHeaderThenTheOffsetSelectsThePayload() {
        byte[] message = SmlFrameBuilder.concat(new byte[37], Fixtures.observedFrame());

        assertEquals(1, SmlDecoder.decode(message, 37).size());
    }

    @Test
    void whenEnergyHasScalerMinusOneThenItIsExactInKilowattHours() {
        SmlReading reading = reading(
                new SmlFrameBuilder().unsigned(ENERGY_IMPORT, UNIT_WH, -1, BigInteger.valueOf(12345678), 8));

        QuantityType<?> energy = reading.values().get("energy-import");
        assertValue("1234.5678", Units.KILOWATT_HOUR, energy);
        assertNotNull(energy);
        assertEquals("1234.5678", energy.toBigDecimal().toPlainString());
    }

    @Test
    void whenPowerIsNegativeSigned64BitThenItIsNegative() {
        SmlReading reading = reading(new SmlFrameBuilder().signed(POWER, UNIT_W, 0, -2300, 8));

        assertValue("-2300", Units.WATT, reading.values().get("power"));
    }

    @Test
    void whenUnsignedCounterHasTheHighBitSetThenItStaysPositive() {
        BigInteger max = new BigInteger("18446744073709551615");
        SmlReading reading = reading(new SmlFrameBuilder().unsigned(ENERGY_IMPORT, UNIT_WH, 0, max, 8));

        QuantityType<?> energy = reading.values().get("energy-import");
        assertNotNull(energy);
        assertEquals(1, energy.toBigDecimal().signum());
        assertEquals("18446744073709551.615", energy.toBigDecimal().toPlainString());
    }

    @Test
    void whenSigned64BitValueIsTheMinimumThenItIsNotOverflowed() {
        SmlReading reading = reading(new SmlFrameBuilder().signed(POWER, UNIT_W, 0, Long.MIN_VALUE, 8));

        QuantityType<?> power = reading.values().get("power");
        assertNotNull(power);
        assertEquals("-9223372036854775808", power.toBigDecimal().toPlainString());
    }

    @Test
    void whenScalerIsPositiveThenTheValueIsMultiplied() {
        SmlReading reading = reading(new SmlFrameBuilder().signed(POWER, UNIT_W, 2, 12, 2));

        QuantityType<?> power = reading.values().get("power");
        assertValue("1200", Units.WATT, power);
        assertEquals("1200 W", String.valueOf(power), "no exponent notation in the state string");
    }

    @Test
    void whenScalerIsAbsentThenItIsZero() {
        SmlReading reading = reading(new SmlFrameBuilder().signed(POWER, UNIT_W, null, 77, 2));

        assertValue("77", Units.WATT, reading.values().get("power"));
    }

    @Test
    void whenScalerIsOutsideMinusEighteenToEighteenThenOnlyThatEntryIsSkipped() {
        SmlReading reading = reading(new SmlFrameBuilder().signed(POWER, UNIT_W, 19, 1, 1)
                .signed(FREQUENCY, UNIT_HZ, -1, 500, 2).signed(ENERGY_IMPORT, UNIT_WH, -19, 1, 1));

        assertEquals(Set.of("frequency"), reading.values().keySet());
    }

    @Test
    void whenIntegerWidthsVaryThenAllAreDecoded() {
        for (int width : new int[] { 1, 2, 4, 8 }) {
            SmlReading signed = reading(new SmlFrameBuilder().signed(POWER, UNIT_W, 0, -100, width));
            SmlReading unsigned = reading(
                    new SmlFrameBuilder().unsigned(POWER, UNIT_W, 0, BigInteger.valueOf(100), width));

            assertValue("-100", Units.WATT, signed.values().get("power"));
            assertValue("100", Units.WATT, unsigned.values().get("power"));
        }
    }

    @Test
    void whenUnsignedByteHasTheHighBitSetThenItIsNotNegative() {
        SmlReading reading = reading(new SmlFrameBuilder().unsigned(POWER, UNIT_W, 0, BigInteger.valueOf(200), 1));

        assertValue("200", Units.WATT, reading.values().get("power"));
    }

    @Test
    void whenUnitCodeIsUnexpectedThenOnlyThatEntryIsSkipped() {
        SmlReading reading = reading(
                new SmlFrameBuilder().signed(VOLTAGE_L1, UNIT_A, -1, 2255, 2).signed(POWER, UNIT_W, 0, 900, 2));

        assertNull(reading.values().get("voltage-l1"));
        assertValue("900", Units.WATT, reading.values().get("power"));
    }

    @Test
    void whenUnitIsAbsentThenTheEntryIsSkipped() {
        SmlReading reading = reading(new SmlFrameBuilder()
                .entry(POWER, null, 0, integer(BigInteger.valueOf(5), 2, true)).signed(FREQUENCY, UNIT_HZ, -1, 500, 2));

        assertEquals(Set.of("frequency"), reading.values().keySet());
    }

    @Test
    void whenValueIsAStringForANumericObisThenOnlyThatEntryIsSkipped() {
        SmlReading reading = reading(new SmlFrameBuilder().entry(POWER, UNIT_W, 0, octetString(new byte[] { 1, 2 }))
                .signed(FREQUENCY, UNIT_HZ, -1, 500, 2));

        assertEquals(Set.of("frequency"), reading.values().keySet());
    }

    @Test
    void whenIntegerIsWiderThanEightBytesThenOnlyThatEntryIsSkipped() {
        byte[] nineBytes = SmlFrameBuilder.concat(tl(6, 9), new byte[9]);
        SmlReading reading = reading(
                new SmlFrameBuilder().entry(POWER, UNIT_W, 0, nineBytes).signed(FREQUENCY, UNIT_HZ, -1, 500, 2));

        assertEquals(Set.of("frequency"), reading.values().keySet());
    }

    @Test
    void whenUnknownObisCodesArePresentThenKnownEntriesAreAppliedAndNothingElse() {
        SmlReading reading = reading(new SmlFrameBuilder().signed(ANGLE, 8, 0, 10, 1)
                .signed(TARIFF_1, UNIT_WH, -1, 5, 2).signed(VENDOR, UNIT_W, 0, 3, 1)
                .octets(FIRMWARE, new byte[] { 'a', 'b' }).signed(POWER, UNIT_W, 0, 900, 2));

        assertEquals(Set.of("power"), reading.values().keySet());
    }

    @Test
    void whenEntriesAreInAnotherOrderThenTheResultIsIdentical() {
        SmlFrameBuilder forward = new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 900, 2)
                .unsigned(ENERGY_IMPORT, UNIT_WH, -1, BigInteger.valueOf(12345678), 8)
                .signed(VOLTAGE_L1, UNIT_V, -1, 2255, 2).signed(FREQUENCY, UNIT_HZ, -1, 499, 2);
        SmlFrameBuilder backward = new SmlFrameBuilder().signed(FREQUENCY, UNIT_HZ, -1, 499, 2)
                .signed(VOLTAGE_L1, UNIT_V, -1, 2255, 2)
                .unsigned(ENERGY_IMPORT, UNIT_WH, -1, BigInteger.valueOf(12345678), 8).signed(POWER, UNIT_W, 0, 900, 2);

        assertEquals(sorted(reading(forward)), sorted(reading(backward)));
    }

    private static Map<String, String> sorted(SmlReading reading) {
        Map<String, String> out = new LinkedHashMap<>();
        reading.values().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> out.put(e.getKey(), e.getValue().toString()));
        return out;
    }

    @Test
    void whenTheValueListHasMoreThanFifteenEntriesThenTheTwoByteListHeaderIsHandled() {
        SmlFrameBuilder builder = new SmlFrameBuilder();
        for (int i = 0; i < 40; i++) {
            builder.signed(obis(0x70 + (i % 8), 0x07 + i), 8, 0, i, 1);
        }
        builder.signed(POWER, UNIT_W, 0, 1, 1);

        assertEquals(Set.of("power"), reading(builder).values().keySet());
    }

    @Test
    void whenAnEntryHoldsAnEscapeSequenceThenTheDoubledFormIsDecoded() {
        // 0x1b1b1b1b as the raw value: the sequence appears in the data and is doubled on the wire.
        SmlFrameBuilder builder = new SmlFrameBuilder().doubleEscapes().signed(POWER, UNIT_W, 0, 0x1b1b1b1bL, 4);

        assertValue("454761243", Units.WATT, reading(builder).values().get("power"));
    }

    @Test
    void whenPowerIsGivenDirectlyThenItWinsOverImportAndExport() {
        SmlReading reading = reading(new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 900, 2)
                .signed(POWER_IMPORT, UNIT_W, 0, 950, 2).signed(POWER_EXPORT, UNIT_W, 0, 0, 1));

        assertValue("900", Units.WATT, reading.values().get("power"));
        assertEquals(1, reading.values().size(), "the auxiliary codes are no channels");
    }

    @Test
    void whenPowerIsAbsentThenItIsDerivedFromImportMinusExport() {
        SmlReading reading = reading(
                new SmlFrameBuilder().signed(POWER_IMPORT, UNIT_W, 0, 0, 1).signed(POWER_EXPORT, UNIT_W, 0, 2300, 2));

        assertValue("-2300", Units.WATT, reading.values().get("power"));
    }

    @Test
    void whenOnlyImportPowerIsAvailableThenPowerIsAbsent() {
        SmlReading reading = reading(
                new SmlFrameBuilder().signed(POWER_IMPORT, UNIT_W, 0, 700, 2).signed(VOLTAGE_L1, UNIT_V, -1, 2255, 2));

        assertNull(reading.values().get("power"));
        assertEquals(Set.of("voltage-l1"), reading.values().keySet());
    }

    @Test
    void whenImportPowerHasTheWrongUnitThenPowerIsNotDerived() {
        SmlReading reading = reading(new SmlFrameBuilder().signed(POWER_IMPORT, UNIT_V, 0, 700, 2)
                .signed(POWER_EXPORT, UNIT_W, 0, 100, 2).signed(VOLTAGE_L1, UNIT_V, -1, 2255, 2));

        assertNull(reading.values().get("power"));
    }

    @Test
    void whenPowerIsExplicitlyZeroThenItIsZeroNotAbsent() {
        SmlReading reading = reading(new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 0, 1));

        assertValue("0", Units.WATT, reading.values().get("power"));
    }

    @Test
    void whenMeterReportsNoPowerAtAllThenNoPowerChannelIsPresent() {
        SmlReading reading = reading(new SmlFrameBuilder().unsigned(ENERGY_IMPORT, UNIT_WH, -1, BigInteger.TEN, 8)
                .unsigned(ENERGY_EXPORT, UNIT_WH, -1, BigInteger.ONE, 8).signed(VOLTAGE_L1, UNIT_V, -1, 2255, 2)
                .signed(VOLTAGE_L2, UNIT_V, -1, 2250, 2));

        assertEquals(Set.of("energy-import", "energy-export", "voltage-l1", "voltage-l2"), reading.values().keySet());
        assertFalse(reading.values().containsKey("power"));
        assertFalse(reading.values().containsKey("power-l1"));
        assertFalse(reading.values().containsKey("frequency"));
    }

    @Test
    void whenFrameHasOnlyFirmwareStringsThenThereIsNoReading() {
        SmlFrameBuilder builder = new SmlFrameBuilder().octets(FIRMWARE, new byte[] { 1, 2, 3 }).octets(METER_ID,
                new byte[] { 9, 9 });

        assertNull(single(builder.build()));
        assertTrue(SmlDecoder.decode(builder.build(), 0).isEmpty());
    }

    @Test
    void whenFrameHasNoListResponseThenThereIsNoReading() {
        byte[] close = message(0x0201, list(1, absent()));

        assertTrue(SmlDecoder.decode(new SmlFrameBuilder().frame(close), 0).isEmpty());
    }

    @Test
    void whenMeterIdIsPresentThenItIsHexAndOnlyThroughTheReading() {
        SmlReading reading = reading(
                new SmlFrameBuilder().octets(METER_ID, new byte[] { 0x0a, 0x01, 0x45 }).signed(POWER, UNIT_W, 0, 5, 1));

        assertEquals("0a0145", reading.meterId());
        assertFalse(reading.toString().contains("0a0145"));
    }

    @Test
    void whenMeterIdIsGivenAsANumberThenItIsIgnored() {
        SmlReading reading = reading(new SmlFrameBuilder().signed(METER_ID, 0, 0, 5, 1).signed(POWER, UNIT_W, 0, 5, 1));

        assertNull(reading.meterId());
    }

    @Test
    void whenTwoValidFramesAreInOnePayloadThenTheyAreAppliedInOrder() {
        byte[] first = new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 100, 2).build();
        byte[] second = new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 200, 2).build();

        List<SmlReading> readings = SmlDecoder.decode(SmlFrameBuilder.concat(first, second), 0);

        assertEquals(2, readings.size());
        assertValue("100", Units.WATT, readings.get(0).values().get("power"));
        assertValue("200", Units.WATT, readings.get(1).values().get("power"));
    }

    @Test
    void whenABadFrameIsFollowedByAGoodFrameThenOnlyTheGoodFrameIsApplied() {
        byte[] bad = new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 100, 2).build();
        bad[bad.length - 1] ^= 0x01;
        byte[] good = new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 200, 2).build();

        List<SmlReading> readings = SmlDecoder.decode(SmlFrameBuilder.concat(bad, good), 0);

        assertEquals(1, readings.size());
        assertValue("200", Units.WATT, readings.get(0).values().get("power"));
    }

    @Test
    void whenFrameIsTruncatedAtAnyLengthThenNoReadingIsProduced() {
        byte[] frame = Fixtures.observedFrame();
        for (int length = 0; length < frame.length; length++) {
            assertTrue(SmlDecoder.decode(Arrays.copyOf(frame, length), 0).isEmpty(), "length " + length);
        }
    }

    @Test
    void whenAnyByteOfTheFrameIsChangedThenNoReadingIsProduced() {
        byte[] frame = Fixtures.observedFrame();
        for (int i = 0; i < frame.length; i++) {
            byte[] changed = Fixtures.with(frame, i, (frame[i] & 0xFF) ^ 0x5a);

            assertTrue(SmlDecoder.decode(changed, 0).isEmpty(), "byte " + i);
        }
    }

    @Test
    void whenAnyByteOfTheMessagesIsChangedAndTheCrcIsRepairedThenNothingFailsUnexpectedly() {
        // The CRC cannot protect against a damaged body once repaired; the structure checks must hold on their own.
        byte[] frame = Fixtures.observedFrame();
        for (int i = 8; i < frame.length - 8 - 3; i++) {
            for (int value : new int[] { 0x00, 0xff, 0x7f, 0xf0 }) {
                byte[] changed = Fixtures.with(frame, i, value);
                int crc = SmlFrameBuilder.crc(changed, changed.length - 2);
                changed[changed.length - 2] = (byte) crc;
                changed[changed.length - 1] = (byte) (crc >> 8);

                assertDoesNotThrow(() -> SmlDecoder.decode(changed, 0));
            }
        }
    }

    @Test
    void whenAMessageIsCutInsideAValidFrameThenTheFrameIsDropped() {
        byte[] list = message(0x0701, new byte[] { 0x77, 0x01 });
        byte[] cut = Arrays.copyOf(list, list.length - 6);

        assertTrue(SmlDecoder.decode(new SmlFrameBuilder().frame(cut), 0).isEmpty());
    }

    @Test
    void whenAnElementLengthExceedsTheFrameThenTheFrameIsDropped() {
        // list of 6, then an octet string that announces 15 bytes but only 2 follow
        byte[] hostile = { 0x76, 0x0f, 0x01, 0x02 };

        assertTrue(SmlDecoder.decode(new SmlFrameBuilder().frame(hostile), 0).isEmpty());
    }

    @Test
    void whenAListCountExceedsTheRemainingBytesThenTheFrameIsDropped() {
        byte[] hostile = { 0x76, (byte) 0xff, (byte) 0xff, 0x0f, 0x01 };

        assertTrue(SmlDecoder.decode(new SmlFrameBuilder().frame(hostile), 0).isEmpty());
        assertTrue(SmlDecoder.decode(new SmlFrameBuilder().frame(new byte[] { 0x7f }), 0).isEmpty());
    }

    @Test
    void whenTheLengthHeaderIsLongerThanFourBytesThenTheFrameIsDropped() {
        byte[] hostile = { (byte) 0xf1, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, 0x01 };

        assertTrue(SmlDecoder.decode(new SmlFrameBuilder().frame(hostile), 0).isEmpty());
    }

    @Test
    void whenListsAreNestedDeeperThanEightLevelsThenTheFrameIsDropped() {
        List<Byte> bytes = new ArrayList<>();
        bytes.add((byte) 0x76);
        for (int i = 0; i < 30; i++) {
            bytes.add((byte) 0x71);
        }
        bytes.add((byte) 0x01);
        byte[] hostile = new byte[bytes.size()];
        for (int i = 0; i < hostile.length; i++) {
            hostile[i] = bytes.get(i);
        }

        assertTrue(SmlDecoder.decode(new SmlFrameBuilder().frame(hostile), 0).isEmpty());
    }

    @Test
    void whenAMessageDoesNotEndWithTheEndMarkerThenTheFrameIsDropped() {
        byte[] good = message(0x0201, list(1, absent()));
        byte[] wrongEnd = good.clone();
        wrongEnd[wrongEnd.length - 1] = 0x01;

        assertTrue(SmlDecoder.decode(new SmlFrameBuilder().frame(wrongEnd), 0).isEmpty());
        assertTrue(SmlDecoder.decode(new SmlFrameBuilder().frame(good), 0).isEmpty(), "no applicable entry");
    }

    @Test
    void whenReadingIsPrintedThenTheMeterIdIsNotIncluded() {
        SmlReading reading = single(Fixtures.observedFrame());

        assertNotNull(reading);
        assertFalse(reading.toString().contains(Fixtures.METER_ID));
        assertFalse(reading.toString().contains("0a01454652"));
    }
}
