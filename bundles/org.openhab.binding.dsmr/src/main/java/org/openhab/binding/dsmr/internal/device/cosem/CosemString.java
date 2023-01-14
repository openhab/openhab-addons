/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.device.cosem;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;

/**
 * {@link CosemString} represents a string value.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Class now a factory instead of data containing class
 */
@NonNullByDefault
class CosemString extends CosemValueDescriptor<StringType> {

    public static final CosemString INSTANCE = new CosemString();

    private CosemString() {
    }

    public CosemString(String channelId) {
        super(channelId);
    }

    /**
     * Parses a String value to a {@link StringType}.
     *
     * @param cosemValue the value to parse
     * @return {@link StringType} representing the value of the cosem value
     */
    @Override
    protected StringType getStateValue(String cosemValue) {
        return new StringType(cosemValue);
    }
}
