/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Doorbell LCD message types.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum LcdMessageType implements ApiValueEnum {
    @SerializedName("LEAVE_PACKAGE_AT_DOOR")
    LEAVE_PACKAGE_AT_DOOR("LEAVE_PACKAGE_AT_DOOR"),
    @SerializedName("DO_NOT_DISTURB")
    DO_NOT_DISTURB("DO_NOT_DISTURB"),
    @SerializedName("CUSTOM_MESSAGE")
    CUSTOM_MESSAGE("CUSTOM_MESSAGE"),
    @SerializedName("IMAGE")
    IMAGE("IMAGE");

    private final String apiValue;

    LcdMessageType(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }
}
