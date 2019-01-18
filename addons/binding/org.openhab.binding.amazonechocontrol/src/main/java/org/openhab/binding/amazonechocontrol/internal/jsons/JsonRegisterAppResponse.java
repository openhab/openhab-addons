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
 * The {@link JsonRegisterAppResponse} encapsulate the GSON data of response from the register command
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonRegisterAppResponse {

    @Nullable
    public Response response;

    @Nullable
    public String request_id;

    public class Response {
        @Nullable
        public Success success;
    }

    public class Success {
        @Nullable
        public Extensions extensions;
        @Nullable
        public Tokens tokens;
    }

    public class Extensions {
        @Nullable
        public DeviceInfo device_info;
        @Nullable
        public CustomerInfo customer_info;
        @Nullable
        public String customer_id;
    }

    public class DeviceInfo {
        @Nullable
        public String device_name;
        @Nullable
        public String device_serial_number;
        @Nullable
        public String device_type;

    }

    public class CustomerInfo {
        @Nullable
        public String account_pool;
        @Nullable
        public String user_id;
        @Nullable
        public String home_region;
        @Nullable
        public String name;
        @Nullable
        public String given_name;
    }

    public class Tokens {
        @Nullable
        public Object website_cookies;
        @Nullable
        public MacDms mac_dms;
        @Nullable
        public Bearer bearer;
    }

    public class MacDms {
        @Nullable
        public String device_private_key;
        @Nullable
        public String adp_token;
    }

    public class Bearer {
        @Nullable
        public String access_token;
        @Nullable
        public String refresh_token;
        @Nullable
        public String expires_in;
    }
}
