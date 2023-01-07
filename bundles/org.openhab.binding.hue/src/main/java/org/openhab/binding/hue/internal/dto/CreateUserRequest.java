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
package org.openhab.binding.hue.internal.dto;

/**
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@SuppressWarnings("unused")
public class CreateUserRequest {
    private String username;
    private String devicetype;

    public CreateUserRequest(String username, String devicetype) {
        if (Util.stringSize(devicetype) > 40) {
            throw new IllegalArgumentException("Device type can be at most 40 characters long");
        }

        if (Util.stringSize(username) < 10 || Util.stringSize(username) > 40) {
            throw new IllegalArgumentException("Username must be between 10 and 40 characters long");
        }

        this.username = username;
        this.devicetype = devicetype;
    }

    public CreateUserRequest(String devicetype) {
        if (Util.stringSize(devicetype) > 40) {
            throw new IllegalArgumentException("Device type can be at most 40 characters long");
        }

        this.devicetype = devicetype;
    }
}
