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
 * The {@link Client} is the internal class for Client information from the sunsynk Account.
 *
 * @author Lee Charlton - Initial contribution
 */

public class Client {

    // {"code":0,"msg":"Success","data":{"access_token":"xxxxxxxxxxxxxxx","token_type":"bearer","refresh_token":"xxxxxxxxxxxxxxx","expires_in":258669,"scope":"all"},"success":true}
    private int code;
    private String msg;
    private String success;
    private APIdata data;

    public Client() {
    }

    public static String getAccessTokenString() {
        return APIdata.static_access_token;
    }

    public void setAccessTokenString(String token) {
        APIdata.static_access_token = token;
    }

    public String getExpiresIn() {

        return data.getExpiresIn();
    }

    public APIdata getData() {
        return data;
    }
}
