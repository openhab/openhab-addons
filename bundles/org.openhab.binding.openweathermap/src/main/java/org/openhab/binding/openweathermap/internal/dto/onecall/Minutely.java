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
package org.openhab.binding.openweathermap.internal.dto.onecall;

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
public class Minutely {
    private int dt;
    private double precipitation;

    public int getDt() {
        return dt;
    }

    public void setDt(int dt) {
        this.dt = dt;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }
}
