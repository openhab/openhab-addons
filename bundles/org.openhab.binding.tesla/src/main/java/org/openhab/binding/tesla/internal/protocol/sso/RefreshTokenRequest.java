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
package org.openhab.binding.tesla.internal.protocol.sso;

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

/**
 * The {@link RefreshTokenRequest} is a datastructure to refresh
 * the access token for the SSO endpoint
 *
 * @author Christian Güdel - Initial contribution
 */
public class RefreshTokenRequest {
    public String grant_type = "refresh_token";
    public String client_id = CLIENT_ID;
    public String refresh_token;
    public String scope = SSO_SCOPES;

    public RefreshTokenRequest(String refresh_token) {
        this.refresh_token = refresh_token;
    }
}
