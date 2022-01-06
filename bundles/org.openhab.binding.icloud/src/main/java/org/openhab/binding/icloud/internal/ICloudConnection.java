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
package org.openhab.binding.icloud.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.icloud.internal.json.request.ICloudAccountDataRequest;
import org.openhab.binding.icloud.internal.json.request.ICloudFindMyDeviceRequest;
import org.openhab.core.io.net.http.HttpRequestBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Handles communication with the Apple server. Provides methods to
 * get device information and to find a device.
 *
 * @author Patrik Gfeller - Initial Contribution
 * @author Patrik Gfeller - SOCKET_TIMEOUT changed from 2500 to 10000
 * @author Martin van Wingerden - add support for custom CA of https://fmipmobile.icloud.com
 */
@NonNullByDefault
public class ICloudConnection {
    private static final String ICLOUD_URL = "https://www.icloud.com";
    private static final String ICLOUD_API_BASE_URL = "https://fmipmobile.icloud.com";
    private static final String ICLOUD_API_URL = ICLOUD_API_BASE_URL + "/fmipservice/device/";
    private static final String ICLOUD_API_COMMAND_PING_DEVICE = "/playSound";
    private static final String ICLOUD_API_COMMAND_REQUEST_DATA = "/initClient";
    private static final int SOCKET_TIMEOUT = 15;

    private final Gson gson = new GsonBuilder().create();
    private final String iCloudDataRequest = gson.toJson(ICloudAccountDataRequest.defaultInstance());

    private final String authorization;
    private final String iCloudDataRequestURL;
    private final String iCloudFindMyDeviceURL;

    public ICloudConnection(String appleId, String password) throws URISyntaxException {
        authorization = new String(Base64.getEncoder().encode((appleId + ":" + password).getBytes()), UTF_8);
        iCloudDataRequestURL = new URI(ICLOUD_API_URL + appleId + ICLOUD_API_COMMAND_REQUEST_DATA).toASCIIString();
        iCloudFindMyDeviceURL = new URI(ICLOUD_API_URL + appleId + ICLOUD_API_COMMAND_PING_DEVICE).toASCIIString();
    }

    /***
     * Sends a "find my device" request.
     *
     * @throws IOException
     */
    public void findMyDevice(String id) throws IOException {
        callApi(iCloudFindMyDeviceURL, gson.toJson(new ICloudFindMyDeviceRequest(id)));
    }

    public String requestDeviceStatusJSON() throws IOException {
        return callApi(iCloudDataRequestURL, iCloudDataRequest);
    }

    private String callApi(String url, String payload) throws IOException {
        // @formatter:off
        return HttpRequestBuilder.postTo(url)
            .withTimeout(Duration.ofSeconds(SOCKET_TIMEOUT))
            .withHeader("Authorization", "Basic " + authorization)
            .withHeader("User-Agent", "Find iPhone/1.3 MeKit (iPad: iPhone OS/4.2.1)")
            .withHeader("Origin", ICLOUD_URL)
            .withHeader("charset", "utf-8")
            .withHeader("Accept-language", "en-us")
            .withHeader("Connection", "keep-alive")
            .withHeader("X-Apple-Find-Api-Ver", "2.0")
            .withHeader("X-Apple-Authscheme", "UserIdGuest")
            .withHeader("X-Apple-Realm-Support", "1.0")
            .withHeader("X-Client-Name", "iPad")
            .withHeader("Content-Type", "application/json")
            .withContent(payload)
            .getContentAsString();
        // @formatter:on
    }
}
