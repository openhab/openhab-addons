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
 * Asuswrt Credentials
 * structure-class for login informations
 * 
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtCredentials {
    private String username = "";
    private String password = "";
    private String encodedCredentials = "";

    /**
     * INIT CLASS
     */
    public AsuswrtCredentials() {
    }

    /**
     * INIT CLASS
     * 
     * @param routerConfig
     */
    public AsuswrtCredentials(AsuswrtConfiguration routerConfig) {
        setCredentials(routerConfig.username, routerConfig.password);
    }

    /**
     * INIT CLASS
     * 
     * @param username
     * @param password
     */
    public AsuswrtCredentials(String username, String password) {
        setCredentials(username, password);
    }

    /***********************************
     *
     * PRIVATE STUFF
     *
     ************************************/

    /**
     * store credentials
     * 
     * @param username
     * @param password
     */
    private void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        this.encodedCredentials = b64encode(username + ":" + password);
    }

    /**
     * b64 encode string
     * 
     * @param string
     * @return
     */
    private String b64encode(String string) {
        return Base64.getEncoder().encodeToString((string).getBytes());
    }

    /***********************************
     *
     * PUBLIC STUFF
     *
     ************************************/

    /**
     * RETURN ENCODED CREDENTIALS
     * 
     * @return b64 encoded string 'username:password'
     */
    public String getEncodedCredentials() {
        return encodedCredentials;
    }

    /**
     * RETURN PASSWORD
     * 
     * @return unencrypted password
     */
    public String getPassword() {
        return password;
    }

    /**
     * RETURN USERNAME
     * 
     * @return username
     */
    public String getUsername() {
        return username;
    }
}
