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
package org.openhab.binding.jablotron.internal.model.ja100f;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JablotronGetThermoDevicesData} class defines the data object for the
 * getThermoDevices response
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronGetThermoDevicesData {

    List<JablotronState> states = new ArrayList<>();

    @SerializedName(value = "thermo-devices")
    List<JablotronThermoDevice> thermoDevices = new ArrayList<>();

    public List<JablotronState> getStates() {
        return states;
    }

    public List<JablotronThermoDevice> getThermoDevices() {
        return thermoDevices;
    }
}
