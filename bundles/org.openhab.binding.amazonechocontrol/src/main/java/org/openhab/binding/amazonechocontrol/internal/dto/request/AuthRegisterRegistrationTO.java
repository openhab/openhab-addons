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
package org.openhab.binding.amazonechocontrol.internal.dto.request;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.API_VERSION;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DI_OS_VERSION;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthRegisterRegistrationTO} encapsulates the registration data for an app registration request
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterRegistrationTO {
    public String domain = "Device";
    @SerializedName("app_version")
    public String appVersion = API_VERSION;
    @SerializedName("device_type")
    public String deviceType = "A2IVLV5VM2W81";
    @SerializedName("device_name")
    public String deviceName = "%FIRST_NAME%'s%DUPE_STRATEGY_1ST%openHAB Alexa Binding";
    @SerializedName("os_version")
    public String osVersion = DI_OS_VERSION;
    @SerializedName("device_serial")
    public @Nullable String deviceSerial;
    @SerializedName("device_model")
    public String deviceModel = "iPhone";
    @SerializedName("app_name")
    public String appName = "openHAB Alexa Binding";
    @SerializedName("software_version")
    public String softwareVersion = "1";

    @Override
    public @NonNull String toString() {
        return "AuthRegisterRegistrationTO{domain='" + domain + "', appVersion='" + appVersion + "', deviceType='"
                + deviceType + "', deviceName='" + deviceName + "', osVersion='" + osVersion + "', deviceSerial='"
                + deviceSerial + "', deviceModel='" + deviceModel + "', appName='" + appName + "', softwareVersion='"
                + softwareVersion + "'}";
    }
}
