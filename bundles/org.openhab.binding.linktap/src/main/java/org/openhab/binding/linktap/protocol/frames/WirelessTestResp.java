/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.linktap.protocol.frames;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link WirelessTestResp} defines the wireless test result data response
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class WirelessTestResp extends EndpointDeviceResponse {

    public WirelessTestResp() {
    }

    /**
     * Defines true if the last packet has been transmitted and
     * therefore the test is complete
     */
    @SerializedName("final")
    @Expose
    public boolean testComplete = false;

    /**
     * Defines how many pings have been sent to the endpoint device
     */
    @SerializedName("ping")
    @Expose
    public int pingCount = DEFAULT_INT;

    /**
     * Defines how many pongs have been received from the endpoint device
     */
    @SerializedName("pong")
    @Expose
    public int pongCount = DEFAULT_INT;

    public String isValid() {
        final String superRst = super.isValid();
        if (!superRst.isEmpty()) {
            return superRst;
        }
        if (pingCount == DEFAULT_INT) {
            return "pingCount invalid";
        }

        if (pongCount == DEFAULT_INT) {
            return "pongCount invalid";
        }

        if (!DEVICE_ID_PATTERN.matcher(deviceId).matches()) {
            return "DeviceId invalid";
        }

        return EMPTY_STRING;
    }
}
