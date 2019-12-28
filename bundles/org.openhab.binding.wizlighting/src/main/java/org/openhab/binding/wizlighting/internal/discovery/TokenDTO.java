/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.discovery;

/**
 * This is {@link TokenDTO} Object to parse Discovery response of access tokens.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class TokenDTO {
    public String access_token;
    public long expires_in;
    public String token_type;
    public String scope;
    public String refresh_token;
}
