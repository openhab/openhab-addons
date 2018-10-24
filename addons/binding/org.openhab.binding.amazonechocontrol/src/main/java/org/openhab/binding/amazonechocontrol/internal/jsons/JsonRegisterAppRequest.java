/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/*
 * {
 * "requested_extensions": ["device_info", "customer_info"],
 * "cookies": {
 * "website_cookies": [{
 * "Value":
 * "Atza|IwEBIAfDPDbXQz97tRIKcXrKWNeW1nfEZ73iXH_CZ6T-6YKFTg1JJW4N_xFbMfq4KpV1MJpLUQ35kH4PMIH74LDUm-BGNSNLSX-YR-G-YZDvaO7rQEtWGK3v1KNOgL_JIS5__UdWQUZJt3taHULih1k6dMtfBR5hjnnqP65uvAtzJrot6mdnIX3LJeOhz_grTc0M2jEpSSTVUehTbyzEtt8d4LAoYTEs4v3470BLtOe0WKXfhtV2jtuiMDf5_QkR04XbzYVbJGY8LEZGEv8yJ1L0bAwKdhnhfVpZu9CyOKfsu87Q8_FvuOkTQPxPptu3xfzXDct6TwkrZgjLc0w35pp3w6M9mEe70UfNKlXeZxrKLqH5_A_xzR5ISo7hCMCn_shVA4vqiinrM0oEwQnbSyFQIrx9",
 * "Name": "at-main"
 * }, {
 * "Value": "\"pliBiQhmseXNKmOJXCeo0itE+mDsnKH7kR4NnH5llgU=\"",
 * "Name": "sess-at-main"
 * }, {
 * "Value":
 * "\"QkzsydGBiaH2getHbUS8KaHb94jlDc\/D4ha09+VNFDppznoIbrtRB9ZuN6VtXH4BE4YpyID+DTEx4q4zfBpB7h3dACyWRj4ZMt2dcOlcBjGju80hpqIlUjwuEnKRU7UlCPSR4OrshJ\/yG12vWj9Jm+nq3qPhdnKU7FJv+gQ7KAA489JuoiUth2J5Ck8R4sC9H6FavvVVgpom8+GzIzv8ZpKTkzNZfZ2rVKonPfNdyu0=\"",
 * "Name": "session-token"
 * }, {
 * "Value": "133-5253956-5626844",
 * "Name": "ubid-main"
 * }, {
 * "Value": "\"T32E@M3jTQZZzZpZmNMTNRvaLZo8ymw4\"",
 * "Name": "x-main"
 * }, {
 * "Value": "adb:adblk_no&tb:XG484NSWB82BMZM8ZEKJ+s-GR0JJKJ8TC6VZQQTXMTZ|1535123633255",
 * "Name": "csm-hit"
 * }, {
 * "Value": "de_AT",
 * "Name": "lc-main"
 * }, {
 * "Value": "143-5615183-2731227",
 * "Name": "session-id"
 * }, {
 * "Value": "2165843037l",
 * "Name": "session-id-time"
 * }],
 * "domain": ".amazon.com"
 * },
 * "registration_data": {
 * "domain": "Device",
 * "app_version": "2.2.223830.0",
 * "device_type": "A2IVLV5VM2W81",
 * "device_name": "%FIRST_NAME%'s%DUPE_STRATEGY_1ST%Amazon Alexa on iOS",
 * "os_version": "11.4.1",
 * "device_serial": "9FB4AB760C4D48D795C883CAF551C52C",
 * "device_model": "iPhone",
 * "app_name": "Amazon Alexa",
 * "software_version": "1"
 * },
 * "auth_data": {
 * "access_token":
 * "Atna|EwICILRJJ-qbYkp6hBS3jh5qy3em3tvRDOsahbTmMBGVtq_lCl47vKUOUHxff1yvA6PRT7fjHi1atJUcfi3O_HYjIdSLqse1d7jL7rrhxZ7hqlxnIQ1D3hwN35oZwvTe44cAoqNXCWouVCCc-VXEtBOe74MgSKc2kd5OsjwscOjQ4u-JBZoCfZuhZeS2b-QEbcAXAJ33uTRIVP2qU2e5-KuP622B7VI2Fl9YRXS9IbOHhTBBnV2hV1ve4lqk2nj15sTKAqS8DA3tnz9Egny0PERyuf8wjnO_NAoRzmJKdA8h1U2wXunc4g-E3K0qGabpYXYhYAN6MN4HWLJ5w0G9X5d56YOS"
 * },
 * "user_context_map": {
 * "frc":
 * "AJLt12uShLU7cAFTr01RnWRgLbR2FmZ9G7hkymFBZUiyNC28Td+T01a+2l4WPMs7n0CW55mfOAgtj\/\/3tZtTgD+0eLlqXGhfHON2G2qm0fjME3BuprtnxugT3QHuiGkoTfKBx\/4Hd1+9pmSaM6BX4QA5TOnnG65aq8UL5kXDdtE1Q4RauwAyurqnJes8Fm6seJKmNRCVu4G+22f9wkPGEJ26bMDr1NA0tiQf6a+nTgRUF03FLqH3lnNcsXllWjl1WOSBx5ZqY0ooW3c0I7Qs\/Qbz5Rwyzv5zIYf8l+IKXu2\/2FhUTVXXB\/2\/QzXTnJgmjTCaXy+q\/SeLXx3S9BWJ8lPfSX7Ib4V4+dZuQBu\/OEQ+A\/A820aUIVO1GyVCDqQq9Qza48WUwqiE5E2yBqFjrko2Z3F8z\/o3Cw=="
 * },
 * "requested_token_type": ["bearer", "mac_dms", "website_cookies"]
 * }
 */
