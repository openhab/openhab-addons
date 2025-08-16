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
package org.openhab.binding.sungrow.internal.client.dto;

import com.google.gson.annotations.SerializedName;

/**
 * @author Christian Kemper - Initial contribution
 */
public enum LoginState {

    @SerializedName("-1")
    ACCOUNT_NOT_EXIST,
    @SerializedName("0")
    INCORRECT_PASSWORD,
    @SerializedName("1")
    SUCCESS,
    @SerializedName("2")
    ACCOUNT_LOCKED,
    @SerializedName("5")
    ACCOUNT_LOCKED_BY_ADMIN
}
