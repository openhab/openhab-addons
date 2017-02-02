/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.meter;

/**
 * The DSMRMeterDescriptor describes a meter.
 *
 * A DSMR Meter consists of the following properties:
 * - MeterType
 * - M-Bus channel
 * - Identifier
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class DSMRMeterDescriptor {
    // Meter type
    private final DSMRMeterType meterType;

    // M-Bus channel
    private final int channel;

    /**
     *
     * @param meterType
     * @param channel
     * @throws IllegalArgumentException if one of the parameters is null
     */
    public DSMRMeterDescriptor(DSMRMeterType meterType, int channel) {
        this.meterType = meterType;
        this.channel = channel;
    }

    /**
     * @return the meterType
     */
    public DSMRMeterType getMeterType() {
        return meterType;
    }

    /**
     * @return the channel
     */
    public Integer getChannel() {
        return channel;
    }

    /**
     * Returns true if both DSMRMeterDescriptor are equal. I.e.:
     * - meterType is the same
     * - channel is the same
     * - identification is the same
     *
     * @param other DSMRMeterDescriptor to check
     * @return true if both objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof DSMRMeterDescriptor)) {
            return false;
        }
        DSMRMeterDescriptor o = (DSMRMeterDescriptor) other;

        return meterType == o.meterType && channel == o.channel;
    }

    @Override
    public String toString() {
        return "Meter type: " + meterType + ", channel: " + channel;
    }
}
