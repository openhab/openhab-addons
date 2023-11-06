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

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * Base class for everything supplying battery base power.
 *
 * This fields have been identified to exist:
 * com.kiwigrid.devices.batteryconverter.BatteryConverter=[
 * PowerACIn,
 * UpTimePDG,
 * CurrentStringDCIn,
 * ResistanceBatteryMean,
 * CurrentBatteryIn,
 * VoltageBatteryCellMax,
 * VoltageGRMOut,
 * FactorForecast,
 * StateOfChargeMinimum,
 * StateEqualizingChargeRequiredIsSet,
 * WorkCharge,
 * VoltageBatteryCellMin,
 * VoltageBatteryCellMean,
 * StateOfHealth,
 * FactorForecastCAN,
 * StateOfCharge,
 * IdFirmwareGRM,
 * WorkCapacity,
 * CurrentGRMOut,
 * StatePDG,
 * TemperatureBatteryCellMax,
 * TemperatureGRM,
 * TemperatureBatteryMin,
 * ResistanceBatteryMin,
 * StateOfChargeMinimumLimit,
 * IdUrlPdg,
 * TemperatureBattery,
 * VoltageGRMIn,
 * CurrentGRMIn,
 * IdSerialNumberGRM,
 * ResistanceBatteryString,
 * VoltageBatteryString,
 * IdSerialNumberBatteryModules,
 * CountBatteryModules,
 * ModeConverter,
 * TemperatureBatteryMax,
 * TemperatureBatteryCellMin,
 * StateOfChargeReactivateDischarging,
 * IdMyReserveSetupRole,
 * ResistanceBatteryMax,
 * AvailableModes,
 * PowerACInMax,
 * PowerACInLimit,
 * StateOfChargeShutDownLimit,
 * CountBatteryContactor,
 * CurrentBatteryOut,
 * TimeEqualizingChargeRemaining,
 * WorkACIn,
 * MapInstallationDetails
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class BatteryConverter extends Inverter {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.devices.batteryconverter.BatteryConverter";

    public BatteryConverter(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    public void update(DeviceDTO deviceDTO) {
        super.update(deviceDTO);

        this.addSwitchState(CHANNEL_MODE_CONVERTER, deviceDTO);
        this.addPercentQuantity(CHANNEL_STATE_OF_CHARGE, deviceDTO);
        this.addPercentQuantity(CHANNEL_STATE_OF_HEALTH, deviceDTO);
        this.addCelsiusQuantity(CHANNEL_TEMPERATURE_BATTERY, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_AC_IN, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_AC_IN, deviceDTO);
        this.addVoltageQuantity(CHANNEL_VOLTAGE_BATTERY_CELL_MIN, deviceDTO, true);
        this.addVoltageQuantity(CHANNEL_VOLTAGE_BATTERY_CELL_MEAN, deviceDTO, true);
        this.addVoltageQuantity(CHANNEL_VOLTAGE_BATTERY_CELL_MAX, deviceDTO, true);
    }

    @Override
    protected String getSolarWattLabel() {
        return "BatteryConverter";
    }
}
