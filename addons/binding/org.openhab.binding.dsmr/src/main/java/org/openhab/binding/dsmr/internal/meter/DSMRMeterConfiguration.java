/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.meter;

import java.util.Objects;

/**
 * This class describes the configuration for a meter.
 *
 * This class is supporting the Configuration.as functionality from {@link Configuration}
 *
 * @author M. Volaart - Initial contribution
 */
public class DSMRMeterConfiguration {
    /**
     * M-Bus channel
     */
    public int channel;

    @Override
    public String toString() {
        return "DSMRMeterConfiguration(channel:" + channel + ")";
    }

    /**
     * Returns if this DSMRMeterConfiguration is equal to the other DSMRMeterConfiguration.
     * Evaluation is done based on all the parameters
     *
     * @param other the other DSMRMeterConfiguration to check
     * @return true if both are equal, false otherwise or if other == null
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DSMRMeterConfiguration)) {
            return false;
        }
        DSMRMeterConfiguration o = (DSMRMeterConfiguration) other;

        return channel == o.channel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel);
    }
}
