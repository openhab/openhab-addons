/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lightwaverf.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lightwaverf.internal.api.AccessToken;
import org.eclipse.smarthome.io.net.http.HttpUtil;

/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Http {

    private static Properties getHeader(@Nullable String type) {
        Properties headers = new Properties();
        switch (type) {
        case "login":
            headers.put("x-lwrf-appid", "ios-01");
            break;
        case "structures":
        case "structure":
        case "feature":
        case "features":
            headers.put("Authorization", "Bearer " + AccessToken.getToken());
            break;
        }
        return headers;
    }

    private static String url(@Nullable String type, @Nullable String groupId) {
        String url;
        switch (type) {
        case "login":
            url = "https://auth.lightwaverf.com/v2/lightwaverf/autouserlogin/lwapps";
            break;
        case "structures":
            url = "https://publicapi.lightwaverf.com/v1/structures/";
            break;
        case "structure":
            url = "https://publicapi.lightwaverf.com/v1/structure/" + groupId;
            break;
        case "feature":
            url = "https://publicapi.lightwaverf.com/v1/feature/" + groupId;
            break;
        case "features":
            url = "https://publicapi.lightwaverf.com/v1/features/read";
            break;
        default:
            url = "";
        }
        return url;
    }

    public static String method(@Nullable String type) {
        String method;
        switch (type) {
        case "login":
        case "feature":
        case "features":
            method = "POST";
            break;
        case "structures":
        case "structure":
            method = "GET";
            break;
        default:
            method = "GET";
        }
        return method;
    }

    public static String httpClient(@Nullable String type, @Nullable InputStream data, @Nullable String other,
            @Nullable String groupId) throws IOException {
        String response = HttpUtil.executeUrl(method(type), url(type, groupId), getHeader(type), data, other, 100000);
        return response;
    }  
}
