/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * WLAN configuration and connection state returned by Robonect.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
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
