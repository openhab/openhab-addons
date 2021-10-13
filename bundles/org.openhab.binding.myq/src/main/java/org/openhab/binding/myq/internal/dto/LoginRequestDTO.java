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
package org.openhab.binding.myq.internal.dto;

/**
 * The {@link LoginRequestDTO} entity from the MyQ API
 *
 * @author Dan Cunningham - Initial contribution
 */
public class LoginRequestDTO {

    public LoginRequestDTO(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public String username;
    public String password;
}
