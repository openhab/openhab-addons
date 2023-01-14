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
 * Base class for all powermeters.
 *
 * This fields have been identified to exist:
 * com.kiwigrid.devices.powermeter.PowerMeter=[
 * InductiveEnergySum,
 * ACCurrentConsumptionL1,
 * ACCurrentConsumptionL2,
 * ACCurrentN,
 * ACCurrentConsumptionL3,
 * ApparentPowerL2,
 * ConsumptionPowerL3,
 * ConsumptionPowerL2,
 * ApparentPowerL3,
 * ApparentPowerL1,
 * ConsumptionPowerNet,
 * ReactivePowerL1,
 * ReactivePowerL2,
 * InductiveEnergyL2,
 * InductiveEnergyL1,
 * ReactivePowerL3,
 * CapacitiveEnergyL2,
 * CapacitiveEnergyL1,
 * PhaseOfMaximumImbalance,
 * InjectionPowerL1,
 * ReactivePowerSum,
 * ActivePowerL3,
 * InjectionPowerL2,
 * InjectionPowerL3,
 * ActivePowerL1,
 * ConsumptionPowerL1,
 * InjectionEnergySum,
 * ActivePowerL2,
 * ImbalanceL1,
 * InjectionPowerNet,
 * ImbalanceL2,
 * ImbalanceL3,
 * CapacitiveEnergyL3,
 * DirectionMetering,
 * ConsumptionEnergySum,
 * PowerOut,
 * InjectionEnergyNet,
 * ACVoltageL1,
 * ACVoltageL2,
 * ACVoltageL3,
 * MaximumImbalance,
 * CosinusPhiL3,
 * CosinusPhiL2,
 * CosinusPhiL1,
 * FrequencyL1,
 * FrequencyL3,
 * FrequencyL2,
 * ACCurrentInjectionL3,
 * InjectionEnergyL3,
 * ACCurrentInjectionL2,
 * ConsumptionPowerSum,
 * ACCurrentInjectionL1,
 * InjectionEnergyL1,
 * InjectionEnergyL2,
 * ActivePowerSum,
 * ConsumptionEnergyL2,
 * ConsumptionEnergyL1,
 * PowerFactorL1,
 * PowerFactorL2,
 * InjectionPowerSum,
 * ConsumptionEnergyL3,
 * PowerFactorL3,
 * ACCurrentL3,
 * CapacitiveEnergySum,
 * WorkIn,
 * InductiveEnergyL3,
 * PowerIn,
 * ACCurrentL1,
 * ACCurrentL2,
 * FrequencyGrid,
 * ConsumptionEnergyNet,
 * ApparentPowerSum,
 * WorkOut
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class PowerMeter extends Device {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.devices.powermeter.PowerMeter";

    public PowerMeter(DeviceDTO deviceDTO) {
        super(deviceDTO);

        this.addStringState(CHANNEL_DIRECTION_METERING, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_IN, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_OUT, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_IN, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_OUT, deviceDTO);
        this.addWattHourQuantity(CHANNEL_CONSUMPTION_ENERGY_SUM, deviceDTO);
    }

    @Override
    protected String getSolarWattLabel() {
        return "PowerMeter";
    }
}
