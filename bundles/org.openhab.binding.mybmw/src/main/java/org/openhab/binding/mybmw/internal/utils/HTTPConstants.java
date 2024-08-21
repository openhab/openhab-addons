/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HTTPConstants} interface contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - added image content type
 */
@NonNullByDefault
public interface HTTPConstants {
    static final int HTTP_TIMEOUT_SEC = 10;

    static final String CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded";
    static final String CONTENT_TYPE_JSON = "application/json";
    static final String CONTENT_TYPE_IMAGE = "image/png";
    static final String KEEP_ALIVE = "Keep-Alive";
    static final String CLIENT_ID = "client_id";
    static final String RESPONSE_TYPE = "response_type";
    static final String TOKEN = "token";
    static final String CODE = "code";
    static final String CODE_VERIFIER = "code_verifier";
    static final String STATE = "state";
    static final String NONCE = "nonce";
    static final String REDIRECT_URI = "redirect_uri";
    static final String AUTHORIZATION = "authorization";
    static final String GRANT_TYPE = "grant_type";
    static final String SCOPE = "scope";
    static final String CREDENTIALS = "Credentials";
    static final String USERNAME = "username";
    static final String PASSWORD = "password";
    static final String CONTENT_LENGTH = "Content-Length";
    static final String CODE_CHALLENGE = "code_challenge";
    static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    static final String ACCESS_TOKEN = "access_token";
    static final String TOKEN_TYPE = "token_type";
    static final String EXPIRES_IN = "expires_in";
    static final String CHUNKED = "chunked";

    // HTTP headers for BMW API
    static final String HEADER_ACP_SUBSCRIPTION_KEY = "ocp-apim-subscription-key";
    static final String HEADER_X_USER_AGENT = "x-user-agent";
    static final String HEADER_X_IDENTITY_PROVIDER = "x-identity-provider";
    static final String HEADER_X_CORRELATION_ID = "x-correlation-id";
    static final String HEADER_BMW_CORRELATION_ID = "bmw-correlation-id";
    static final String HEADER_BMW_VIN = "bmw-vin";
}
