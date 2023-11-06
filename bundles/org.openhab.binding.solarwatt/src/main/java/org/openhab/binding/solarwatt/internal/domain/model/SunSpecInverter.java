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
package org.openhab.binding.solarwatt.internal.domain.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * Specialised class for inverters adhering to the sunspec modbus protocol.
 *
 * This fields have been identified to exist:
 * com.kiwigrid.devices.sunspec.SunSpecInverter=[
 * IdSunSpecBlocks
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SunSpecInverter extends Inverter {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.devices.sunspec.SunSpecInverter";

    public SunSpecInverter(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    protected String getSolarWattLabel() {
        return "SunSpecInverter";
    }
}
