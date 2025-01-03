/**
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
package org.openhab.binding.homewizard.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.devices.dto.HomeWizardEnergyMeterMeasurementPayload;
import org.openhab.binding.homewizard.internal.devices.p1_meter.HomeWizardP1MeterHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link HomeWizardP1MeterHandler} implements functionality to handle a HomeWizard P1 Meter.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class HomeWizardEnergyMeterHandler extends HomeWizardDeviceHandler {

    /**
     * Constructor
     *
     * @param thing The thing to handle
     * @param timeZoneProvider The TimeZoneProvider
     */
    public HomeWizardEnergyMeterHandler(Thing thing) {
        super(thing);
    }

    /**
     * Not listening to any commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Device specific handling of the returned data.
     *
     * @param payload The data obtained form the API call
     */
    @Override
    protected void handleDataPayload(String data) {
        var payload = gson.fromJson(data, HomeWizardEnergyMeterMeasurementPayload.class);
        if (payload != null) {
            if (!thing.getThingTypeUID().equals(HomeWizardBindingConstants.THING_TYPE_P1_METER)
                    && !thing.getThingTypeUID().equals(HomeWizardBindingConstants.THING_TYPE_ENERGY_SOCKET)) {
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT,
                        new QuantityType<>(payload.getEnergyImport(), Units.KILOWATT_HOUR));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1,
                        new QuantityType<>(payload.getEnergyImportT1(), Units.KILOWATT_HOUR));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T2,
                        new QuantityType<>(payload.getEnergyImportT2(), Units.KILOWATT_HOUR));

                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT,
                        new QuantityType<>(payload.getEnergyExport(), Units.KILOWATT_HOUR));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1,
                        new QuantityType<>(payload.getEnergyExportT1(), Units.KILOWATT_HOUR));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T2,
                        new QuantityType<>(payload.getEnergyExportT2(), Units.KILOWATT_HOUR));

                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY, HomeWizardBindingConstants.CHANNEL_POWER,
                        new QuantityType<>(payload.getPower(), Units.WATT));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L1,
                        new QuantityType<>(payload.getPowerL1(), Units.WATT));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L2,
                        new QuantityType<>(payload.getPowerL2(), Units.WATT));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L3,
                        new QuantityType<>(payload.getPowerL3(), Units.WATT));

                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY, HomeWizardBindingConstants.CHANNEL_CURRENT,
                        new QuantityType<>(payload.getCurrent(), Units.AMPERE));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L1,
                        new QuantityType<>(payload.getCurrentL1(), Units.AMPERE));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L2,
                        new QuantityType<>(payload.getCurrentL2(), Units.AMPERE));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L3,
                        new QuantityType<>(payload.getCurrentL3(), Units.AMPERE));

                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY, HomeWizardBindingConstants.CHANNEL_VOLTAGE,
                        new QuantityType<>(payload.getVoltage(), Units.VOLT));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L1,
                        new QuantityType<>(payload.getVoltageL1(), Units.VOLT));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L2,
                        new QuantityType<>(payload.getVoltageL2(), Units.VOLT));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L3,
                        new QuantityType<>(payload.getVoltageL3(), Units.VOLT));
            } else {
                updateState("total_energy_import_t1",
                        new QuantityType<>(payload.getEnergyImportT1(), Units.KILOWATT_HOUR));
                updateState("total_energy_import_t2",
                        new QuantityType<>(payload.getEnergyImportT2(), Units.KILOWATT_HOUR));
                updateState("total_energy_export_t1",
                        new QuantityType<>(payload.getEnergyExportT1(), Units.KILOWATT_HOUR));
                updateState("total_energy_export_t2",
                        new QuantityType<>(payload.getEnergyExportT2(), Units.KILOWATT_HOUR));

                updateState("active_power", new QuantityType<>(payload.getPower(), Units.WATT));
                updateState("active_power_l1", new QuantityType<>(payload.getPowerL1(), Units.WATT));
                updateState("active_power_l2", new QuantityType<>(payload.getPowerL2(), Units.WATT));
                updateState("active_power_l3", new QuantityType<>(payload.getPowerL3(), Units.WATT));

                updateState("active_voltage", new QuantityType<>(payload.getVoltage(), Units.VOLT));
                updateState("active_voltage_l1", new QuantityType<>(payload.getVoltageL1(), Units.VOLT));
                updateState("active_voltage_l2", new QuantityType<>(payload.getVoltageL2(), Units.VOLT));
                updateState("active_voltage_l3", new QuantityType<>(payload.getVoltageL3(), Units.VOLT));

                updateState("active_current", new QuantityType<>(payload.getCurrent(), Units.AMPERE));
                updateState("active_current_l1", new QuantityType<>(payload.getCurrentL1(), Units.AMPERE));
                updateState("active_current_l2", new QuantityType<>(payload.getCurrentL2(), Units.AMPERE));
                updateState("active_current_l3", new QuantityType<>(payload.getCurrentL3(), Units.AMPERE));
            }

        }
    }
}
