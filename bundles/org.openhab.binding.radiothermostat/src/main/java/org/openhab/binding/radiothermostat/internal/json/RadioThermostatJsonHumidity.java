/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.radiothermostat.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RadioThermostatJsonHumidity} is responsible for storing
 * the data from the thermostat 'tstat/humidity' JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatJsonHumidity {
    @SerializedName("humidity")
    private Integer humidity = 0;
    
    public RadioThermostatJsonHumidity() {
    }

    public Integer getHumidity() {
        return humidity;
    }

}
