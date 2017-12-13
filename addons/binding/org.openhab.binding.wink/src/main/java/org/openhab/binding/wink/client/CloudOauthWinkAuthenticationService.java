/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.client;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This WinkAuthenticationService requires that the user apply for and receive their own
 * wink app id and appropriate client secret. It Also requires that they must perform
 * a little authentication and get their initial access token and refresh token. Initial access
 * token can be fudged and the client will actually perform and persist a refresh as necessary.
 *
 * In order to work, you must put a wink.cfg file in your $openhab/conf/services folder with the
 * following parameters defined
 *
 * ---wink.cfg---
 * client_id=wink_app_client_id_lakjdsflakjdf
 * client_secret=wink_app_client_secret_laskdjflakjsdf
 * refresh_token=refresh_token_from_external_auth_call_aldjflakjsdoie
 * ---end---
 *
 * @author Shawn Crosby (sacrosby@gmail.com)
 *
 */
public class CloudOauthWinkAuthenticationService implements IWinkAuthenticationService {
    private static final String ACCESS_TOKEN = "access_token";

    private final Logger logger = LoggerFactory.getLogger(CloudOauthWinkAuthenticationService.class);

    private String token;
    private String clientId;
    private String clientSecret;
    private String refresh_token;

    public CloudOauthWinkAuthenticationService(Map<String, String> properties) {
        clientId = properties.get("client_id");
        clientSecret = properties.get("client_secret");
        refresh_token = properties.get("refresh_token");
    }

    @Override
    public String getAuthToken() {
        return token;
    }

    @Override
    public String refreshToken() throws AuthenticationException {
        logger.debug("Refreshing token for client id {}", clientId);
        Client winkClient = ClientBuilder.newClient();
        WebTarget target = winkClient.target("https://api.wink.com");
        WebTarget tokenTarget = target.path("/oauth2/token");

        Map<String, String> payload = new HashMap<String, String>();
        payload.put("client_id", clientId);
        payload.put("client_secret", clientSecret);
        payload.put("grant_type", "refresh_token");
        payload.put("refresh_token", refresh_token);

        String json = new Gson().toJson(payload);

        Response response = tokenTarget.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(json));
        JsonObject responseJson = getResultAsJson(response);

        if (response.getStatus() == 200) {
            token = responseJson.get("data").getAsJsonObject().get(ACCESS_TOKEN).getAsString();
            logger.debug("New Access Token: {}", token);
        } else {
            logger.debug("Got status: {} refreshing token", response.getStatus());
            logger.trace("Error Response: {}", responseJson.get("errors").getAsString());
            throw new AuthenticationException("Invalid refresh token or app key and secret");
        }

        winkClient.close();

        return token;
    }

    private JsonObject getResultAsJson(Response response) {
        String result = response.readEntity(String.class);
        JsonParser parser = new JsonParser();
        JsonObject resultJson = parser.parse(result).getAsJsonObject();
        return resultJson;
    }
}
