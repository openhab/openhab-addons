/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.protocol;

class ShortResponseParser extends AbstractResponseParser<Short> {

    @Override
    public int responseLength() {
        return 5;
    }

    @Override
    protected Short convert(byte[] responseBytes) {
        return ValueConverter.sevenBitFormatToShort(responseBytes, 1);
    }
}
