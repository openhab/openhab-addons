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
package org.openhab.binding.semsportal.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Iwan Bron - Initial contribution
 */

@NonNullByDefault
public class LoginRequestBody {
    private String account;
    private String pwd;

    public LoginRequestBody(String account, String pwd) {
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
