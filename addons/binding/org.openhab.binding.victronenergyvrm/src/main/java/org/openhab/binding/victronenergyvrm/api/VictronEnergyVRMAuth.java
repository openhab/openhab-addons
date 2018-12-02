/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.victronenergyvrm.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handle Authorization against VRM API
 *
 *
 * @author Samuel Lueckoff
 */

public class VictronEnergyVRMAuth {
    private final Logger logger = LoggerFactory.getLogger(VictronEnergyVRMSolarChargerSummery.class);
    private String baseUrl;
    private String token;

    public VictronEnergyVRMAuth(String baseUrl) {
        // Constructor
        this.baseUrl = baseUrl;
        this.token = null;
    }

    public String GetToken(String username, String password) {

        String authUrl = baseUrl + "auth/login";

        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {
            HttpPost postRequest = new HttpPost(authUrl);
            postRequest
                    .setEntity(new StringEntity("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}",
                            ContentType.create("application/json")));

            HttpResponse httpResponse = httpClient.execute(postRequest);
            logger.debug("httpResponse: " + httpResponse.getStatusLine().toString());
            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            StringBuilder bd = new StringBuilder();
            for (String line = null; (line = rd.readLine()) != null;) {
                bd.append(line).append("\n");
            }
            JsonParser jp = new JsonParser();
            JsonObject jo = (JsonObject) jp.parse(bd.toString());

            token = jo.get("token").toString();
            // Entferne " am Anfang und Ende des token
            token = token.substring(1, token.length() - 1);
            httpClient.getConnectionManager().shutdown();

        } catch (ParseException e) {
            logger.error(e.toString());
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return token;
    }

}
