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
public class UInt16Field extends Field<Integer> {

    @Override
    public int defaultLength() {
        return 2;
    }

    @Override
    public Integer value(ByteBuffer bytes) {
        return bytes.getShort() & 0xFFFF;
    }

    @Override
    public ByteBuffer bytesInternal(Integer value) {
        return ByteBuffer.allocate(2).putShort((short) (value & 0xFFFF));
    }
}
