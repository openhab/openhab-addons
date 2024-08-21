/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
public class SetHevCycleRequest extends Packet {

    public static final int TYPE = 0x8F;

    public static final Field<Boolean> FIELD_ENABLE = new BoolIntField();
    public static final Field<Long> FIELD_DURATION = new UInt32Field().little();

    private boolean enable;
    private long duration;

    public boolean isEnable() {
        return enable;
    }

    public Duration getDuration() {
        return Duration.ofSeconds(duration);
    }

    public SetHevCycleRequest() {
        setAddressable(true);
        setResponseRequired(true);
    }

    public SetHevCycleRequest(boolean enable) {
        this();
        this.enable = enable;
    }

    public SetHevCycleRequest(boolean enable, Duration duration) {
        this();
        this.enable = enable;
        this.duration = duration.toSeconds();
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 5;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        enable = FIELD_ENABLE.value(bytes);
        duration = FIELD_DURATION.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_ENABLE.bytes(enable)).put(FIELD_DURATION.bytes(duration));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StateHevCycleResponse.TYPE };
    }
}
