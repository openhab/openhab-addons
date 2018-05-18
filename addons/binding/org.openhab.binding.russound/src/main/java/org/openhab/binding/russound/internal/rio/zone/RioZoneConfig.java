/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.zone;

/**
 * Configuration class for the {@link RioZoneHandler}
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioZoneConfig {
    /**
     * Constant defined for the "zone" configuration field
     */
    public static final String ZONE = "zone";

    /**
     * ID of the zone
     */
    private int zone;

    /**
     * Gets the zone identifier
     *
     * @return the zone identifier
     */
    public int getZone() {
        return zone;
    }

    /**
     * Sets the zone identifier
     *
     * @param zoneId the zone identifier
     */
    public void setZone(int zoneId) {
        this.zone = zoneId;
    }
}
