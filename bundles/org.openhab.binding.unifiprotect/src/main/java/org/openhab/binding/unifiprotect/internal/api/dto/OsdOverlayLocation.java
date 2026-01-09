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
 * OSD overlay screen location options.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum OsdOverlayLocation implements ApiValueEnum {
    @SerializedName("topLeft")
    TOP_LEFT("topLeft"),
    @SerializedName("topMiddle")
    TOP_MIDDLE("topMiddle"),
    @SerializedName("topRight")
    TOP_RIGHT("topRight"),
    @SerializedName("bottomLeft")
    BOTTOM_LEFT("bottomLeft"),
    @SerializedName("bottomMiddle")
    BOTTOM_MIDDLE("bottomMiddle"),
    @SerializedName("bottomRight")
    BOTTOM_RIGHT("bottomRight");

    private final String apiValue;

    OsdOverlayLocation(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }
}
