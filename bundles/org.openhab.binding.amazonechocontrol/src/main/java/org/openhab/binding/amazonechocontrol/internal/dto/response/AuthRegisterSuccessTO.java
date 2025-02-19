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
 * The {@link AuthRegisterSuccessTO} encapsulates the success section of an app registration response
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterSuccessTO {
    public AuthRegisterExtensionsTO extensions = new AuthRegisterExtensionsTO();
    public AuthRegisterTokensTO tokens = new AuthRegisterTokensTO();
    @SerializedName("customer_id")
    public String customerId;

    @Override
    public @NonNull String toString() {
        return "AuthRegisterSuccessTO{extensions=" + extensions + ", tokens=" + tokens + ", customerId='" + customerId
                + "'}";
    }
}
