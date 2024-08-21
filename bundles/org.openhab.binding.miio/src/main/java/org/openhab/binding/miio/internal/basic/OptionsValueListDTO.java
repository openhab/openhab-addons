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
package org.openhab.binding.miio.internal.basic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping properties from json for channel options
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class OptionsValueListDTO {

    @SerializedName("value")
    @Expose
    public @Nullable String value;

    @SerializedName("label")
    @Expose
    public @Nullable String label;

    public @Nullable String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public @Nullable String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
