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
package org.openhab.binding.regoheatpump.internal.rego6xx;

/**
 * The {@link ErrorLineResponseParser} is responsible for parsing error information (log) entry.
 *
 * @author Boris Krivonog - Initial contribution
 */
class ErrorLineResponseParser extends AbstractLongResponseParser<ErrorLine> {

    @Override
    protected ErrorLine convert(byte[] responseBytes) {
        // 255 marks no error.
        if (responseBytes[1] == (byte) 255) {
            return null;
        }

        return new ErrorLine(ValueConverter.arrayToByte(responseBytes, 1),
                ValueConverter.stringFromBytes(responseBytes, 3, 15));
    }
}
