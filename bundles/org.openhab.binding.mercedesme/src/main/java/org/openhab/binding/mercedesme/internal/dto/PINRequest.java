/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal.dto;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PINRequest} dto contains JSon body for PIN request
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PINRequest {
    public String emailOrPhoneNumber;
    public String countryCode;
    public String nonce;

    public PINRequest(String mail, String country) {
        emailOrPhoneNumber = mail;
        countryCode = country;
        nonce = UUID.randomUUID().toString();
    }
}
