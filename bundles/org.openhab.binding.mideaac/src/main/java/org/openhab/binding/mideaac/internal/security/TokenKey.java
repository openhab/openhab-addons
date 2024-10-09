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
package org.openhab.binding.mideaac.internal.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TokenKey} returns the active Token and Key.
 *
 * @author Jacek Dobrowolski - Initial Contribution
 * @author Bob Eckhoff - JavaDoc
 */
@NonNullByDefault
public class TokenKey {
    String token;
    String key;

    /**
     * Parameters for TokenKey class
     * 
     * @param token Cloud Provider token
     * @param key Cloud Provider key
     */
    public TokenKey(String token, String key) {
        super();
        this.token = token;
        this.key = key;
    }

    /**
     * Gets token
     * 
     * @return token
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets key
     * 
     * @return key
     */
    public String getKey() {
        return key;
    }
}
