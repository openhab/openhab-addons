/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.meter;

/**
 * This class describes the configuration for a meter.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Added refresh field
 */
public class DSMRMeterConfiguration {
    /**
     * M-Bus channel
     */
    public int channel;

    /**
     * Status update rate as specified by the user in seconds.
     */
    public int refresh;

    @Override
    public String toString() {
        return "DSMRMeterConfiguration(channel:" + channel + ",refresh=" + refresh + ")";
    }
}
