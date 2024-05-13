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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Alexander Drent - Initial contribution
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Setpoints {
    private IconID roomTemperature;
    private IconID leavingWaterTemperature;
    private IconID leavingWaterOffset;

    private IconID domesticHotWaterTemperature;

    public IconID getRoomTemperature() {
        return roomTemperature;
    }

    public IconID getLeavingWaterTemperature() {
        return leavingWaterTemperature;
    }

    public IconID getLeavingWaterOffset() {
        return leavingWaterOffset;
    }

    public IconID getdomesticHotWaterTemperature() {
        return domesticHotWaterTemperature;
    }
}
