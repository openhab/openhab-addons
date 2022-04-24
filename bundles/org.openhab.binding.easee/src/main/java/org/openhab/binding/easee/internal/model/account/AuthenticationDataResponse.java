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
package org.openhab.binding.easee.internal.model.account;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * class to map json response of the Account login/refreshToken API calls
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class AuthenticationDataResponse {
    public int expiresIn;
    public String accessToken = "";
    public String refreshToken = "";

    /**
     * validates login data
     *
     * @return
     */
    public boolean isValidLogin() {
        if (accessToken.isBlank() || refreshToken.isBlank() || expiresIn == 0) {
            return false;
        }
        return true;
    }
}
