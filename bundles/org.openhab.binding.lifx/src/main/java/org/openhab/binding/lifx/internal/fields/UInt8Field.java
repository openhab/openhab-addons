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
 */
@NonNullByDefault
public class UInt8Field extends Field<Integer> {

    public UInt8Field() {
        super(1);
    }

    @Override
    public int defaultLength() {
        return 1;
    }

    @Override
    public Integer value(ByteBuffer bytes) {
        return (int) (bytes.get() & 0xFF);
    }

    @Override
    public ByteBuffer bytesInternal(Integer value) {
        return ByteBuffer.allocate(1).put((byte) (value & 0xFF));
    }
}
