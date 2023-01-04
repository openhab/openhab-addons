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
package org.openhab.binding.lifx.internal.dto;

import java.nio.ByteBuffer;

import org.openhab.binding.lifx.internal.fields.Field;
import org.openhab.binding.lifx.internal.fields.UInt16Field;

/**
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class SetPowerRequest extends Packet {

    public static final int TYPE = 0x15;

    public static final Field<Integer> FIELD_STATE = new UInt16Field();

    private PowerState state;

    public PowerState getState() {
        return state;
    }

    public SetPowerRequest() {
        state = PowerState.OFF;
        setAddressable(true);
        setResponseRequired(true);
    }

    public SetPowerRequest(PowerState state) {
        this.state = state;
        setAddressable(true);
        setResponseRequired(true);
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 2;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        state = PowerState.fromValue(FIELD_STATE.value(bytes));
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(2).put(FIELD_STATE.bytes(state.getValue()));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StatePowerResponse.TYPE };
    }
}
