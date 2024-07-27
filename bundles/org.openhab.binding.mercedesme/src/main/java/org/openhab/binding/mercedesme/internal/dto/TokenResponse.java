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

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.Constants;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TokenResponse} dto contains JSon body of token response
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TokenResponse {
    @SerializedName("access_token")
    public String accessToken = Constants.NOT_SET;
    @SerializedName("refresh_token")
    public String refreshToken = Constants.NOT_SET;
    @SerializedName("token_type")
    public String tokenType = Constants.NOT_SET;
    @SerializedName("expires_in")
    public int expiresIn;
    public String createdOn = Instant.now().toString();
}
