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
package org.openhab.binding.iotawatt.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Status response of IoTaWatt.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public record StatusResponse(@Nullable List<Input> inputs, @Nullable List<Output> outputs) {
    /**
     * Represents the inputs of IoTaWatt
     * 
     * @param channel The channel ID
     * @param vrms Current VRMS
     * @param hz Current frequency
     * @param phase Current phase
     * @param watts Current watts
     * @param pf Current power factor
     */
    public record Input(int channel, @Nullable @SerializedName("Vrms") Float vrms,
            @Nullable @SerializedName("Hz") Float hz, @Nullable Float phase,
            @Nullable @SerializedName("Watts") Float watts, @Nullable @SerializedName("Pf") Float pf) {
    }

    /**
     * Represents the outputs of IoTaWatt
     * 
     * @param name Name of the output
     * @param units Unit of the output
     * @param value Current value of the output
     */
    public record Output(String name, String units, Float value) {
    }
}
