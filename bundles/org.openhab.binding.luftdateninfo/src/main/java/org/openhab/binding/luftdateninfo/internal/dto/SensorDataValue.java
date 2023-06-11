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
package org.openhab.binding.luftdateninfo.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SensorDataValue} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class SensorDataValue {
    private long id;
    @SerializedName("value_type")
    private String valueType;
    private String value;

    @Override
    public String toString() {
        return valueType + ":" + value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
