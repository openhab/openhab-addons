/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

/**
 * The {@link JsonRegisterApp} encapsulate the GSON data of register application request
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonRegisterAppRequest {

    public JsonRegisterAppRequest(String serial, String access_token, String frc, JsonWebSiteCookie[] webSiteCookies) {
        registration_data.device_serial = serial;
        auth_data.access_token = access_token;
        user_context_map.frc = frc;
        cookies.website_cookies = webSiteCookies;
    }

    public String[] requested_extensions = { "device_info", "customer_info" };

    public Cookies cookies = new Cookies();
    public RegistrationData registration_data = new RegistrationData();
    public AuthData auth_data = new AuthData();
    public UserContextMap user_context_map = new UserContextMap();
    public String[] requested_token_type = { "bearer", "mac_dms", "website_cookies" };

    public class Cookies {
        @Nullable
        public JsonWebSiteCookie @Nullable [] website_cookies;
        @Nullable
        public String domain = ".amazon.com";

    }

    public class RegistrationData {
        public String domain = "Device";
        public String app_version = "2.2.223830.0";
        public String device_type = "A2IVLV5VM2W81";
        public String device_name = "%FIRST_NAME%'s%DUPE_STRATEGY_1ST%Open HAB Alexa Binding";
        public String os_version = "11.4.1";
        @Nullable
        public String device_serial;
        public String device_model = "iPhone";
        public String app_name = "Open HAB Alexa Binding";// Amazon Alexa";
        public String software_version = "1";
    }

    public class AuthData {
        @Nullable
        public String access_token;
    }

    public class UserContextMap {
        public String frc = "";
    }
}
