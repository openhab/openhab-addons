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
package org.openhab.binding.solarwatt.internal.domain.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * Specialised class for the MyReserve inverter.
 *
 * The MyReserve also contains {@link MyReservePowerMeter} and {@link MyReserve}.
 * This fields have been identified to exist:
 * com.kiwigrid.devices.solarwatt.MyReserveInverter=[
 * PowerBatteryDC,
 * IdMyReserveSetupRole,
 * IdMyReserveSetup,
 * IdDcCoupledBatteriesList,
 * IdInverter,
 * Command
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class MyReserveInverter extends Inverter {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.devices.solarwatt.MyReserveInverter";

    public MyReserveInverter(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    protected String getSolarWattLabel() {
        return "MyReserveInverter";
    }
}
