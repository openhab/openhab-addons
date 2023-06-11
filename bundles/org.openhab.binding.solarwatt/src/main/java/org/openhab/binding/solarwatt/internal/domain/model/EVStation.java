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

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * Base class for a wallbox.
 *
 * This fields have been identified to exist:
 * com.kiwigrid.devices.evstation.EVStation=[
 * PowerACOutMax,
 * PowerACIn,
 * VoltageACL1,
 * VoltageACL2,
 * VoltageACL3,
 * WorkACOut,
 * ConnectivityStatus,
 * StateOfChargeMinimum,
 * WorkACInLimitSession,
 * WorkCharge,
 * PowerACOut,
 * PowerACOutLimit,
 * AvailableModes,
 * PowerACInMax,
 * StateOfCharge,
 * PowerACInLimit,
 * CurrentACInLimit,
 * StateInfo,
 * ModeStation,
 * VehicleId,
 * CurrentACInMinimum,
 * WorkCapacity,
 * WorkACInSession,
 * UserId,
 * TemperatureBattery,
 * WorkACIn,
 * CurrentACInL1,
 * CurrentACInL3,
 * PowerACInMin,
 * CurrentACInL2
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class EVStation extends Device {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.devices.evstation.EVStation";

    public EVStation(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    public void update(DeviceDTO deviceDTO) {
        super.update(deviceDTO);

        this.addStringState(CHANNEL_CONNECTIVITY_STATUS, deviceDTO);
        this.addStringState(CHANNEL_MODE_STATION, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_AC_IN, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_AC_IN, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_AC_IN_SESSION, deviceDTO);
    }

    @Override
    protected String getSolarWattLabel() {
        return "EVStation";
    }
}
