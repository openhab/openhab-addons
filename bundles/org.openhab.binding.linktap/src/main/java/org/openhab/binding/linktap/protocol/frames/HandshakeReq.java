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
 * The {@link HandshakeReq} is a handshake from the Gateway.
 *
 * @provides App: Gateway ID, Firmware revision, Registered / Addded Endpoint Device ID List
 * @response Gw: Expects response of HandshakeResp, to inform the Gateway of the current local Date and Time
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class HandshakeReq extends GatewayEndDevListReq {

    public HandshakeReq() {
    }

    /**
     * Defines the firmware version identifier.
     */
    @SerializedName("ver")
    @Expose
    public String version = EMPTY_STRING;

    public String isValid() {
        final String superRst = super.isValid();
        if (!superRst.isEmpty()) {
            return superRst;
        }

        if (version.isEmpty()) {
            return "Version is empty";
        }
        return EMPTY_STRING;
    }
}
