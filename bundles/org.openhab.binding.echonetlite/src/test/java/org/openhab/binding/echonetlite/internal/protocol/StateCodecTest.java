/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.echonetlite.internal.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.echonetlite.internal.LangUtil.b;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.echonetlite.internal.StateCodec;
import org.openhab.binding.echonetlite.internal.StateCodec.HexStringCodec;
import org.openhab.binding.echonetlite.internal.StateCodec.OperatingTimeDecode;
import org.openhab.binding.echonetlite.internal.StateCodec.Option;
import org.openhab.binding.echonetlite.internal.StateCodec.OptionCodec;
import org.openhab.binding.echonetlite.internal.StateCodec.StandardVersionInformationCodec;
import org.openhab.binding.echonetlite.internal.StateDecode;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
class StateCodecTest {
    private void assertEncodeDecode(StateCodec stateCodec, State state, byte[] expectedOutput) {
        final ByteBuffer buffer = ByteBuffer.allocate(1024);
        stateCodec.encodeState(state, buffer);
        buffer.flip();

        final byte[] encoded = new byte[buffer.remaining()];
        buffer.get(encoded);
        assertArrayEquals(expectedOutput, encoded);

        buffer.flip();

        assertEquals(state, stateCodec.decodeState(buffer));
    }

    private void assertDecode(StateDecode stateDecode, State expectedState, byte[] data) {
        assertEquals(expectedState, stateDecode.decodeState(ByteBuffer.wrap(data)));
    }

    @Test
    void shouldEncodeOnOff() {
        final int on = 34;
        final int off = 27;
        final StateCodec.OnOffCodec onOffCodec = new StateCodec.OnOffCodec(on, off);

        assertEncodeDecode(onOffCodec, OnOffType.ON, new byte[] { b(on) });
        assertEncodeDecode(onOffCodec, OnOffType.OFF, new byte[] { b(off) });
    }

    @Test
    void shouldDecodeStandardVersionInformation() {
        assertDecode(StandardVersionInformationCodec.INSTANCE, StringType.EMPTY, new byte[0]);
        assertDecode(StandardVersionInformationCodec.INSTANCE, StringType.EMPTY, new byte[1]);
        assertDecode(StandardVersionInformationCodec.INSTANCE, StringType.EMPTY, new byte[2]);
        assertDecode(StandardVersionInformationCodec.INSTANCE, StringType.EMPTY, new byte[3]);
        assertDecode(StandardVersionInformationCodec.INSTANCE, StringType.EMPTY, new byte[5]);
        assertDecode(StandardVersionInformationCodec.INSTANCE, new StringType("A"), new byte[] { 0, 0, 'A', 0 });
        assertDecode(StandardVersionInformationCodec.INSTANCE, new StringType("Z"), new byte[] { 0, 0, 'Z', 0 });
    }

    @Test
    void shouldDecodeHexString() {
        assertDecode(HexStringCodec.INSTANCE, new StringType("000102030467"), new byte[] { 0, 1, 2, 3, 4, b(0x67) });
    }

    @Test
    void shouldDecodeCumulativeOperatingTime() {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[5]);
        buffer.order(ByteOrder.BIG_ENDIAN);

        final int valueInSeconds = 484260;
        final long valueInMinutes = TimeUnit.SECONDS.toMinutes(valueInSeconds);
        buffer.put(b(0x42));
        buffer.putInt((int) valueInMinutes);

        buffer.flip();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(valueInSeconds, ((QuantityType<?>) OperatingTimeDecode.INSTANCE.decodeState(buffer)).intValue());

        buffer.flip();
        buffer.order(ByteOrder.BIG_ENDIAN);
        assertEquals(valueInSeconds, ((QuantityType<?>) OperatingTimeDecode.INSTANCE.decodeState(buffer)).intValue());
    }

    @Test
    void shouldEncodeDecodeOption() {
        final OptionCodec optionCodec = new OptionCodec(new Option("ABC", 123), new Option("DEF", 101));
        assertEncodeDecode(optionCodec, new StringType("ABC"), new byte[] { 123 });
        assertEncodeDecode(optionCodec, new StringType("DEF"), new byte[] { 101 });
    }

    @Test
    void shouldEncodeAndDecode8Bit() {
        assertEncodeDecode(StateCodec.Decimal8bitCodec.INSTANCE, new DecimalType(123), new byte[] { 123 });
        assertEncodeDecode(StateCodec.Decimal8bitCodec.INSTANCE, new DecimalType(1), new byte[] { 1 });
        assertEncodeDecode(StateCodec.Decimal8bitCodec.INSTANCE, new DecimalType(-1), new byte[] { b(255) });
    }

    @Test
    void shouldEncodeAndDecodeTemperature() {
        assertEncodeDecode(StateCodec.Temperature8bitCodec.INSTANCE, new QuantityType<>(123, SIUnits.CELSIUS),
                new byte[] { 123 });
        assertEncodeDecode(StateCodec.Temperature8bitCodec.INSTANCE, new QuantityType<>(1, SIUnits.CELSIUS),
                new byte[] { 1 });
        assertEncodeDecode(StateCodec.Temperature8bitCodec.INSTANCE, new QuantityType<>(-1, SIUnits.CELSIUS),
                new byte[] { b(255) });
    }
}
