/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

/**
 * The {@link AccountTO} encapsulates the account information
 *
 * @author Jan N. Klug - Initial contribution
 */
@SuppressWarnings("unused")
public class AccountTO {
    @SuppressWarnings("unchecked")
    public static final TypeToken<List<AccountTO>> LIST_TYPE_TOKEN = (TypeToken<List<AccountTO>>) TypeToken
            .getParameterized(List.class, AccountTO.class);

    public String commsId;
    public String directedId;
    public String phoneCountryCode;
    public String phoneNumber;
    public String firstName;
    public String lastName;
    public String phoneticFirstName;
    public String phoneticLastName;
    public String commsProvisionStatus;
    public Boolean isChild;
    public Boolean signedInUser;
    public Boolean commsProvisioned;
    public Boolean speakerProvisioned;

    @Override
    public @NonNull String toString() {
        return "AccountTO{commsId='" + commsId + "', directedId='" + directedId + "', phoneCountryCode='"
                + phoneCountryCode + "', phoneNumber='" + phoneNumber + "', firstName='" + firstName + "', lastName='"
                + lastName + "', phoneticFirstName='" + phoneticFirstName + "', phoneticLastName='" + phoneticLastName
                + "', commsProvisionStatus='" + commsProvisionStatus + "', isChild=" + isChild + ", signedInUser="
                + signedInUser + ", commsProvisioned=" + commsProvisioned + ", speakerProvisioned=" + speakerProvisioned
                + "}";
    }
}
