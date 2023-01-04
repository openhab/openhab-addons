/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * The {@link TokenResponse} is a datastructure to capture
 * authentication response from Tesla Remote Service
 *
 * @author Nicolai Grødum - Initial contribution
 */
public class TokenResponse {

    public String access_token;
    public String token_type;
    public Long expires_in;
    public Long created_at;
    public String refresh_token;

    public TokenResponse() {
    }
}
