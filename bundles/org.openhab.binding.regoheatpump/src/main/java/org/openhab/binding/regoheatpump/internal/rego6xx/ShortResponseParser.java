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
package org.openhab.binding.regoheatpump.internal.rego6xx;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ShortResponseParser} is responsible for parsing short form data format
 * coming from the rego 6xx unit.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
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
