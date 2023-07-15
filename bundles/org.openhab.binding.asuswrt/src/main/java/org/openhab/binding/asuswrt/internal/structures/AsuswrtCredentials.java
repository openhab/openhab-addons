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
package org.openhab.binding.asuswrt.internal.structures;

import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class is used for storing Asuswrt credentials.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtCredentials {
    private String username = "";
    private String password = "";
    private String encodedCredentials = "";

    public AsuswrtCredentials() {
    }

    public AsuswrtCredentials(AsuswrtConfiguration routerConfig) {
        setCredentials(routerConfig.username, routerConfig.password);
    }

    public AsuswrtCredentials(String username, String password) {
        setCredentials(username, password);
    }

    /*
     * Private methods
     */

    /**
     * Stores the given credentials.
     */
    private void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        encodedCredentials = b64encode(username + ":" + password);
    }

    /**
     * Encodes a String using Base64.
     */
    private String b64encode(String string) {
        return Base64.getEncoder().encodeToString((string).getBytes());
    }

    /*
     * Public methods
     */

    /**
     * Returns Base64 encoded credentials.
     *
     * @return 'username:password' as Base64 encoded string
     */
    public String getEncodedCredentials() {
        return encodedCredentials;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
