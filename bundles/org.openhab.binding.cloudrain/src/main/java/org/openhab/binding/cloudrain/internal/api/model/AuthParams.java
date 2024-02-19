/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A Class for authentication parameters of the Cloudrain API
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class AuthParams {
    private String user;
    private String password;
    private String clientId;
    private String clientSecret;

    /**
     * Crates an AuthParams object with all required information.
     *
     * @param user the Cloudrain account user name
     * @param password the Cloudrain account password
     * @param clientId the Cloudrain Developer API client ID
     * @param clientSecret the Cloudrain Developer API client secret
     */
    public AuthParams(String user, String password, String clientId, String clientSecret) {
        this.user = user;
        this.password = password;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
