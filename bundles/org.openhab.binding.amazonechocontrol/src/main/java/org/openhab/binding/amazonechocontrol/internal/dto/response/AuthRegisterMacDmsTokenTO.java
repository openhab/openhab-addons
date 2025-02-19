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
 * The {@link AuthRegisterMacDmsTokenTO} encapsulates MAC dms tokens
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterMacDmsTokenTO {
    @SerializedName("device_private_key")
    public String devicePrivateKey;

    @SerializedName("adp_token")
    public String adpToken;

    @Override
    public @NonNull String toString() {
        return "AuthRegisterMacDmsTokenTO{devicePrivateKey='" + devicePrivateKey + "', adpToken='" + adpToken + "'}";
    }
}
