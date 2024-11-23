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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JsonRenewTokenResponse} encapsulate the GSON response of the renew token request
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonRenewTokenResponse {
    @SerializedName("access_token")
    public @Nullable String accessToken;
    @SerializedName("token_type")
    public @Nullable String tokenType;
    @SerializedName("expires_in")
    public @Nullable Long expiresIn;
}
