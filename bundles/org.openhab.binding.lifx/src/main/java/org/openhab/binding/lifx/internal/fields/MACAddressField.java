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
package org.openhab.binding.lifx.internal.fields;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class MACAddressField extends Field<MACAddress> {

    public MACAddressField() {
        super(8);
    }

    @Override
    public int defaultLength() {
        return 8;
    }

    @Override
    public MACAddress value(ByteBuffer bytes) {
        byte[] data = new byte[length];
        bytes.get(data);

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.limit(length - 2);
        return new MACAddress(buffer);
    }

    @Override
    protected ByteBuffer bytesInternal(MACAddress value) {
        return value.getBytes().duplicate();
    }

    @Override
    public ByteBuffer bytes(MACAddress value) {
        byte[] data = new byte[length];
        ByteBuffer bytes = bytesInternal(value);
        bytes.rewind();
        bytes.get(data, 0, bytes.limit());
        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.rewind();
        return buf;
    }
}
