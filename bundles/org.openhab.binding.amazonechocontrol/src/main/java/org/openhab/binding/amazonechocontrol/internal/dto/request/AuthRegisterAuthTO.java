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
package org.openhab.binding.amazonechocontrol.internal.dto.request;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthRegisterAuthTO} encapsulates the auth information of an app registration request
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterAuthTO {
    @SerializedName("access_token")
    public @Nullable String accessToken;

    @Override
    public @NonNull String toString() {
        return "AuthRegisterAuthTO{accessToken='" + accessToken + "'}";
    }
}
