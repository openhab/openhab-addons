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
package org.openhab.binding.lgthinq.lgservices.model.ac;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link ACSnapshotV2}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ACSnapshotV2 extends ACSnapshot {

    @Override
    @JsonProperty("airState.windStrength")
    public Integer getAirWindStrength() {
        return super.getAirWindStrength();
    }

    @Override
    @JsonProperty("airState.tempState.target")
    public Double getTargetTemperature() {
        return super.getTargetTemperature();
    }

    @Override
    @JsonProperty("airState.tempState.current")
    public Double getCurrentTemperature() {
        return super.getCurrentTemperature();
    }

    @Override
    @JsonProperty("airState.opMode")
    public Integer getOperationMode() {
        return super.getOperationMode();
    }

    @Override
    @JsonProperty("airState.operation")
    @Nullable
    public Integer getOperation() {
        return super.getOperation();
    }
}
