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
package org.openhab.binding.tibberpulselocal.internal.sml;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibberpulselocal.internal.sml.SmlFrameBuilder.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.Fixtures;
import org.openhab.core.library.types.QuantityType;

/**
 * Hostile and randomised input for the frame scanner and the decoder (QA review, stage 5). The properties checked:
 * nothing throws, nothing runs for long, and every accepted value is exact.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(120)
class SmlAdversarialInputTest {

    private static final byte[] START = { 0x1b, 0x1b, 0x1b, 0x1b, 1, 1, 1, 1 };

    @Test
    void whenDoubledEscapesAreFollowedByManyStartSequencesThenExtractionStaysFast() {
        // Every second half of a doubled escape followed by 01010101 looks like a new frame start.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(START);
        while (out.size() < 64 * 1024 - 12) {
            out.writeBytes(new byte[] { 0x1b, 0x1b, 0x1b, 0x1b, 0x1b, 0x1b, 0x1b, 0x1b, 1, 1, 1, 1 });
        }
        byte[] payload = out.toByteArray();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertTrue(SmlFrames.extract(payload, 0).isEmpty()));
    }

    @Test
    void whenRandomMessageBytesCarryAValidFrameCrcThenDecodingNeverThrows() {
        byte[] alphabet = { 0x00, 0x01, 0x02, 0x05, 0x06, 0x07, 0x0f, 0x1b, 0x52, 0x62, 0x63, 0x65, 0x69, 0x70, 0x71,
                0x76, 0x77, 0x7f, (byte) 0x81, (byte) 0x82, (byte) 0x87, (byte) 0xff };
        SmlFrameBuilder builder = new SmlFrameBuilder();
        for (int seed = 0; seed < 30_000; seed++) {
            Random random = new Random(seed);
            byte[] messages = new byte[random.nextInt(160)];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = alphabet[random.nextInt(alphabet.length)];
            }
            byte[] frame = builder.frame(messages);
            try {
                for (SmlReading reading : SmlDecoder.decode(frame, 0)) {
                    assertFalse(reading.values().isEmpty());
                }
            } catch (RuntimeException | StackOverflowError e) {
                fail("seed " + seed + ": " + e);
            }
        }
    }

    @Test
    void whenSeveralBytesOfTheObservedMessagesAreChangedAtOnceThenDecodingNeverThrows() {
        byte[] body = SmlFrames.extract(Fixtures.observedFrame(), 0).get(0);
        SmlFrameBuilder builder = new SmlFrameBuilder();
        int decoded = 0;
        for (int seed = 0; seed < 20_000; seed++) {
            Random random = new Random(seed);
            byte[] mutated = body.clone();
            int changes = 1 + random.nextInt(6);
            for (int i = 0; i < changes; i++) {
                mutated[random.nextInt(mutated.length)] = (byte) random.nextInt(256);
            }
            if (random.nextInt(4) == 0) {
                mutated = Arrays.copyOf(mutated, random.nextInt(mutated.length));
            }
            try {
                decoded += SmlDecoder.decode(builder.frame(mutated), 0).size();
            } catch (RuntimeException | StackOverflowError e) {
                fail("seed " + seed + ": " + e);
            }
        }
        // Many mutations only touch a value or a signature, so a good share of the frames still decodes.
        assertTrue(decoded > 0);
    }

    @Test
    void whenIntegersOfAnyWidthAndScalerAreDecodedThenTheValueIsExactAndNeverInExponentNotation() {
        Random random = new Random(42);
        for (int i = 0; i < 4_000; i++) {
            int width = 1 + random.nextInt(8);
            boolean signed = random.nextBoolean();
            int scaler = random.nextInt(37) - 18;
            BigInteger raw;
            SmlFrameBuilder builder = new SmlFrameBuilder();
            if (signed) {
                BigInteger bound = BigInteger.ONE.shiftLeft(8 * width - 1);
                raw = new BigInteger(8 * width, random).subtract(bound);
                builder.signed(POWER, UNIT_W, scaler, raw.longValueExact(), width);
            } else {
                raw = new BigInteger(8 * width, random);
                builder.unsigned(POWER, UNIT_W, scaler, raw, width);
            }
            builder.unsigned(ENERGY_IMPORT, UNIT_WH, scaler, raw.abs(), 8);

            List<SmlReading> readings = SmlDecoder.decode(builder.build(), 0);

            assertEquals(1, readings.size(), "raw " + raw + " width " + width + " scaler " + scaler);
            BigDecimal expected = new BigDecimal(raw).scaleByPowerOfTen(scaler);
            QuantityType<?> power = readings.get(0).values().get("power");
            assertNotNull(power);
            assertEquals(0, expected.compareTo(power.toBigDecimal()), "power " + expected + " vs " + power);
            if (isPlainRepresentable(expected)) {
                assertFalse(power.toString().contains("E"), power.toString());
            }
            QuantityType<?> energy = readings.get(0).values().get("energy-import");
            assertNotNull(energy);
            assertEquals(0, new BigDecimal(raw.abs()).scaleByPowerOfTen(scaler - 3).compareTo(energy.toBigDecimal()));
            if (isPlainRepresentable(expected.abs().movePointLeft(3))) {
                assertFalse(energy.toString().contains("E"), energy.toString());
            }
        }
    }

    @Test
    void whenAnEscapedEscapeSequenceInTheFirstFrameIsFollowedByASecondFrameThenBothAreDecodedInOrder() {
        byte[] id = { 0x1b, 0x1b, 0x1b, 0x1b, 0x0a };
        byte[] first = new SmlFrameBuilder().doubleEscapes().signed(POWER, UNIT_W, 0, 100, 8).octets(METER_ID, id)
                .build();
        byte[] second = new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 200, 8).build();
        byte[] payload = concat(first, second);

        List<SmlReading> readings = SmlDecoder.decode(payload, 0);

        assertEquals(2, readings.size());
        assertEquals(new QuantityType<>("100 W"), readings.get(0).values().get("power"));
        assertEquals("1b1b1b1b0a", readings.get(0).meterId());
        assertEquals(new QuantityType<>("200 W"), readings.get(1).values().get("power"));
    }

    @Test
    void whenTheMeterIdIsLongerThanSixtyFourBytesThenItIsNotReportedButTheValuesAre() {
        byte[] atLimit = new byte[64];
        Arrays.fill(atLimit, (byte) 0x0a);
        byte[] tooLong = new byte[65];
        Arrays.fill(tooLong, (byte) 0x0a);

        List<SmlReading> accepted = SmlDecoder
                .decode(new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 1, 8).octets(METER_ID, atLimit).build(), 0);
        List<SmlReading> ignored = SmlDecoder
                .decode(new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 1, 8).octets(METER_ID, tooLong).build(), 0);

        assertEquals(1, accepted.size());
        assertEquals(128, accepted.get(0).meterId().length());
        assertEquals(1, ignored.size());
        assertNull(ignored.get(0).meterId());
    }

    @Test
    void whenAZeroHasALongScaleThenItIsPublishedWithoutExponentNotation() {
        for (int scaler = -18; scaler <= 18; scaler++) {
            byte[] frame = new SmlFrameBuilder().signed(POWER, UNIT_W, scaler, 0, 8)
                    .unsigned(ENERGY_EXPORT, UNIT_WH, scaler, BigInteger.ZERO, 8).build();

            SmlReading reading = SmlDecoder.decode(frame, 0).get(0);

            for (QuantityType<?> value : reading.values().values()) {
                assertEquals(0, BigDecimal.ZERO.compareTo(value.toBigDecimal()), "scaler " + scaler);
                assertFalse(value.toString().contains("E"), "scaler " + scaler + ": " + value);
            }
        }
    }

    /** BigDecimal prints plain digits unless the adjusted exponent is below -6 (zero is handled by the decoder). */
    private static boolean isPlainRepresentable(BigDecimal value) {
        return value.signum() == 0 || value.abs().compareTo(new BigDecimal("0.000001")) >= 0;
    }

    @Test
    void whenTheOffsetIsBeyondThePayloadOrNegativeThenNothingIsReturnedAndNothingIsThrown() {
        byte[] frame = new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 1, 8).build();

        assertTrue(SmlDecoder.decode(frame, frame.length).isEmpty());
        assertTrue(SmlDecoder.decode(frame, frame.length + 100).isEmpty());
        assertEquals(1, SmlDecoder.decode(frame, -5).size());
    }
}
