/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.rego6xx;

import javax.xml.bind.DatatypeConverter;

/**
 * The {@link AbstractResponseParser} is responsible for parsing responses coming from
 * rego6xx controllers.
 *
 * @author Boris Krivonog - Initial contribution
 */
abstract class AbstractResponseParser<T> implements ResponseParser<T> {
    private static final byte ComputerAddress = (byte) 0x01;

    @Override
    public abstract int responseLength();

    protected abstract T convert(byte[] responseBytes);

    @Override
    public T parse(byte[] buffer) throws Rego6xxProtocolException {
        if (buffer.length != responseLength()) {
            throw new Rego6xxProtocolException(
                    "Expected size does not match: " + buffer.length + " != " + responseLength());
        }

        if (buffer[0] != ComputerAddress) {
            throw new Rego6xxProtocolException("Invalid header " + DatatypeConverter.printHexBinary(buffer));
        }

        if (Checksum.calculate(buffer, 1, responseLength() - 2) != buffer[responseLength() - 1]) {
            throw new Rego6xxProtocolException("Invalid crc - " + DatatypeConverter.printHexBinary(buffer));
        }

        return convert(buffer);
    }
}
