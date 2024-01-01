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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JsonRegisterAppResponse} encapsulate the GSON data of response from the register command
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonRegisterAppResponse {

    public @Nullable Response response;

    @SerializedName("request_id")
    public @Nullable String requestId;

    public static class Response {
        public @Nullable Success success;
    }

    public static class Success {
        public @Nullable Extensions extensions;

        public @Nullable Tokens tokens;

        @SerializedName("customer_id")
        public @Nullable String customerId;
    }

    public static class Extensions {
        @SerializedName("device_info")
        public @Nullable DeviceInfo deviceInfo;

        @SerializedName("customer_info")
        public @Nullable CustomerInfo customerInfo;

        @SerializedName("customer_id")
        public @Nullable String customerId;
    }

    public static class DeviceInfo {
        @SerializedName("device_name")
        public @Nullable String deviceName;

        @SerializedName("device_serial_number")
        public @Nullable String deviceSerialNumber;

        @SerializedName("device_type")
        public @Nullable String deviceType;
    }

    public static class CustomerInfo {
        @SerializedName("account_pool")
        public @Nullable String accountPool;

        @SerializedName("user_id")
        public @Nullable String userId;

        @SerializedName("home_region")
        public @Nullable String homeRegion;

        public @Nullable String name;

        @SerializedName("given_name")
        public @Nullable String givenName;
    }

    public static class Tokens {
        @SerializedName("website_cookies")
        public @Nullable Object websiteCookies;

        @SerializedName("mac_dms")
        public @Nullable MacDms macDms;

        public @Nullable Bearer bearer;
    }

    public static class MacDms {
        @SerializedName("device_private_key")
        public @Nullable String devicePrivateKey;

        @SerializedName("adp_token")
        public @Nullable String adpToken;
    }

    public static class Bearer {
        @SerializedName("access_token")
        public @Nullable String accessToken;

        @SerializedName("refresh_token")
        public @Nullable String refreshToken;

        @SerializedName("expires_in")
        public @Nullable String expiresIn;
    }
}
