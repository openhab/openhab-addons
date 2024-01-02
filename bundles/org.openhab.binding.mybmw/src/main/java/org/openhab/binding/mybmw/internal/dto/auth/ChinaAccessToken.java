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
package org.openhab.binding.mybmw.internal.dto.auth;

import org.openhab.binding.mybmw.internal.utils.Constants;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ChinaAccessToken} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class ChinaAccessToken {
    @SerializedName("access_token")
    public String accessToken = Constants.EMPTY;
    @SerializedName("token_type")
    public String tokenType = Constants.EMPTY;
}
