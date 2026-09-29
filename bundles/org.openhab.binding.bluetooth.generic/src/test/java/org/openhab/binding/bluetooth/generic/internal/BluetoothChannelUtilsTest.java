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
package org.openhab.binding.bluetooth.generic.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.bluetooth.gattparser.BluetoothGattParser;
import org.openhab.bluetooth.gattparser.BluetoothGattParserFactory;
import org.openhab.bluetooth.gattparser.GattRequest;
import org.openhab.core.library.types.DecimalType;

/**
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public class BluetoothChannelUtilsTest {

    private static final String TEMPERATURE_UUID = "2A6E";
    private static final String TEMPERATURE_FIELD = "Temperature";

    @Test
    public void encodeDecodeFieldNameTest() {
        String str = "easure";
        assertEquals(str, BluetoothChannelUtils.decodeFieldName(BluetoothChannelUtils.encodeFieldName(str)));
    }

    /**
     * An integer field that carries a DecimalExponent represents a fractional real-world value: the
     * Temperature characteristic (0x2A6E) is a sint16 in units of 0.01 degrees C. Writing 21.5 degrees
     * must serialize to the raw value 2150, not 2100 (which is what truncating the state to a long
     * before applying the field multiplier would produce).
     */
    @Test
    public void updateHolderPreservesDecimalsOnIntegerFieldWithExponent() {
        BluetoothGattParser parser = BluetoothGattParserFactory.getDefault();
        GattRequest request = parser.prepare(TEMPERATURE_UUID);

        BluetoothChannelUtils.updateHolder(parser, request, TEMPERATURE_FIELD, new DecimalType("21.5"));

        byte[] serialized = parser.serialize(request);
        // sint16, little endian: 2150 == 0x0866
        assertEquals(2, serialized.length);
        assertEquals(2150, (short) ((serialized[1] & 0xFF) << 8 | (serialized[0] & 0xFF)));
    }
}
