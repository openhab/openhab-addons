/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link HomeWizardEnergyMeterSubHandler} implements functionality generic to several energy meters.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class HomeWizardEnergyMeterSubHandler {

    protected final Logger logger = LoggerFactory.getLogger(HomeWizardEnergyMeterSubHandler.class);
    private final static Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    /**
     * Device specific handling of the returned data.
     *
     * @param data The data obtained from the API call
     */
    public static void handleMeasurementData(String data, HomeWizardDeviceHandler handler) {
        var payload = gson.fromJson(data, HomeWizardEnergyMeterMeasurementPayload.class);
        if (payload != null) {
            if (!handler.getThing().getThingTypeUID().equals(HomeWizardBindingConstants.THING_TYPE_P1_METER) && !handler
                    .getThing().getThingTypeUID().equals(HomeWizardBindingConstants.THING_TYPE_ENERGY_SOCKET)) {
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT,
                        new QuantityType<>(payload.getEnergyImport(), Units.KILOWATT_HOUR));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1,
                        new QuantityType<>(payload.getEnergyImportT1(), Units.KILOWATT_HOUR));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T2,
                        new QuantityType<>(payload.getEnergyImportT2(), Units.KILOWATT_HOUR));

                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT,
                        new QuantityType<>(payload.getEnergyExport(), Units.KILOWATT_HOUR));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1,
                        new QuantityType<>(payload.getEnergyExportT1(), Units.KILOWATT_HOUR));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T2,
                        new QuantityType<>(payload.getEnergyExportT2(), Units.KILOWATT_HOUR));

                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER, new QuantityType<>(payload.getPower(), Units.WATT));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L1,
                        new QuantityType<>(payload.getPowerL1(), Units.WATT));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L2,
                        new QuantityType<>(payload.getPowerL2(), Units.WATT));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L3,
                        new QuantityType<>(payload.getPowerL3(), Units.WATT));

                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT,
                        new QuantityType<>(payload.getCurrent(), Units.AMPERE));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L1,
                        new QuantityType<>(payload.getCurrentL1(), Units.AMPERE));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L2,
                        new QuantityType<>(payload.getCurrentL2(), Units.AMPERE));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L3,
                        new QuantityType<>(payload.getCurrentL3(), Units.AMPERE));

                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE,
                        new QuantityType<>(payload.getVoltage(), Units.VOLT));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L1,
                        new QuantityType<>(payload.getVoltageL1(), Units.VOLT));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L2,
                        new QuantityType<>(payload.getVoltageL2(), Units.VOLT));
                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L3,
                        new QuantityType<>(payload.getVoltageL3(), Units.VOLT));

                handler.updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_FREQUENCY,
                        new QuantityType<>(payload.getFrequency(), Units.HERTZ));
            } else {
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_IMPORT_T1,
                        new QuantityType<>(payload.getEnergyImportT1(), Units.KILOWATT_HOUR));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_IMPORT_T2,
                        new QuantityType<>(payload.getEnergyImportT2(), Units.KILOWATT_HOUR));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_EXPORT_T1,
                        new QuantityType<>(payload.getEnergyExportT1(), Units.KILOWATT_HOUR));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_EXPORT_T2,
                        new QuantityType<>(payload.getEnergyExportT2(), Units.KILOWATT_HOUR));

                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_POWER,
                        new QuantityType<>(payload.getPower(), Units.WATT));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_POWER_L1,
                        new QuantityType<>(payload.getPowerL1(), Units.WATT));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_POWER_L2,
                        new QuantityType<>(payload.getPowerL2(), Units.WATT));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_POWER_L3,
                        new QuantityType<>(payload.getPowerL3(), Units.WATT));

                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE,
                        new QuantityType<>(payload.getVoltage(), Units.VOLT));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE_L1,
                        new QuantityType<>(payload.getVoltageL1(), Units.VOLT));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE_L2,
                        new QuantityType<>(payload.getVoltageL2(), Units.VOLT));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE_L3,
                        new QuantityType<>(payload.getVoltageL3(), Units.VOLT));

                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT,
                        new QuantityType<>(payload.getCurrent(), Units.AMPERE));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT_L1,
                        new QuantityType<>(payload.getCurrentL1(), Units.AMPERE));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT_L2,
                        new QuantityType<>(payload.getCurrentL2(), Units.AMPERE));
                handler.updateState("", HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT_L3,
                        new QuantityType<>(payload.getCurrentL3(), Units.AMPERE));
            }

        }
    }
}
