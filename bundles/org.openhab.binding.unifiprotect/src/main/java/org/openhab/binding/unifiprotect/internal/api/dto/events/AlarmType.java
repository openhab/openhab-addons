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
package org.openhab.binding.unifiprotect.internal.api.dto.events;

import org.openhab.binding.unifiprotect.internal.api.dto.ApiValueEnum;

import com.google.gson.annotations.SerializedName;

/**
 * Sensor alarm types.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum AlarmType implements ApiValueEnum {
    @SerializedName("smoke")
    SMOKE("smoke"),
    @SerializedName("CO")
    CO("CO");

    private final String apiValue;

    AlarmType(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }
}
