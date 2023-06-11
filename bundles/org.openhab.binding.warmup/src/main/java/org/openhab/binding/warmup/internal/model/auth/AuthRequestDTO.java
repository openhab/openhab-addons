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
package org.openhab.binding.warmup.internal.model.auth;

/**
 * @author James Melville - Initial contribution
 */
@SuppressWarnings("unused")
public class AuthRequestDTO {

    private AuthRequestDataDTO request;

    public AuthRequestDTO(String email, String password, String method, String appId) {
        setRequest(new AuthRequestDataDTO(email, password, method, appId));
    }

    public void setRequest(AuthRequestDataDTO request) {
        this.request = request;
    }
}
