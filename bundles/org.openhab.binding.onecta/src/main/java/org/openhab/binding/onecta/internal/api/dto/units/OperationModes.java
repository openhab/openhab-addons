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
package org.openhab.binding.onecta.internal.api.dto.units;

import org.openhab.binding.onecta.internal.api.Enums;

import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Drent - Initial contribution
 */
public class OperationModes {
    @SerializedName("heating")
    private OpertationMode heating;
    @SerializedName("cooling")
    private OpertationMode cooling;
    @SerializedName("auto")
    private OpertationMode auto;

    public OpertationMode getOperationMode(Enums.OperationMode operationMode) {
        if (operationMode.equals(Enums.OperationMode.HEAT)) {
            return this.heating;
        } else if (operationMode.equals(Enums.OperationMode.COLD)) {
            return this.cooling;
        } else if (operationMode.equals(Enums.OperationMode.AUTO)) {
            return this.auto;
        } else
            return null;
    }
}
