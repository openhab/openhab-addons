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
package org.openhab.binding.warmup.internal.model.auth;

/**
 * @author James Melville - Initial contribution
 */
@SuppressWarnings("unused")
public class AuthRequestDataDTO {
    private String email;
    private String password;
    private String method;
    private String appId;

    public AuthRequestDataDTO(String email, String password, String method, String appId) {
        this.setEmail(email);
        this.setPassword(password);
        this.setMethod(method);
        this.setAppId(appId);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
