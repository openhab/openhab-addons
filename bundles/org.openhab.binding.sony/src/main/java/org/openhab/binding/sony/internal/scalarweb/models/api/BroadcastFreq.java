/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the broadcase frequenecy
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class BroadcastFreq {
    /** The broadcast frequency */
    private @Nullable Integer frequency;

    /** THe broadcast band */
    private @Nullable String band;

    /**
     * Constructor used for deserialization only
     */
    public BroadcastFreq() {
    }

    /**
     * Returns the broadcast frequency
     * 
     * @return possibly null broadcast frequency
     */
    public @Nullable Integer getFrequency() {
        return frequency;
    }

    /**
     * Returns the broadcast band
     * 
     * @return possibly null, possibly empty broadcast band
     */
    public @Nullable String getBand() {
        return band;
    }

    @Override
    public String toString() {
        return "BroadcastFreq [frequency=" + frequency + ", band=" + band + "]";
    }
}
