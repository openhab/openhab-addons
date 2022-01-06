/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JsonRegisterAppRequest} encapsulate the GSON data of register application request
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonRegisterAppRequest {

    public JsonRegisterAppRequest(String serial, @Nullable String accessToken, String frc,
            List<JsonWebSiteCookie> webSiteCookies) {
        registrationData.deviceSerial = serial;
        authData.accessToken = accessToken;
        userContextMap.frc = frc;
        cookies.webSiteCookies = webSiteCookies;
    }

    @SerializedName("requested_extensions")
    public String[] requestedExtensions = { "device_info", "customer_info" };

    public Cookies cookies = new Cookies();
    @SerializedName("registration_data")
    public RegistrationData registrationData = new RegistrationData();
    @SerializedName("auth_data")
    public AuthData authData = new AuthData();
    @SerializedName("user_context_map")
    public UserContextMap userContextMap = new UserContextMap();
    @SerializedName("requested_token_type")
    public String[] requestedTokenType = { "bearer", "mac_dms", "website_cookies" };

    public static class Cookies {
        @SerializedName("website_cookies")
        public List<JsonWebSiteCookie> webSiteCookies = List.of();
        public @Nullable String domain = ".amazon.com";
    }

    public static class RegistrationData {
        public String domain = "Device";
        @SerializedName("app_version")
        public String appVersion = "2.2.223830.0";
        @SerializedName("device_type")
        public String deviceType = "A2IVLV5VM2W81";
        @SerializedName("device_name")
        public String deviceName = "%FIRST_NAME%'s%DUPE_STRATEGY_1ST%openHAB Alexa Binding";
        @SerializedName("os_version")
        public String osVersion = "11.4.1";
        @SerializedName("device_serial")
        public @Nullable String deviceSerial;
        @SerializedName("device_model")
        public String deviceModel = "iPhone";
        @SerializedName("app_name")
        public String appName = "openHAB Alexa Binding";
        @SerializedName("software_version")
        public String softwareVersion = "1";
    }

    public static class AuthData {
        @SerializedName("access_token")
        public @Nullable String accessToken;
    }

    public static class UserContextMap {
        public String frc = "";
    }
}
