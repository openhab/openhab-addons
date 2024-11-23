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
package org.openhab.binding.millheat.internal.dto;

/**
 * This DTO class wraps the login request
 * 
 * @author Arne Seime - Initial contribution
 */
public class LoginRequest implements AbstractRequest {
    public final String account;
    public final String password;

    public LoginRequest(final String username, final String password) {
        this.account = username;
        this.password = password;
    }

    @Override
    public String getRequestUrl() {
        return "login";
    }
}
