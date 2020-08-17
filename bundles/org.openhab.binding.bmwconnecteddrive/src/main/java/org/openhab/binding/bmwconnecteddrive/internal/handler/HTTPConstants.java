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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HTTPConstants} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class HTTPConstants {
    public final static String CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded";
    public final static String CONTENT_TYPE_JSON = "application/json";
    public final static String KEEP_ALIVE = "Keep-Alive";
    public final static String CLIENT_ID = "client_id";
    public final static String RESPONSE_TYPE = "response_type";
    public final static String TOKEN = "token";
    public final static String REDIRECT_URI = "redirect_uri";
    public final static String SCOPE = "scope";
    public final static String CREDENTIALS = "Credentials";
    public final static String USERNAME = "username";
    public final static String PASSWORD = "password";

    public final static String ACCESS_TOKEN = "access_token";
    public final static String TOKEN_TYPE = "token_type";
    public final static String EXPIRES_IN = "expires_in";
}
