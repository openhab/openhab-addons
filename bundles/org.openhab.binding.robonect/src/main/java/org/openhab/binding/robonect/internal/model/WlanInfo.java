/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * WLAN configuration and connection state returned by Robonect.
 *
 * @author Nico Brüttner - Initial contribution
 */
public class WlanInfo extends RobonectAnswer {
    private @Nullable AccessPoint ap;
    private @Nullable WifiStation station;

    /**
     * Returns the access-point information.
     *
     * @return the access-point information, or {@code null} if not reported
     */
    public @Nullable AccessPoint getAp() {
        return ap;
    }

    /**
     * Returns the WLAN station information.
     *
     * @return the WLAN station information, or {@code null} if not reported
     */
    public @Nullable WifiStation getStation() {
        return station;
    }
}
