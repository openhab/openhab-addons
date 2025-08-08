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
 * The {@link AuthRegisterTO} encapsulate an app registration response
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterTO {
    @SerializedName("request_id")
    public String requestId;
    public AuthRegisterResponseTO response = new AuthRegisterResponseTO();

    @Override
    public @NonNull String toString() {
        return "AuthRegister{requestId='" + requestId + "', response=" + response + "}";
    }
}
