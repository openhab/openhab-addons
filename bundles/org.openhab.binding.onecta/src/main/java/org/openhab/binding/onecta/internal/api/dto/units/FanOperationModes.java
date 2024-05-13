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
package org.openhab.binding.onecta.internal.api.dto.units;

import org.openhab.binding.onecta.internal.api.Enums;

import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Drent - Initial contribution
 */
public class FanOperationModes {

    @SerializedName("heating")
    private FanOnlyClass heating;
    @SerializedName("cooling")
    private FanOnlyClass cooling;
    @SerializedName("auto")
    private FanOnlyClass auto;
    @SerializedName("dry")
    private FanOnlyClass dry;
    @SerializedName("fanOnly")
    private FanOnlyClass fanOnly;

    public FanOnlyClass getFanOperationMode(Enums.OperationMode operationMode) {
        if (operationMode.getValue() == Enums.OperationMode.HEAT.getValue()) {
            return this.heating;
        } else if (operationMode.getValue() == Enums.OperationMode.COLD.getValue()) {
            return this.cooling;
        } else if (operationMode.getValue() == Enums.OperationMode.AUTO.getValue()) {
            return this.auto;
        } else if (operationMode.getValue() == Enums.OperationMode.FAN.getValue()) {
            return this.fanOnly;
        } else if (operationMode.getValue() == Enums.OperationMode.DEHUMIDIFIER.getValue()) {
            return this.dry;
        } else
            return null;
    }

    public FanOnlyClass getHeating() {
        return heating;
    }

    public FanOnlyClass getCooling() {
        return cooling;
    }

    public FanOnlyClass getAuto() {
        return auto;
    }

    public FanOnlyClass getDry() {
        return dry;
    }

    public FanOnlyClass getFanOnly() {
        return fanOnly;
    }
}
