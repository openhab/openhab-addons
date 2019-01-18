/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal.protocol;

import java.security.GeneralSecurityException;

/**
 * The {@link TokenRequestRefreshToken} is a datastructure to capture
 * authentication/credentials required to log into the
 * Tesla Remote Service
 *
 * @author Nicolai Grødum - Adding token based auth
 */
public class TokenRequestRefreshToken extends TokenRequest {

    private String grant_type = "refresh_token";
    private String refresh_token;

    public TokenRequestRefreshToken(String refresh_token) throws GeneralSecurityException {
        super();
        this.refresh_token = refresh_token;
    }
}
