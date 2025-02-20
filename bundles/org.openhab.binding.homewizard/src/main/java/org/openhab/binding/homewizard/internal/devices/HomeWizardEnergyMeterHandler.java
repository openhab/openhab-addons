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
package org.openhab.binding.homewizard.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link HomeWizardEnergyMeterHandler} implements functionality generic to several energy meters.
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
     * @param data The data obtained form the API call
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

                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_FREQUENCY,
                        new QuantityType<>(payload.getFrequency(), Units.HERTZ));
            } else {
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_IMPORT_T1,
                        new QuantityType<>(payload.getEnergyImportT1(), Units.KILOWATT_HOUR));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_IMPORT_T2,
                        new QuantityType<>(payload.getEnergyImportT2(), Units.KILOWATT_HOUR));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_EXPORT_T1,
                        new QuantityType<>(payload.getEnergyExportT1(), Units.KILOWATT_HOUR));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_EXPORT_T2,
                        new QuantityType<>(payload.getEnergyExportT2(), Units.KILOWATT_HOUR));

                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_POWER,
                        new QuantityType<>(payload.getPower(), Units.WATT));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_POWER_L1,
                        new QuantityType<>(payload.getPowerL1(), Units.WATT));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_POWER_L2,
                        new QuantityType<>(payload.getPowerL2(), Units.WATT));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_POWER_L3,
                        new QuantityType<>(payload.getPowerL3(), Units.WATT));

                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE,
                        new QuantityType<>(payload.getVoltage(), Units.VOLT));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE_L1,
                        new QuantityType<>(payload.getVoltageL1(), Units.VOLT));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE_L2,
                        new QuantityType<>(payload.getVoltageL2(), Units.VOLT));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE_L3,
                        new QuantityType<>(payload.getVoltageL3(), Units.VOLT));

                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT,
                        new QuantityType<>(payload.getCurrent(), Units.AMPERE));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT_L1,
                        new QuantityType<>(payload.getCurrentL1(), Units.AMPERE));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT_L2,
                        new QuantityType<>(payload.getCurrentL2(), Units.AMPERE));
                updateState(HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT_L3,
                        new QuantityType<>(payload.getCurrentL3(), Units.AMPERE));
            }

        }
    }
}
