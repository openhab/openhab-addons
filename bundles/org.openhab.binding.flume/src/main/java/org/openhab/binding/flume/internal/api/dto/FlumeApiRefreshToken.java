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
package org.openhab.binding.flume.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FlumeApiRefreshToken} dto for refresh token
 *
 * @author Jeff James - Initial contribution
 */
public class FlumeApiRefreshToken {
    @SerializedName("grant_type")
    public final String grantType = "refresh_token";
    @SerializedName("client_id")
    public String clientId;
    @SerializedName("client_secret")
    public String clientSecret;
    @SerializedName("refresh_token")
    public String refeshToken;
}
