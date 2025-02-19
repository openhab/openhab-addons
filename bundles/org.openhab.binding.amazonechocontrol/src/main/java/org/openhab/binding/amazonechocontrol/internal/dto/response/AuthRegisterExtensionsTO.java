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
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthRegisterExtensionsTO} encapsulates the extension part of an app registration response
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterExtensionsTO {
    @SerializedName("device_info")
    public AuthRegisterDeviceInfoTO deviceInfo = new AuthRegisterDeviceInfoTO();
    @SerializedName("customer_info")
    public AuthRegisterCustomerInfoTO customerInfo = new AuthRegisterCustomerInfoTO();
    @SerializedName("customer_id")
    public @Nullable String customerId;

    @Override
    public @NonNull String toString() {
        return "AuthRegisterExtensions" + "TO{deviceInfo=" + deviceInfo + ", customerInfo=" + customerInfo
                + ", customerId='" + customerId + "'}";
    }
}
