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
import org.openhab.binding.lifx.internal.fields.UInt64Field;

/**
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateInfoResponse extends Packet {

    public static final int TYPE = 0x23;

    public static final Field<Long> FIELD_TIME = new UInt64Field().little();
    public static final Field<Long> FIELD_UPTIME = new UInt64Field().little();
    public static final Field<Long> FIELD_DOWNTIME = new UInt64Field().little();

    private long time;
    private long uptime;
    private long downtime;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public long getDowntime() {
        return downtime;
    }

    public void setDowntime(long downtime) {
        this.downtime = downtime;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 24;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        time = FIELD_TIME.value(bytes);
        uptime = FIELD_UPTIME.value(bytes);
        downtime = FIELD_DOWNTIME.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_TIME.bytes(time)).put(FIELD_UPTIME.bytes(uptime))
                .put(FIELD_DOWNTIME.bytes(downtime));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
