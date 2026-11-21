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
package org.openhab.binding.bmwcardata.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Device authorization response as defined by RFC 8628, section 3.2.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class DeviceCodeResponse {
    @SerializedName("device_code")
    public @Nullable String deviceCode;
    @SerializedName("user_code")
    public @Nullable String userCode;
    @SerializedName("verification_uri")
    public @Nullable String verificationUri;
    @SerializedName("expires_in")
    public int expiresIn;
    public int interval;
}
