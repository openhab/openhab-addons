/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/** WLAN configuration and connection state returned by Robonect. */
public class WlanInfo extends RobonectAnswer {
    private @Nullable AccessPoint ap;
    private @Nullable WifiStation station;

    public @Nullable AccessPoint getAp() {
        return ap;
    }

    public @Nullable WifiStation getStation() {
        return station;
    }
}
