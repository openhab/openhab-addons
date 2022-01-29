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

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.CHANNEL_POWER_AC_OUT;
import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.CHANNEL_WORK_AC_OUT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * Class to represent the producing parts of the photovoltaic installation.
 *
 * This fields have been identified to exist:
 * com.kiwigrid.devices.pvplant.PVPlant=[
 * PowerACOutMax,
 * IdInverterList,
 * TimePowerOutForecastGranularity,
 * ForecastDateUpdate,
 * WorkACOut,
 * DegreeDirection,
 * ForecastPowerOut,
 * WorkAnnualYield,
 * PowerACOut,
 * DatePowerOutForecastStart,
 * PowerLimit,
 * IdMountingType,
 * FractionDeratingLimit,
 * DateInstallation,
 * ForecastWorkOut,
 * PowerOutForecastNow,
 * PriceProfitFeedin,
 * AddressLocation,
 * PowerOutForecastValues,
 * FractionConfigDeratingLimit,
 * PowerInstalledPeak,
 * LocationGeographical,
 * DegreeInclination
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class PVPlant extends Device {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.devices.pvplant.PVPlant";

    public PVPlant(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    public void update(DeviceDTO deviceDTO) {
        super.update(deviceDTO);

        this.addWattQuantity(CHANNEL_POWER_AC_OUT, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_AC_OUT, deviceDTO);
    }

    @Override
    protected String getSolarWattLabel() {
        return "PVPlant";
    }
}
