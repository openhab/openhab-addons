/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.cosem;

import org.eclipse.smarthome.core.library.types.StringType;

/**
 * {@link CosemString} represents a string value.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Class now a factory instead of data containing class
 */
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
