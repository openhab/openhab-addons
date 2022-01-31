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
package org.openhab.binding.surepetcare.internal.dto;

/**
 * The {@link SurePetcareLoginCredentials} is the Java class as a DTO to hold login credentials for the Sure Petcare
 * API.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareLoginCredentials {

    public String emailAddress;
    public String password;
    public String deviceId;

    public SurePetcareLoginCredentials() {
    }

    public SurePetcareLoginCredentials(String emailAddress, String password, String deviceId) {
        this.emailAddress = emailAddress;
        this.password = password;
        this.deviceId = deviceId;
    }
}
