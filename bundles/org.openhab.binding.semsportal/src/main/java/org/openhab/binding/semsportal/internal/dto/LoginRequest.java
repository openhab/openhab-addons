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
package org.openhab.binding.semsportal.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The login request to the portal. Response can be deserialized in a {@link LoginResponse}
 *
 * @author Iwan Bron - Initial contribution
 */

@NonNullByDefault
public class LoginRequest {
    private String account;
    private String pwd;

    public LoginRequest(String account, String pwd) {
        this.account = account;
        this.pwd = pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwd() {
        return pwd;
    }

    public String getAccount() {
        return account;
    }
}
