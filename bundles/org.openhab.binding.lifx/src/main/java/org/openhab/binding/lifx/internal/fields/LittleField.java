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
package org.openhab.binding.lifx.internal.fields;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Reads a wrapped field in reversed byte order.
 *
 * @author Tim Buckley - Initial contribution
 */
@NonNullByDefault
public class LittleField<T> extends Field<T> {

    private final Field<T> wrapped;

    public LittleField(Field<T> wrapped) {
        super(wrapped.length);

        this.wrapped = wrapped;
    }

    @Override
    public int defaultLength() {
        return wrapped.defaultLength();
    }

    @Override
    public T value(ByteBuffer bytes) {
        byte[] field = new byte[wrapped.length];
        bytes.get(field);

        ByteBuffer flipped = flip(ByteBuffer.wrap(field));

        T value = wrapped.value(flipped);

        return value;
    }

    @Override
    public ByteBuffer bytesInternal(T value) {
        return flip(wrapped.bytes(value));
    }

    public static ByteBuffer flip(ByteBuffer buf) {
        buf.rewind();

        ByteBuffer ret = ByteBuffer.allocate(buf.limit());

        for (int i = buf.limit() - 1; i >= 0; i--) {
            ret.put(buf.get(i));
        }

        ret.rewind();

        return ret;
    }
}
