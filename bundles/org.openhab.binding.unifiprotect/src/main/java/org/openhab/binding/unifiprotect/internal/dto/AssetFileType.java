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
package org.openhab.binding.unifiprotect.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Device asset file type.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum AssetFileType implements ApiValueEnum {
    @SerializedName("animations")
    ANIMATIONS("animations");

    private final String apiValue;

    AssetFileType(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }
}
