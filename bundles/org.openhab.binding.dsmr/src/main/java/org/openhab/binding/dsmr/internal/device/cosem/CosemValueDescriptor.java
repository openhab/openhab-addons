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

import java.text.ParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * This CosemValueDescriptor provides meta data for a CosemValue
 *
 * @author M. Volaart - Initial contribution
 */
@NonNullByDefault
abstract class CosemValueDescriptor<S extends State> {

    /**
     * String describing the channel on which this value descriptor is available.
     */
    private final String channelId;

    /**
     * Creates a new {@link CosemValueDescriptor} with no unit and a default channel.
     */
    public CosemValueDescriptor() {
        this("");
    }

    /**
     * Creates a new {@link CosemValueDescriptor}.
     *
     * @param channelId the channel for this CosemValueDescriptor
     */
    public CosemValueDescriptor(String channelId) {
        this.channelId = channelId;
    }

    /**
     * Parses the string value to the {@link State} value
     *
     * @param CosemValue the Cosem value to parse
     * @return S the {@link State} object instance of the Cosem value
     * @throws ParseException if parsing failed
     */
    protected abstract S getStateValue(String cosemValue) throws ParseException;

    /**
     * Returns the channel id for this {@link CosemValueDescriptor}
     *
     * @return the channel identifier
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Returns String representation of this {@link CosemValueDescriptor}
     *
     * @return String representation of this {@link CosemValueDescriptor}
     */
    @Override
    public String toString() {
        return "CosemValueDescriptor[channel=" + channelId + "]";
    }
}
