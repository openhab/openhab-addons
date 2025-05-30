/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthRegisterDeviceInfoTO} encapsulates the device information of an app registration response
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterDeviceInfoTO {
    @SerializedName("device_name")
    public String deviceName = "Unknown";
    @SerializedName("device_serial_number")
    public String deviceSerialNumber;
    @SerializedName("device_type")
    public String deviceType;

    @Override
    public @NonNull String toString() {
        return "AuthRegisterDeviceInfoTO{deviceName='" + deviceName + "', deviceSerialNumber='" + deviceSerialNumber
                + "', deviceType='" + deviceType + "'}";
    }
}
