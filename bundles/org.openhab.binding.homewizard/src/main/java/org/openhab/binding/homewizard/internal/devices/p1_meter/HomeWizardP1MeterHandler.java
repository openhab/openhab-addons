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
package org.openhab.binding.homewizard.internal.devices.p1_meter;

import java.time.DateTimeException;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.devices.HomeWizardEnergyMeterHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link HomeWizardP1MeterHandler} implements functionality to handle a HomeWizard P1 Meter.
 *
 * @author Daniël van Os - Initial contribution
 * @author Gearrel Welvaart - Adapted to new structure
 */
@NonNullByDefault
public class HomeWizardP1MeterHandler extends HomeWizardEnergyMeterHandler {

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
        supportedTypes.add(HomeWizardBindingConstants.HWE_P1);
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
        super.handleDataPayload(data);

        var payload = gson.fromJson(data, HomeWizardP1MeterMeasurementPayload.class);
        if (payload != null) {
            if (!thing.getThingTypeUID().equals(HomeWizardBindingConstants.THING_TYPE_P1_METER)) {
                if (!meterModel.equals(payload.getMeterModel())) {
                    meterModel = payload.getMeterModel();
                    updateProperty(HomeWizardBindingConstants.PROPERTY_METER_MODEL, meterModel);
                }

                if (meterVersion != payload.getProtocolVersion()) {
                    meterVersion = payload.getProtocolVersion();
                    updateProperty(HomeWizardBindingConstants.PROPERTY_METER_VERSION,
                            String.format("%d", meterVersion));
                }

                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_FAILURES,
                        new DecimalType(payload.getAnyPowerFailCount()));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_LONG_POWER_FAILURES,
                        new DecimalType(payload.getLongPowerFailCount()));

                ZonedDateTime gasTimestamp;
                try {
                    gasTimestamp = payload.getGasTimestamp(timeZoneProvider.getTimeZone());
                } catch (DateTimeException e) {
                    logger.warn("Unable to parse Gas timestamp: {}", e.getMessage());
                    gasTimestamp = null;
                }
                if (gasTimestamp != null) {
                    updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                            HomeWizardBindingConstants.CHANNEL_GAS_TOTAL,
                            new QuantityType<>(payload.getTotalGasM3(), SIUnits.CUBIC_METRE));
                    updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                            HomeWizardBindingConstants.CHANNEL_GAS_TIMESTAMP, new DateTimeType(gasTimestamp));
                }
            } else {
                if (!meterModel.equals(payload.getMeterModel())) {
                    meterModel = payload.getMeterModel();
                    updateProperty(HomeWizardBindingConstants.PROPERTY_METER_MODEL, meterModel);
                }

                if (meterVersion != payload.getProtocolVersion()) {
                    meterVersion = payload.getProtocolVersion();
                    updateProperty(HomeWizardBindingConstants.PROPERTY_METER_VERSION,
                            String.format("%d", meterVersion));
                }

                updateState(HomeWizardBindingConstants.CHANNEL_POWER_FAILURES,
                        new DecimalType(payload.getAnyPowerFailCount()));
                updateState(HomeWizardBindingConstants.CHANNEL_LONG_POWER_FAILURES,
                        new DecimalType(payload.getLongPowerFailCount()));

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
    }
}
