/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal.dto;

import java.nio.ByteBuffer;

import org.openhab.binding.lifx.internal.fields.Field;
import org.openhab.binding.lifx.internal.fields.FloatField;
import org.openhab.binding.lifx.internal.fields.UInt16Field;
import org.openhab.binding.lifx.internal.fields.UInt32Field;

/**
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateWifiInfoResponse extends Packet {

    public static final int TYPE = 0x11;

    public static final Field<Float> FIELD_SIGNAL = new FloatField().little();
    public static final Field<Long> FIELD_RX = new UInt32Field().little();
    public static final Field<Long> FIELD_TX = new UInt32Field().little();
    public static final Field<Integer> FIELD_TEMP = new UInt16Field();

    private float signal;
    private long rx;
    private long tx;
    private int mcuTemperature;

    public SignalStrength getSignalStrength() {
        return new SignalStrength(signal);
    }

    public long getRx() {
        return rx;
    }

    public long getTx() {
        return tx;
    }

    public int getMcuTemperature() {
        return mcuTemperature;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 14;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        signal = FIELD_SIGNAL.value(bytes);
        rx = FIELD_RX.value(bytes);
        tx = FIELD_TX.value(bytes);
        mcuTemperature = FIELD_TEMP.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_SIGNAL.bytes(signal)).put(FIELD_RX.bytes(rx))
                .put(FIELD_TX.bytes(tx)).put(FIELD_TEMP.bytes(mcuTemperature));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
