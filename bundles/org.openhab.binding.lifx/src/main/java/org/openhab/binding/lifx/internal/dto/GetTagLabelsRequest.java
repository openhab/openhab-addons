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
public class GetTagLabelsRequest extends Packet {

    public static final int TYPE = 0x1D;

    public static final Field<Long> FIELD_TAGS = new UInt64Field();

    private long tags;

    public long getTags() {
        return tags;
    }

    public GetTagLabelsRequest() {
    }

    public GetTagLabelsRequest(long tags) {
        this.tags = tags;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 8;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        tags = FIELD_TAGS.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_TAGS.bytes(tags));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { TagLabelsResponse.TYPE };
    }
}
