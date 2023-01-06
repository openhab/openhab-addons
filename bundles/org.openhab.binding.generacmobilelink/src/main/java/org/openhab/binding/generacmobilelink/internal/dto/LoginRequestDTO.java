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
package org.openhab.binding.generacmobilelink.internal.dto;

/**
 * {@link LoginRequestDTO} request for the MobileLink API
 *
 * @author Dan Cunningham - Initial contribution
 */
public class LoginRequestDTO {
    public LoginRequestDTO(String sharedKey, String userLogin, String userPassword) {
        super();
        this.sharedKey = sharedKey;
        this.userLogin = userLogin;
        this.userPassword = userPassword;
    }

    public String sharedKey;
    public String userLogin;
    public String userPassword;
}
