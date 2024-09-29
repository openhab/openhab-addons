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
import org.openhab.binding.lifx.internal.fields.StringField;
import org.openhab.binding.lifx.internal.fields.UInt16Field;
import org.openhab.binding.lifx.internal.fields.UInt64Field;

/**
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateResponse extends Packet {

    public static final int TYPE = 0x6B;

    public static final HSBKField FIELD_COLOR = new HSBKField();
    public static final Field<Integer> FIELD_DIM = new UInt16Field().little();
    public static final Field<Integer> FIELD_POWER = new UInt16Field();
    public static final Field<String> FIELD_LABEL = new StringField(32);
    public static final Field<Long> FIELD_TAGS = new UInt64Field();

    private HSBK color;
    private int dim;
    private PowerState power;
    private String label;
    private long tags;

    @Override
    public String toString() {
        return color.toString("color") + ", dim=" + dim + ", power=" + power + ", label=" + label;
    }

    public HSBK getColor() {
        return color;
    }

    public int getDim() {
        return dim;
    }

    public PowerState getPower() {
        return power;
    }

    public String getLabel() {
        return label;
    }

    public long getTags() {
        return tags;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 52;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        color = FIELD_COLOR.value(bytes);
        dim = FIELD_DIM.value(bytes);
        power = PowerState.fromValue(FIELD_POWER.value(bytes));
        label = FIELD_LABEL.value(bytes);
        tags = FIELD_TAGS.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_COLOR.bytes(color)).put(FIELD_DIM.bytes(dim))
                .put(FIELD_POWER.bytes(power.getValue())).put(FIELD_LABEL.bytes(label)).put(FIELD_TAGS.bytes(tags));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
