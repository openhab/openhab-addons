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
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;

/**
 * The {@link Token} BMW ConnectedDrive Token storage
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Token {
    private String token = Constants.EMPTY;
    private String tokenType = Constants.EMPTY;
    private long expiration = 0;

    public String getBearerToken() {
        return new StringBuffer(tokenType).append(Constants.SPACE).append(token).toString();
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiration(int expiration) {
        this.expiration = System.currentTimeMillis() / 1000 + expiration;
    }

    /**
     * @return true if Token expires in less than 1 second
     */
    public boolean isExpired() {
        return (expiration - System.currentTimeMillis() / 1000) < 1;
    }

    public void setType(String type) {
        tokenType = type;
    }

    public boolean isValid() {
        return (!token.equals(Constants.EMPTY) && !tokenType.equals(Constants.EMPTY) && expiration > 0);
    }
}
