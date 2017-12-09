/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communication with the apple server. Provides methods to
 * get device information and to find a device.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class Connection {
    private final Logger logger = LoggerFactory.getLogger(Connection.class);
    private final String iCloudURL = "https://www.icloud.com";
    private final String iCloudApiURL = "https://fmipmobile.icloud.com/fmipservice/device/";
    private final String iCloudAPIRequestDataCommand = "/initClient";
    private final String iCloudAPIPingDeviceCommand = "/playSound";

    private final String dataRequest = "{\"clientContext\":{\"appName\":\"iCloud Find (Web)\",\"fmly\": true,\"appVersion\":\"2.0\",\"timezone\":\"US/Eastern\",\"inactiveTime\":2255,\"apiVersion\":\"3.0\",\"webStats\":\"0:15\"}}\n";

    private final byte[] authorization;
    private URL iCloudDataRequestURL;
    private URL iCloudFindMyDeviceURL;

    public Connection(String appleId, String password) throws MalformedURLException {
        authorization = Base64.getEncoder().encode((appleId + ":" + password).getBytes());
        iCloudDataRequestURL = new URL(iCloudApiURL + appleId + iCloudAPIRequestDataCommand);
        iCloudFindMyDeviceURL = new URL(iCloudApiURL + appleId + iCloudAPIPingDeviceCommand);
    }

    public String requestDeviceStatusJSON() throws Exception {
        HttpsURLConnection connection = connect(iCloudDataRequestURL);
        String response = postRequest(connection, dataRequest);
        connection.disconnect();

        return response;
    }

    /***
     * Sends a "find my device" request.
     *
     * @throws Exception
     */
    public void findMyDevice(String id) throws Exception {
        String request = "{ \n \"device\": \"" + id + "\", \n \"subject\": \"Find My Device alert\" \n }";
        HttpsURLConnection connection = connect(iCloudFindMyDeviceURL);
        postRequest(connection, request);
        connection.disconnect();
    }

    private void setRequestProperties(HttpsURLConnection connection, String payload)
            throws UnsupportedEncodingException {
        connection.setRequestProperty("Authorization", this.getBasicAuthorization());
        connection.setRequestProperty("User-Agent", "Find iPhone/1.3 MeKit (iPad: iPhone OS/4.2.1)");
        connection.setRequestProperty("Origin", iCloudURL);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Accept-language", "en-us");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("X-Apple-Find-Api-Ver", "2.0");
        connection.setRequestProperty("X-Apple-Authscheme", "UserIdGuest");
        connection.setRequestProperty("X-Apple-Realm-Support", "1.0");
        connection.setRequestProperty("X-Client-Name", "iPad");
        connection.setRequestProperty("Content-Length", Integer.toString(payload.getBytes("UTF-8").length));
    }

    private String postRequest(HttpsURLConnection connection, String payload) throws Exception {
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        setRequestProperties(connection, payload);

        connection.getOutputStream().write(payload.getBytes("UTF-8"));

        return getResponse(connection);
    }

    private HttpsURLConnection connect(URL url) throws IOException {
        HttpsURLConnection connection;
        connection = (HttpsURLConnection) url.openConnection();
        return connection;
    }

    private String getResponse(HttpsURLConnection connection) {
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

                String inputLine;
                StringBuffer stringBuffer = new StringBuffer();
                while ((inputLine = reader.readLine()) != null) {
                    stringBuffer.append(inputLine);
                }

                return stringBuffer.toString();
            }
        } catch (IOException e) {
            logger.warn("Communication problem with icloud", e);
        }
        return null;
    }

    private String getBasicAuthorization() {
        try {
            return "Basic " + new String(this.authorization, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.warn("Unsupported encoding); unable to create basic authorization string", e);
        }

        return null;
    }

}
