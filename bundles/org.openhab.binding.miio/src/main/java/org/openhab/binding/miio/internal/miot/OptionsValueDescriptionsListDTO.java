/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.miot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping properties from json for miot device info
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class OptionsValueDescriptionsListDTO {

    @SerializedName("value")
    @Expose
    @Nullable
    public Integer value;
    @SerializedName("description")
    @Expose
    @Nullable
    public String description;

    public int getValue() {
        final Integer val = this.value;
        return val != null ? val.intValue() : 0;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDescription() {
        final String description = this.description;
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
