/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.protocol;

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
