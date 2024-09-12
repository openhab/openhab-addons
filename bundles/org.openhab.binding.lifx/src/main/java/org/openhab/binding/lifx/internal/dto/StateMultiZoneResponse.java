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

import org.openhab.binding.lifx.internal.fields.Field;
import org.openhab.binding.lifx.internal.fields.HSBK;
import org.openhab.binding.lifx.internal.fields.HSBKField;
import org.openhab.binding.lifx.internal.fields.UInt8Field;

/**
 * @author Wouter Born - Initial contribution
 */
public class StateMultiZoneResponse extends Packet {

    public static final int TYPE = 0x1FA;
    public static final int ZONES = 8;

    public static final Field<Integer> FIELD_COUNT = new UInt8Field();
    public static final Field<Integer> FIELD_INDEX = new UInt8Field();
    public static final HSBKField FIELD_COLOR = new HSBKField();

    private int count;
    private int index;
    private HSBK[] colors = new HSBK[ZONES];

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("count=");
        sb.append(count);
        sb.append(", index=");
        sb.append(index);

        for (int i = 0; i < ZONES; i++) {
            sb.append(", ");
            sb.append(colors[i].toString("color[" + i + "]"));
        }

        return sb.toString();
    }

    public int getCount() {
        return count;
    }

    public int getIndex() {
        return index;
    }

    public HSBK[] getColors() {
        return colors;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 66;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        count = FIELD_COUNT.value(bytes);
        index = FIELD_INDEX.value(bytes);
        for (int i = 0; i < ZONES; i++) {
            colors[i] = FIELD_COLOR.value(bytes);
        }
    }

    @Override
    protected ByteBuffer packetBytes() {
        ByteBuffer bb = ByteBuffer.allocate(packetLength()).put(FIELD_COUNT.bytes(count)).put(FIELD_INDEX.bytes(index));
        for (int i = 0; i < ZONES; i++) {
            bb.put(FIELD_COLOR.bytes(colors[i]));
        }
        return bb;
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
