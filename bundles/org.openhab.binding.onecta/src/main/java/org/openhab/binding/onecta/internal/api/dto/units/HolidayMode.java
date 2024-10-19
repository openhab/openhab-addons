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
package org.openhab.binding.onecta.internal.api.dto.units;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Drent - Initial contribution
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayMode {
    @SerializedName("ref")
    private String ref;
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("value")
    private HolidayModeValue value;

    public String getRef() {
        return ref;
    }

    public boolean isSettable() {
        return settable;
    }

    public String getValue() {
        return value.isEnabled() ? "ON" : "OFF";
    }
}
