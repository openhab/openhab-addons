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
package org.openhab.binding.peblar.internal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
class PeblarSystemDTO {

    public String productPn;
    public String productSn;
    public String firmwareVersion;

    /** WLAN signal strength in dBm */
    @SerializedName("WLANSignalStrength")
    public Integer wlanSignalStrength;

    /** Cellular (LTE) signal strength in dBm */
    public Integer cellularSignalStrength;

    /** Device uptime in seconds */
    public Long uptime;

    /** Number of phases: 1 or 3 */
    public Integer phaseCount;
}
