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
package org.openhab.binding.openweathermap.internal.dto.onecallhist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Holds the data from the deserialised JSON response. Created using http://www.jsonschema2pojo.org/.
 * Settings:
 * Annotation Style: GSON
 * Use primitive types
 * Use double numbers
 * allow additional properties
 *
 * @author Wolfgang Klimt - Initial contribution
 */
public class Snow {

    @SerializedName("1h")
    @Expose
    private double _1h;

    public double get1h() {
        return _1h;
    }

    public void set1h(double _1h) {
        this._1h = _1h;
    }
}
