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
 * The {@link Token} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Token {
    private String token = "";
    private String tokenType = "";
    private long expiration = 0;

    public String getToken() {
        return tokenType + " " + token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiration(int expiration) {
        this.expiration = System.currentTimeMillis() / 1000 + expiration;
    }

    public boolean isExpired() {
        return (expiration - System.currentTimeMillis() / 1000) < 2;
    }

    public void setType(String type) {
        tokenType = type;
    }
}
