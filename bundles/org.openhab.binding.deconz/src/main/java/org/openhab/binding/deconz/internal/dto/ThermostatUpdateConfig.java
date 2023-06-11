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
package org.openhab.binding.deconz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.types.ThermostatMode;

/**
 * The {@link ThermostatUpdateConfig} is send to the Rest API to configure Thermostat.
 *
 * @author Lukas Agethen - Initial contribution
 */
@NonNullByDefault
public class ThermostatUpdateConfig {
    public @Nullable Integer heatsetpoint;
    public @Nullable ThermostatMode mode;
    public @Nullable Integer offset;
}
