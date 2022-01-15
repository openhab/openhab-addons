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
import org.openhab.binding.lifx.internal.fields.UInt8Field;

/**
 * @author Wouter Born - Initial contribution
 */
public class StateLastHevCycleResultResponse extends Packet {

    public static final int TYPE = 0x95;

    public static final Field<Integer> FIELD_RESULT = new UInt8Field().little();

    private int result;

    public LightLastHevCycleResult getResult() {
        return LightLastHevCycleResult.fromValue(result);
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 1;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        result = FIELD_RESULT.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_RESULT.bytes(result));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
