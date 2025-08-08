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

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthRegisterCustomerInfoTO} encapsulates the customer information of a registration response
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterCustomerInfoTO {
    @SerializedName("account_pool")
    public String accountPool;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("home_region")
    public String homeRegion;
    public String name;
    @SerializedName("given_name")
    public String givenName;

    @Override
    public @NonNull String toString() {
        return "AuthRegisterCustomerInfoTO{accountPool='" + accountPool + "', userId='" + userId + "', homeRegion='"
                + homeRegion + "', name='" + name + "', givenName='" + givenName + "'}";
    }
}
