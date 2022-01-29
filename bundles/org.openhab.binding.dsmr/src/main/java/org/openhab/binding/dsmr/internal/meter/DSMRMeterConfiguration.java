/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.meter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class describes the configuration for a meter.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Added refresh field
 */
@NonNullByDefault
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
