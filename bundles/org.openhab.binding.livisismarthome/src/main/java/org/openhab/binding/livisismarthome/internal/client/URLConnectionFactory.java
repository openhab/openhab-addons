/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;

/**
 * The {@link URLConnectionFactory} is responsible for creating requests / connections.
 * This is useful to avoid real connections in test mode (via replacing it with mocks).
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class URLConnectionFactory {

    public static final String CONTENT_TYPE = "application/json";
    public static final int HTTP_REQUEST_TIMEOUT_MILLISECONDS = 10000;

    private static final String BEARER = "Bearer ";

    public HttpURLConnection createRequest(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    public HttpURLConnection createBaseRequest(String url, HttpMethod httpMethod,
            AccessTokenResponse accessTokenResponse) throws IOException {
        HttpURLConnection urlConnection = createRequest(url);
        urlConnection.setRequestMethod(httpMethod.asString());
        urlConnection.setRequestProperty(HttpHeader.ACCEPT.asString(), CONTENT_TYPE);
        urlConnection.setRequestProperty(HttpHeader.AUTHORIZATION.asString(),
                BEARER + accessTokenResponse.getAccessToken());
        urlConnection.setConnectTimeout(HTTP_REQUEST_TIMEOUT_MILLISECONDS);
        urlConnection.setReadTimeout(HTTP_REQUEST_TIMEOUT_MILLISECONDS);
        return urlConnection;
    }
}
