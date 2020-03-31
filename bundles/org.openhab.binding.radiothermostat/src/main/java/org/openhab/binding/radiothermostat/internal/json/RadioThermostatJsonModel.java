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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RadioThermostatJsonModel} is responsible for storing
 * the data from the thermostat 'tstat/model' JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
public class RadioThermostatJsonModel {
    @SerializedName("model")
    private String model;

    public String getModel() {
        return model;
    }

}
