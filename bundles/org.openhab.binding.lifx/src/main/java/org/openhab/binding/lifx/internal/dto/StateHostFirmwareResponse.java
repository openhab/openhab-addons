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
import org.openhab.binding.lifx.internal.fields.UInt64Field;
import org.openhab.binding.lifx.internal.fields.Version;
import org.openhab.binding.lifx.internal.fields.VersionField;

/**
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateHostFirmwareResponse extends Packet {

    public static final int TYPE = 0x0F;

    public static final Field<Long> FIELD_BUILD = new UInt64Field().little();
    public static final Field<Long> FIELD_RESERVED = new UInt64Field().little();
    public static final Field<Version> FIELD_VERSION = new VersionField().little();

    private long build;
    private long reserved;
    private Version version;

    public long getBuild() {
        return build;
    }

    public void setBuild(long build) {
        this.build = build;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 20;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        build = FIELD_BUILD.value(bytes);
        reserved = FIELD_RESERVED.value(bytes);
        version = FIELD_VERSION.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_BUILD.bytes(build)).put(FIELD_RESERVED.bytes(reserved))
                .put(FIELD_VERSION.bytes(version));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
