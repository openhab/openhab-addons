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
package org.openhab.binding.homewizard.internal;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
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

    /**
     * Constructor
     *
     * @param thing The thing to handle
     */
    public HomeWizardP1MeterHandler(Thing thing) {
        super(thing);
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

        // If no data from the gas meter is present, the json value will be null, which means gson ignores it,
        // leaving the value in the payload object at 0.
        long dtv = payload.getGasTimestamp();
        if (dtv > 0) {
            updateState(HomeWizardBindingConstants.CHANNEL_TOTAL_GAS,
                    new QuantityType<>(payload.getTotalGasM3(), SIUnits.CUBIC_METRE));

            // 210119164000
            int seconds = (int) (dtv % 100);

            dtv /= 100;
            int minutes = (int) (dtv % 100);

            dtv /= 100;
            int hours = (int) (dtv % 100);

            dtv /= 100;
            int day = (int) (dtv % 100);

            dtv /= 100;
            int month = (int) (dtv % 100);

            dtv /= 100;
            int year = (int) (dtv + 2000);

            try {
                DateTimeType dtt = new DateTimeType(
                        ZonedDateTime.of(year, month, day, hours, minutes, seconds, 0, ZoneId.systemDefault()));
                updateState(HomeWizardBindingConstants.CHANNEL_GAS_TIMESTAMP, dtt);
                updateState(HomeWizardBindingConstants.CHANNEL_TOTAL_GAS,
                        new QuantityType<>(payload.getTotalGasM3(), SIUnits.CUBIC_METRE));
            } catch (DateTimeException e) {
                logger.warn("Unable to parse Gas timestamp: {}", payload.getGasTimestamp());
            }
        }
    }
}
