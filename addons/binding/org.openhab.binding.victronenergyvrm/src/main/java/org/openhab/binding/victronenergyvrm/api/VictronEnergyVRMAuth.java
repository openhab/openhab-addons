/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.victronenergyvrm.api;

import java.io.IOException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
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

        SslContextFactory sslContextFactory = new SslContextFactory();
        HttpClient httpJettyClient = new HttpClient(sslContextFactory);
        httpJettyClient.setFollowRedirects(false);

        try {

            httpJettyClient.start();
            Request request = httpJettyClient.newRequest(authUrl);
            request.method(HttpMethod.POST);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.content(new StringContentProvider(
                    "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}", "utf-8"));
            ContentResponse response = request.send();
            String res = new String(response.getContent());

            JsonParser jp = new JsonParser();
            JsonObject jo = (JsonObject) jp.parse(res); //

            token = jo.get("token").toString();
            // Entferne " am Anfang und Ende des token
            token = token.substring(1, token.length() - 1);
            // httpClient.getConnectionManager().shutdown();
            httpJettyClient.stop();

        } catch (IOException e) {
            logger.debug(e.toString());
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return token;
    }

}
