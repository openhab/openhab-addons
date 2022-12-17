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
import org.openhab.binding.lifx.internal.fields.UInt32Field;

/**
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateVersionResponse extends Packet {

    public static final int TYPE = 0x21;

    public static final Field<Long> FIELD_VENDOR = new UInt32Field().little();
    public static final Field<Long> FIELD_PRODUCT = new UInt32Field().little();
    public static final Field<Long> FIELD_VERSION = new UInt32Field().little();

    private long vendor;
    private long product;
    private long version;

    public long getVendor() {
        return vendor;
    }

    public void setVendor(long build) {
        this.vendor = build;
    }

    public long getProduct() {
        return product;
    }

    public void setProduct(long product) {
        this.product = product;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 12;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        vendor = FIELD_VENDOR.value(bytes);
        product = FIELD_PRODUCT.value(bytes);
        version = FIELD_VERSION.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_VENDOR.bytes(vendor)).put(FIELD_PRODUCT.bytes(product))
                .put(FIELD_VERSION.bytes(version));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }
}
