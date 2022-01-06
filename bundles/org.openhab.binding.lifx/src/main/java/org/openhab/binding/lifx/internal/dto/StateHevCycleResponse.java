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
import java.time.Duration;

import org.openhab.binding.lifx.internal.fields.BoolIntField;
import org.openhab.binding.lifx.internal.fields.Field;
import org.openhab.binding.lifx.internal.fields.UInt32Field;

/**
 * @author Wouter Born - Initial contribution
 */
public class StateHevCycleResponse extends Packet {

    public static final int TYPE = 0x90;

    public static final Field<Long> FIELD_DURATION = new UInt32Field().little();
    public static final Field<Long> FIELD_REMAINING = new UInt32Field().little();
    public static final Field<Boolean> FIELD_LAST_POWER = new BoolIntField();

    private long duration;
    private long remaining;
    private boolean lastPower;

    public Duration getDuration() {
        return Duration.ofSeconds(duration);
    }

    public Duration getRemaining() {
        return Duration.ofSeconds(remaining);
    }

    public boolean isLastPower() {
        return lastPower;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 9;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        duration = FIELD_DURATION.value(bytes);
        remaining = FIELD_REMAINING.value(bytes);
        lastPower = FIELD_LAST_POWER.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_DURATION.bytes(duration))
                .put(FIELD_REMAINING.bytes(remaining)).put(FIELD_LAST_POWER.bytes(lastPower));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
