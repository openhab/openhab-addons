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
package org.openhab.binding.sunsynk.internal.classes;

/**
 * The {@link APIdata} is the internal class for a Sunsynk Connect
 * Account.
 * 
 * @author Lee Charlton - Initial contribution
 */

// "data":{"access_token":"xxxxxxx","token_type":"bearer","refresh_token":"xxxxxxxx","expires_in":258669,"scope":"all"},"success":true}
public class APIdata {

    public static String static_access_token;
    private String access_token;
    private String refresh_token;
    private String token_type;
    private Long expires_in;
    private String scope;

    public Long getExpiresIn() {
        return this.expires_in;
    }

    public String getRefreshToken() {
        return this.refresh_token;
    }

    public String getToken_type() {
        return this.token_type;
    }

    public String getScope() {
        return this.scope;
    }

    public String getAccessToken() {
        return this.access_token;
    }
}
