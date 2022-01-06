/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AbstractResponse} class is the base class for all decoded JSON responses from the API
 *
 * @author Arne Seime - Initial contribution
 */
public abstract class AbstractResponse {
    public static final int ERROR_CODE_ACCESS_TOKEN_EXPIRED = 3515;
    public static final int ERROR_CODE_INVALID_SIGNATURE = 3015;
    public static final int ERROR_CODE_AUTHENTICATION_FAILURE = 1025;
    public int errorCode;
    @SerializedName("error")
    public String errorName;
    @SerializedName("description")
    public String errorDescription;
}
