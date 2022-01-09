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
package org.openhab.binding.solarwatt.internal.domain.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * Specialised class for the powermeter connected via S0 bus.
 *
 * This fields have been identified to exist:
 * com.kiwigrid.devices.s0counter.S0Counter=[
 * SettingRateImpulses,
 * CountPulses
 * ]
 * 
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class S0Counter extends PowerMeter {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.devices.s0counter.S0Counter";

    public S0Counter(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    protected String getSolarWattLabel() {
        return "S0Counter";
    }
}
