/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.icloud.internal.json.request.ICloudAccountDataRequest;
import org.openhab.binding.icloud.internal.json.request.ICloudFindMyDeviceRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Handles communication with the Apple server. Provides methods to
 * get device information and to find a device.
 *
 * @author Patrik Gfeller - Initial Contribution
 */
public class ICloudConnection {
    private static final String ICLOUD_URL = "https://www.icloud.com";
    private static final String ICLOUD_API_URL = "https://fmipmobile.icloud.com/fmipservice/device/";
    private static final String ICLOUD_API_COMMAND_PING_DEVICE = "/playSound";
    private static final String ICLOUD_API_COMMAND_REQUEST_DATA = "/initClient";
    private static final int SOCKET_TIMEOUT = 2500;

    private final Gson gson = new GsonBuilder().create();
    private final String iCloudDataRequest = gson.toJson(ICloudAccountDataRequest.defaultInstance());

    private final Properties httpHeader;
    private final URL iCloudDataRequestURL;
    private final URL iCloudFindMyDeviceURL;

    public ICloudConnection(String appleId, String password)
            throws MalformedURLException, UnsupportedEncodingException {
        byte[] authorization = Base64.getEncoder().encode((appleId + ":" + password).getBytes());
        iCloudDataRequestURL = new URL(ICLOUD_API_URL + appleId + ICLOUD_API_COMMAND_REQUEST_DATA);
        iCloudFindMyDeviceURL = new URL(ICLOUD_API_URL + appleId + ICLOUD_API_COMMAND_PING_DEVICE);
        httpHeader = createHttpHeader(authorization);
    }

    /***
     * Sends a "find my device" request.
     *
     * @throws IOException
     */
    public void findMyDevice(String id) throws IOException {
        String iCloudFindMyDeviceRequest = gson.toJson(new ICloudFindMyDeviceRequest(id));
        HttpUtil.executeUrl("POST", iCloudFindMyDeviceURL.toString(), httpHeader,
                new ByteArrayInputStream(iCloudFindMyDeviceRequest.getBytes("UTF-8")), "application/json",
                SOCKET_TIMEOUT);
    }

    public String requestDeviceStatusJSON() throws IOException {
        return HttpUtil.executeUrl("POST", iCloudDataRequestURL.toString(), httpHeader,
                new ByteArrayInputStream(iCloudDataRequest.getBytes("UTF-8")), "application/json", SOCKET_TIMEOUT);
    }

    private Properties createHttpHeader(byte[] authorization) throws UnsupportedEncodingException {
        Properties httpHeader = new Properties();

        httpHeader.setProperty("Authorization", "Basic " + new String(authorization, "UTF-8"));
        httpHeader.setProperty("User-Agent", "Find iPhone/1.3 MeKit (iPad: iPhone OS/4.2.1)");
        httpHeader.setProperty("Origin", ICLOUD_URL);
        httpHeader.setProperty("charset", "utf-8");
        httpHeader.setProperty("Accept-language", "en-us");
        httpHeader.setProperty("Connection", "keep-alive");
        httpHeader.setProperty("X-Apple-Find-Api-Ver", "2.0");
        httpHeader.setProperty("X-Apple-Authscheme", "UserIdGuest");
        httpHeader.setProperty("X-Apple-Realm-Support", "1.0");
        httpHeader.setProperty("X-Client-Name", "iPad");

        return httpHeader;
    }
}
