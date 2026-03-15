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
 * Smart detection object types.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum ObjectType implements ApiValueEnum {
    @SerializedName("person")
    PERSON("person"),
    @SerializedName("vehicle")
    VEHICLE("vehicle"),
    @SerializedName("package")
    PACKAGE("package"),
    @SerializedName("licensePlate")
    LICENSE_PLATE("licensePlate"),
    @SerializedName("face")
    FACE("face"),
    @SerializedName("animal")
    ANIMAL("animal");

    private final String apiValue;

    ObjectType(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }
}
