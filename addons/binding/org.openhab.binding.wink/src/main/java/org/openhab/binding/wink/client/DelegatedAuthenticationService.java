/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.client;

import static org.openhab.binding.wink.WinkBindingConstants.DELEGATED_AUTH_SERVICE;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Uses a heroku app to manage access tokens, client ids and secrets for wink service. Removes the need for users
 * to apply and use their own wink api app.
 *
 * To use, create the following wink.cfg
 *
 * auth_service = delegated
 * auth_service_token = token_from_auth_service
 *
 * The authentication service is hosted in Heroku and owned current by Shawn Crosby (sacrosby@gmail.com) and
 * based on the python-social-auth django app.
 *
 * @author Shawn Crosby
 *
 */
public class DelegatedAuthenticationService implements IWinkAuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(DelegatedAuthenticationService.class);

    private String auth_token;
    private String token;

    public DelegatedAuthenticationService(String auth_token) {
        this.auth_token = auth_token;
        ClientConfig configuration = new ClientConfig();
        configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, 1000 * 15);
        configuration = configuration.property(ClientProperties.READ_TIMEOUT, 1000 * 15);
        Client client = ClientBuilder.newBuilder().withConfig(configuration).build();
        WebTarget target = client.target(DELEGATED_AUTH_SERVICE);
        WebTarget tokenPath = target.path("/token");
        Response response = tokenPath.request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Token " + this.auth_token).get();
        JsonElement json = getResultAsJson(response);
        logger.debug("Access Token Response: {}", json);
        if (json.getAsJsonObject().get("access_token") != null) {
            token = json.getAsJsonObject().get("access_token").getAsString();
        }
        client.close();
    }

    @Override
    public String getAuthToken() {
        return token;
    }

    @Override
    public String refreshToken() throws AuthenticationException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(DELEGATED_AUTH_SERVICE);
        WebTarget tokenPath = target.path("/token/refresh");
        Response response = tokenPath.request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Token " + this.auth_token).get();
        client.close();
        JsonElement json = getResultAsJson(response);

        if (json.getAsJsonObject().get("access_token") != null) {
            token = json.getAsJsonObject().get("access_token").getAsString();
        }

        return token;
    }

    private JsonElement getResultAsJson(Response response) {
        String result = response.readEntity(String.class);
        JsonParser parser = new JsonParser();
        JsonObject resultJson = parser.parse(result).getAsJsonObject();

        logger.trace("Json Result: {}", resultJson);

        return resultJson;
    }
}
