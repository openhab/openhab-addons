/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.protocol;

abstract class AbstractResponseParser<T> implements ResponseParser<T> {
    private final static byte ComputerAddress = (byte) 0x01;

    @Override
    public abstract int responseLength();

    protected abstract T convert(byte[] responseBytes);

    @Override
    public T parse(byte[] buffer) {
        if (buffer == null) {
            throw new NullPointerException();
        }

        if (buffer.length != responseLength()) {
            throw new IllegalStateException(
                    "Expected size does not match: " + buffer.length + " != " + responseLength());
        }

        if (buffer[0] != ComputerAddress) {
            throw new IllegalStateException("Invalid header " + buffer[0]);
        }

        if (Checksum.calculate(buffer, 1, responseLength() - 2) != buffer[responseLength() - 1]) {
            throw new IllegalStateException("Invalid crc.");
        }

        return convert(buffer);
    }
}
