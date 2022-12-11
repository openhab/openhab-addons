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

import static org.openhab.binding.lifx.internal.LifxBindingConstants.*;

import java.nio.ByteBuffer;

import org.openhab.binding.lifx.internal.fields.Field;
import org.openhab.binding.lifx.internal.fields.UInt8Field;

/**
 * @author Wouter Born - Initial contribution
 */
public class GetColorZonesRequest extends Packet {

    public static final int TYPE = 0x1F6;

    public static final Field<Integer> FIELD_START_INDEX = new UInt8Field();
    public static final Field<Integer> FIELD_END_INDEX = new UInt8Field();

    private int startIndex = MIN_ZONE_INDEX;
    private int endIndex = MAX_ZONE_INDEX;

    public GetColorZonesRequest() {
        setAddressable(true);
        setResponseRequired(true);
    }

    public GetColorZonesRequest(int index) {
        this(index, index);
    }

    public GetColorZonesRequest(int startIndex, int endIndex) {
        this();
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
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
        startIndex = FIELD_START_INDEX.value(bytes);
        endIndex = FIELD_END_INDEX.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_START_INDEX.bytes(startIndex))
                .put(FIELD_END_INDEX.bytes(endIndex));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StateMultiZoneResponse.TYPE, StateZoneResponse.TYPE };
    }
}
