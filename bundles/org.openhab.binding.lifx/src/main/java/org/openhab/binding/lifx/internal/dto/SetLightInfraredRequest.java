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
import org.openhab.binding.lifx.internal.fields.UInt16Field;

/**
 * @author Wouter Born - Initial contribution
 */
public class SetLightInfraredRequest extends Packet {

    public static final int TYPE = 0x7A;

    public static final Field<Integer> FIELD_STATE = new UInt16Field().little();

    private int infrared;

    public int getInfrared() {
        return infrared;
    }

    public SetLightInfraredRequest() {
        setTagged(false);
        setAddressable(true);
        setResponseRequired(true);
    }

    public SetLightInfraredRequest(int infrared) {
        this();
        this.infrared = infrared;
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
        infrared = FIELD_STATE.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(2).put(FIELD_STATE.bytes(infrared));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StateLightInfraredResponse.TYPE };
    }
}
