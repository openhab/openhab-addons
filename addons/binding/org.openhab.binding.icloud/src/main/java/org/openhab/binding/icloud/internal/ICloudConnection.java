/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.icloud.internal.json.request.ICloudAccountDataRequest;
import org.openhab.binding.icloud.internal.json.request.ICloudFindMyDeviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ICloudConnection {
    private final Logger logger = LoggerFactory.getLogger(ICloudConnection.class);

    private static final String ICLOUD_URL = "https://www.icloud.com";
    private static final String ICLOUD_API_BASE_URL = "https://fmipmobile.icloud.com";
    private static final String ICLOUD_API_URL = ICLOUD_API_BASE_URL + "/fmipservice/device/";
    private static final String ICLOUD_API_COMMAND_PING_DEVICE = "/playSound";
    private static final String ICLOUD_API_COMMAND_REQUEST_DATA = "/initClient";
    private static final int SOCKET_TIMEOUT = 10;

    private final Gson gson = new GsonBuilder().create();
    private final String iCloudDataRequest = gson.toJson(ICloudAccountDataRequest.defaultInstance());

    private final HttpClient httpClient;
    private final String authorization;
    private final URI iCloudDataRequestURL;
    private final URI iCloudFindMyDeviceURL;

    public ICloudConnection(HttpClientFactory httpClientFactory, String appleId, String password)
            throws UnsupportedEncodingException, URISyntaxException {
        httpClient = httpClientFactory.createHttpClient("icloud", ICLOUD_API_BASE_URL);
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start the client", e);
        }
        authorization = new String(Base64.getEncoder().encode((appleId + ":" + password).getBytes()), "UTF-8");
        iCloudDataRequestURL = new URI(ICLOUD_API_URL + appleId + ICLOUD_API_COMMAND_REQUEST_DATA);
        iCloudFindMyDeviceURL = new URI(ICLOUD_API_URL + appleId + ICLOUD_API_COMMAND_PING_DEVICE);
    }

    public void disconnect() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Failed stopping the http client", e);
        }
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
        ContentResponse response = callApi(iCloudDataRequestURL, iCloudDataRequest);
        return new String(response.getContent(), StandardCharsets.UTF_8);
    }

    private ContentResponse callApi(URI url, String payload) throws IOException {
        try {
            // @formatter:off
            return httpClient.newRequest(url)
                    .method(HttpMethod.POST)
                    .header("Authorization", "Basic " + authorization)
                    .header("User-Agent", "Find iPhone/1.3 MeKit (iPad: iPhone OS/4.2.1)")
                    .header("Origin", ICLOUD_URL)
                    .header("charset", "utf-8")
                    .header("Accept-language", "en-us")
                    .header("Connection", "keep-alive")
                    .header("X-Apple-Find-Api-Ver", "2.0")
                    .header("X-Apple-Authscheme", "UserIdGuest")
                    .header("X-Apple-Realm-Support", "1.0")
                    .header("X-Client-Name", "iPad")
                    .timeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
                    .content(new StringContentProvider(payload), "application/json")
                    .send();
            // @formatter:on
        } catch (ExecutionException e) {
            throw new IOException("Problem while calling the API", e.getCause());
        } catch (InterruptedException | TimeoutException e) {
            throw new IOException("Failed to respond in time", e);
        }
    }
}
