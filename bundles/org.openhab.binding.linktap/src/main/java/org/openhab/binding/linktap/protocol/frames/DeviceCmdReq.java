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
 * The {@link DeviceCmdReq} is a request targetted to a device.
 *
 * @provides App: Device ID
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class DeviceCmdReq extends TLGatewayFrame {

    public DeviceCmdReq() {
    }

    public DeviceCmdReq(final int command) {
        this.command = command;
    }

    /**
     * Defines the targetted device ID
     */
    @SerializedName("dev_id")
    @Expose
    public String deviceId = EMPTY_STRING;

    public String isValid() {
        final String superRst = super.isValid();
        if (!superRst.isEmpty()) {
            return superRst;
        }

        if (!DEVICE_ID_PATTERN.matcher(deviceId).matches() && !SUB_DEVICE_ID_PATTERN.matcher(deviceId).matches()) {
            return "DeviceId invalid";
        }

        return EMPTY_STRING;
    }
}
