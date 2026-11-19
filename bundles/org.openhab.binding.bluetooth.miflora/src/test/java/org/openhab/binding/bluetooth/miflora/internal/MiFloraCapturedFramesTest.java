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
package org.openhab.binding.bluetooth.miflora.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.miflora.internal.MiBeaconParser.Frame;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.util.HexUtils;

/**
 * Decodes every distinct frame two Mi Flora sensors broadcast during a three-minute capture and compares the result
 * with what the JavaScript rule this binding replaces produced from the same frames.
 *
 * @author Lukas Brinias - Initial contribution
 */
@NonNullByDefault
public class MiFloraCapturedFramesTest {

    // Service data as captured, the channel it belongs to, and the state the rule posted for it
    private static final String[][] FRAMES = { { "712098009527B1127E855C0D0910020505", "conductivity", "1285 µS/cm" },
            { "712098009627B1127E855C0D041002DD00", "temperature", "22.1 °C" },
            { "712098009727B1127E855C0D071003A80000", "illuminance", "168 lx" },
            { "712098009827B1127E855C0D08100115", "moisture", "21 %" },
            { "712098009927B1127E855C0D091002FC04", "conductivity", "1276 µS/cm" },
            { "712098009A27B1127E855C0D041002DD00", "temperature", "22.1 °C" },
            { "712098009B27B1127E855C0D071003A80000", "illuminance", "168 lx" },
            { "712098009C27B1127E855C0D08100115", "moisture", "21 %" },
            { "712098009D27B1127E855C0D0910020505", "conductivity", "1285 µS/cm" },
            { "712098009E27B1127E855C0D041002DC00", "temperature", "22 °C" },
            { "7120980095BCB8127E855C0D041002DC00", "temperature", "22 °C" },
            { "712098009F27B1127E855C0D071003A80000", "illuminance", "168 lx" },
            { "71209800A027B1127E855C0D08100115", "moisture", "21 %" },
            { "7120980096BCB8127E855C0D071003AE0000", "illuminance", "174 lx" },
            { "7120980097BCB8127E855C0D08100128", "moisture", "40 %" },
            { "71209800A127B1127E855C0D0910021105", "conductivity", "1297 µS/cm" },
            { "7120980098BCB8127E855C0D091002D502", "conductivity", "725 µS/cm" },
            { "71209800A227B1127E855C0D041002DC00", "temperature", "22 °C" },
            { "7120980099BCB8127E855C0D041002DD00", "temperature", "22.1 °C" },
            { "71209800A327B1127E855C0D071003A80000", "illuminance", "168 lx" },
            { "71209800A427B1127E855C0D08100115", "moisture", "21 %" },
            { "712098009ABCB8127E855C0D071003970000", "illuminance", "151 lx" },
            { "712098009BBCB8127E855C0D08100128", "moisture", "40 %" },
            { "71209800A527B1127E855C0D0910021105", "conductivity", "1297 µS/cm" },
            { "712098009CBCB8127E855C0D091002D802", "conductivity", "728 µS/cm" },
            { "71209800A627B1127E855C0D041002DD00", "temperature", "22.1 °C" },
            { "712098009DBCB8127E855C0D041002DD00", "temperature", "22.1 °C" },
            { "71209800A727B1127E855C0D071003A80000", "illuminance", "168 lx" },
            { "71209800A927B1127E855C0D0910020505", "conductivity", "1285 µS/cm" } };

    @Test
    public void capturedFramesDecodeLikeTheRule() {
        for (String[] row : FRAMES) {
            Frame frame = MiBeaconParser.parse(HexUtils.hexToBytes(row[0]));
            assertNotNull(frame, row[0]);
            assertEquals(0x0098, frame.productId(), row[0]);
            assertEquals(1, frame.readings().size(), row[0]);

            Map.Entry<String, State> channelState = MiFloraHandler.toChannelState(frame.readings().get(0));
            assertNotNull(channelState, row[0]);
            assertEquals(row[1], channelState.getKey(), row[0]);
            QuantityType<?> expected = new QuantityType<>(row[2]);
            QuantityType<?> actual = assertInstanceOf(QuantityType.class, channelState.getValue(), row[0]);
            QuantityType<?> converted = actual.toUnit(expected.getUnit());
            assertNotNull(converted, row[0]);
            assertEquals(0, expected.toBigDecimal().compareTo(converted.toBigDecimal()), row[0]);
        }
    }
}
