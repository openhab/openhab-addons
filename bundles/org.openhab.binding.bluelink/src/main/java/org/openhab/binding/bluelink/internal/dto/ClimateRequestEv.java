/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.dto;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.library.types.QuantityType;

/**
 * Climate control request for electric vehicles.
 *
 * @author Marcus Better - Initial contribution
 */
public record ClimateRequestEv(int airCtrl, AirTemperature airTemp, boolean defrost, int heating1) {

    public static ClimateRequestEv create(final @NonNull QuantityType<@NonNull Temperature> temperature,
            final boolean heat, final boolean defrost) {
        return new ClimateRequestEv(1, AirTemperature.of(temperature), defrost, heat ? 1 : 0);
    }
}
