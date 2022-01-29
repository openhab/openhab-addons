/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HTTPConstants} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class HTTPConstants {
    public static final int HTTP_TIMEOUT_SEC = 10;

    public static final String AUTH_HTTP_CLIENT_NAME = "AuthHttpClient";
    public static final String CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON_ENCODED = "application/json";
    public static final String KEEP_ALIVE = "Keep-Alive";
    public static final String CLIENT_ID = "client_id";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String TOKEN = "token";
    public static final String CODE = "code";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String AUTHORIZATION = "authorization";
    public static final String GRANT_TYPE = "grant_type";
    public static final String SCOPE = "scope";
    public static final String CREDENTIALS = "Credentials";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String CONTENT_LENGTH = "Content-Length";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE = "token_type";
    public static final String EXPIRES_IN = "expires_in";
    public static final String CHUNKED = "chunked";
}
