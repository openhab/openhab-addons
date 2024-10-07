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
package org.openhab.binding.homewizard.internal.handler;

import java.time.DateTimeException;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.dto.DataPayload;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link HomeWizardP1MeterHandler} implements functionality to handle a HomeWizard P1 Meter.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class HomeWizardP1MeterHandler extends HomeWizardDeviceHandler {

    private String meterModel = "";
    private int meterVersion = 0;
    private TimeZoneProvider timeZoneProvider;

    /**
     * Constructor
     *
     * @param thing The thing to handle
     * @param timeZoneProvider The TimeZoneProvider
     */
    public HomeWizardP1MeterHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    /**
     * Not listening to any commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Device specific handling of the returned payload.
     *
     * @param payload The data parsed from the Json file
     */
    @Override
    protected void handleDataPayload(DataPayload payload) {
        if (!meterModel.equals(payload.getMeterModel())) {
            meterModel = payload.getMeterModel();
            updateProperty(HomeWizardBindingConstants.PROPERTY_METER_MODEL, meterModel);
        }

        if (meterVersion != payload.getSmrVersion()) {
            meterVersion = payload.getSmrVersion();
            updateProperty(HomeWizardBindingConstants.PROPERTY_METER_VERSION, String.format("%d", meterVersion));
        }

        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1,
                new QuantityType<>(payload.getTotalEnergyImportT1Kwh(), Units.KILOWATT_HOUR));
        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T2,
                new QuantityType<>(payload.getTotalEnergyImportT2Kwh(), Units.KILOWATT_HOUR));
        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1,
                new QuantityType<>(payload.getTotalEnergyExportT1Kwh(), Units.KILOWATT_HOUR));
        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T2,
                new QuantityType<>(payload.getTotalEnergyExportT2Kwh(), Units.KILOWATT_HOUR));

        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER,
                new QuantityType<>(payload.getActivePowerW(), Units.WATT));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L1,
                new QuantityType<>(payload.getActivePowerL1W(), Units.WATT));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L2,
                new QuantityType<>(payload.getActivePowerL2W(), Units.WATT));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L3,
                new QuantityType<>(payload.getActivePowerL3W(), Units.WATT));

        updateState(HomeWizardBindingConstants.CHANNEL_POWER_FAILURES, new DecimalType(payload.getAnyPowerFailCount()));
        updateState(HomeWizardBindingConstants.CHANNEL_LONG_POWER_FAILURES,
                new DecimalType(payload.getLongPowerFailCount()));

        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT,
                new QuantityType<>(payload.getActiveCurrent(), Units.AMPERE));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT_L1,
                new QuantityType<>(payload.getActiveCurrentL1(), Units.AMPERE));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT_L2,
                new QuantityType<>(payload.getActiveCurrentL2(), Units.AMPERE));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT_L3,
                new QuantityType<>(payload.getActiveCurrentL3(), Units.AMPERE));

        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE,
                new QuantityType<>(payload.getActiveVoltage(), Units.VOLT));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE_L1,
                new QuantityType<>(payload.getActiveVoltageL1(), Units.VOLT));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE_L2,
                new QuantityType<>(payload.getActiveVoltageL2(), Units.VOLT));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE_L3,
                new QuantityType<>(payload.getActiveVoltageL3(), Units.VOLT));

        ZonedDateTime gasTimestamp;
        try {
            gasTimestamp = payload.getGasTimestamp(timeZoneProvider.getTimeZone());
        } catch (DateTimeException e) {
            logger.warn("Unable to parse Gas timestamp: {}", e.getMessage());
            gasTimestamp = null;
        }
        if (gasTimestamp != null) {
            updateState(HomeWizardBindingConstants.CHANNEL_GAS_TOTAL,
                    new QuantityType<>(payload.getTotalGasM3(), SIUnits.CUBIC_METRE));
            updateState(HomeWizardBindingConstants.CHANNEL_GAS_TIMESTAMP, new DateTimeType(gasTimestamp));
        }
    }
}
