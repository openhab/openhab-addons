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
package org.openhab.binding.automower.internal.rest.api.authentication;

import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.automower.internal.rest.api.HusqvarnaApi;
import org.openhab.binding.automower.internal.rest.api.authentication.dto.PostOAuth2Response;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.binding.automower.internal.rest.exceptions.UnauthorizedException;

import com.google.gson.JsonSyntaxException;

/**
 * Gives access to the Husqvarna authentication api
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class AuthenticationApi extends HusqvarnaApi {

    public AuthenticationApi(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String getBaseUrl() {
        return "https://api.authentication.husqvarnagroup.dev/v1";
    }

    public PostOAuth2Response loginOAuth2(String appKey, String username, String password)
            throws AutomowerCommunicationException {
        final Request request = getHttpClient().newRequest(getBaseUrl() + "/oauth2/token");
        request.method(HttpMethod.POST);

        Fields fields = new Fields();
        fields.put("grant_type", "password");
        fields.put("client_id", appKey);
        fields.put("username", username);
        fields.put("password", password);

        request.content(new FormContentProvider(fields));

        ContentResponse response;

        try {
            response = request.send();
        } catch (Exception e) {
            throw new AutomowerCommunicationException(e);
        }

        return parseResponse(response);
    }

    public PostOAuth2Response loginWithRefreshToken(String appKey, String refreshToken)
            throws AutomowerCommunicationException {
        final Request request = getHttpClient().newRequest(getBaseUrl() + "/oauth2/token");
        request.method(HttpMethod.POST);

        Fields fields = new Fields();
        fields.put("grant_type", "refresh_token");
        fields.put("client_id", appKey);
        fields.put("refresh_token", refreshToken);

        request.content(new FormContentProvider(fields));

        ContentResponse response;

        try {
            response = request.send();
        } catch (Exception e) {
            throw new AutomowerCommunicationException(e);
        }

        return parseResponse(response);

    }

    private PostOAuth2Response parseResponse(ContentResponse response) throws AutomowerCommunicationException {
        int statusCode = response.getStatus();
        switch (statusCode) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_CREATED:
                try {
                    return gson.fromJson(response.getContentAsString(), PostOAuth2Response.class);
                } catch (JsonSyntaxException e) {
                    throw new AutomowerCommunicationException(e);
                }

            case HttpStatus.SC_BAD_REQUEST:
                throw new AutomowerCommunicationException(statusCode,
                        "Unable to authenticate. Maybe the given credentials are wrong or the Authentication Api is not connected to the given application key: "
                                + response.getContentAsString());

            case HttpStatus.SC_FORBIDDEN:
            case HttpStatus.SC_UNAUTHORIZED:
                throw new UnauthorizedException(statusCode, response.getContentAsString());

            default:
                throw new AutomowerCommunicationException(statusCode, response.getContentAsString());
        }
    }
}
