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

import org.openhab.binding.lifx.internal.fields.ByteField;
import org.openhab.binding.lifx.internal.fields.Field;
import org.openhab.binding.lifx.internal.fields.StringField;
import org.openhab.binding.lifx.internal.fields.UInt64Field;

/**
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateLocationResponse extends Packet {

    public static final int TYPE = 0x32;

    public static final Field<ByteBuffer> FIELD_LOCATION = new ByteField(16);
    public static final Field<String> FIELD_LABEL = new StringField(32).utf8();
    public static final Field<Long> FIELD_UPDATED_AT = new UInt64Field().little();

    private ByteBuffer location;
    private String label;
    private long updatedAt;

    public static int getType() {
        return TYPE;
    }

    public ByteBuffer getLocation() {
        return location;
    }

    public void setLocation(ByteBuffer location) {
        this.location = location;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
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
        location = FIELD_LOCATION.value(bytes);
        label = FIELD_LABEL.value(bytes);
        updatedAt = FIELD_UPDATED_AT.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_LOCATION.bytes(location)).put(FIELD_LABEL.bytes(label))
                .put(FIELD_UPDATED_AT.bytes(updatedAt));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
