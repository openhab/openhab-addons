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
import org.openhab.binding.lifx.internal.fields.StringField;
import org.openhab.binding.lifx.internal.fields.UInt64Field;

/**
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class TagLabelsResponse extends Packet {

    public static final int TYPE = 0x1F;

    public static final Field<Long> FIELD_TAGS = new UInt64Field();
    public static final Field<String> FIELD_LABEL = new StringField(32).utf8();

    private long tags;
    private String label;

    public long getTags() {
        return tags;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 40;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        tags = FIELD_TAGS.value(bytes);
        label = FIELD_LABEL.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_TAGS.bytes(tags)).put(FIELD_LABEL.bytes(label));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
