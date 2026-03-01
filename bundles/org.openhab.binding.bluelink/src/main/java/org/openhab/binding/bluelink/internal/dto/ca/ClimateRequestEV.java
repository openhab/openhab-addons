/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.dto.ca;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.core.library.types.QuantityType;

/**
 * Climate control request for electric vehicles (Canada) except KIA.
 *
 * @author Marcus Better - Initial contribution
 */
public record ClimateRequestEV(String pin, HvacInfo hvacInfo) {

    public record HvacInfo(int airCtrl, AirTemperature airTemp, boolean defrost, int heating1) {
    }

    public static ClimateRequestEV create(final IVehicle vehicle, final String pin,
            final @NonNull QuantityType<@NonNull Temperature> temperature, final boolean heat, final boolean defrost) {
        return new ClimateRequestEV(pin,
                new HvacInfo(1, AirTemperature.of(vehicle, temperature), defrost, heat ? 1 : 0));
    }
}
