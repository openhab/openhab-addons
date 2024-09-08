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
 * The {@link FlumeApiToken} dto response for getToken.
 *
 * @author Jeff James - Initial contribution
 */
public class FlumeApiToken {
    @SerializedName("token_type")
    public String tokenType;
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("expires_in")
    public int expiresIn;
    @SerializedName("refresh_token")
    public String refreshToken;
}
