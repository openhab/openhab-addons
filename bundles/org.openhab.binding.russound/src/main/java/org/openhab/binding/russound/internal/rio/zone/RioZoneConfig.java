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
