/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.data;

/**
 * The {@link SurePetcareLoginCredentials} is the Java class as a DTO to hold login credentials for the Sure Petcare
 * API.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareLoginCredentials {

    private String email_address;
    private String password;
    private String device_id;

    public SurePetcareLoginCredentials() {
    }

    public SurePetcareLoginCredentials(String email_address, String password, String device_id) {
        super();
        this.email_address = email_address;
        this.password = password;
        this.device_id = device_id;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
