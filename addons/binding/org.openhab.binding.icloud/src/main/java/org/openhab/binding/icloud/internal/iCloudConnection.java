/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/**
 *
 */
package org.openhab.binding.icloud.internal;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Patrik Gfeller
 *
 */
public class iCloudConnection {
    private final String iCloudURL = "https://www.icloud.com";
    private final String iCloudApiURL = "https://fmipmobile.icloud.com/fmipservice/device/";
    private final String iCloudAPIRequestDataCommand = "/initClient";
    private final String iCloudAPIPingDeviceCommand = "/playSound";

    private final String dataRequest = "{\"clientContext\":{\"appName\":\"iCloud Find (Web)\",\"appVersion\":\"2.0\",\"timezone\":\"US/Eastern\",\"inactiveTime\":2255,\"apiVersion\":\"3.0\",\"webStats\":\"0:15\"}}\n";

    private final byte[] authorization;
    private URL iCloudDataRequestURL;
    private URL iCloudPingDeviceURL;

    public iCloudConnection(String appleId, String password) throws MalformedURLException {
        authorization = Base64.getEncoder().encode((appleId + ":" + password).getBytes());
        iCloudDataRequestURL = new URL(iCloudApiURL + appleId + iCloudAPIRequestDataCommand);
        iCloudPingDeviceURL = new URL(iCloudApiURL + appleId + iCloudAPIPingDeviceCommand);
    }

    public String requestDeviceStatusJSON() throws Exception {
        HttpsURLConnection connection = postRequest(iCloudDataRequestURL, dataRequest);

        String response = getResponse(connection);
        return response;
    }

    /***
     * Sends a ping request (find my phone).
     *
     * @throws Exception
     */
    public void pingPhone(String id) throws Exception {
        String request = "{ \n \"device\": \"" + id + "\", \n \"subject\": \"Find My Device alert\" \n }";
        getResponse(postRequest(iCloudPingDeviceURL, request));
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

    private HttpsURLConnection postRequest(URL url, String payload) throws Exception {
        HttpsURLConnection connection;
        connection = (HttpsURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        setRequestProperties(connection, payload);

        connection.connect();
        connection.getOutputStream().write(payload.getBytes("UTF-8"));
        connection.disconnect();
        return connection;
    }

    private String getResponse(HttpsURLConnection connection) throws IOException {
        String response;
        BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String inputLine;
        StringBuffer stringBuffer = new StringBuffer();
        while ((inputLine = reader.readLine()) != null) {
            stringBuffer.append(inputLine);
        }

        reader.close();
        inputStream.close();
        response = stringBuffer.toString();
        return response;
    }

    private String getBasicAuthorization() throws UnsupportedEncodingException {
        return "Basic " + new String(this.authorization, "UTF-8"); // for UTF-8 encoding
    }

}
