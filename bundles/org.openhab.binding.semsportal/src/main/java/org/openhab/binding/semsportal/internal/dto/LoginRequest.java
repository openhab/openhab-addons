/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * POJO for the login request: the account and the hashed password. The other fields are required by the portal.
 *
 * @author Iwan Bron - Initial contribution
 * @author Julio Vilmar Gesser - Migrated to the SEMS+ API
 */
@SuppressWarnings("unused") // the fields are used by Gson
@NonNullByDefault
public class LoginRequest {
    private String account;
    private String pwd;
    private int agreement = 1;
    private boolean isChinese = false;
    private boolean isLocal = false;

    public LoginRequest(String account, String pwd) {
        this.account = account;
        this.pwd = pwd;
    }

    public String getPwd() {
        return pwd;
    }

    public String getAccount() {
        return account;
    }
}
