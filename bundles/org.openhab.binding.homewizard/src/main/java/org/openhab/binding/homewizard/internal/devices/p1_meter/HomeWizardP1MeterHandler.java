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
package org.openhab.binding.homewizard.internal.devices.p1_meter;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.devices.HomeWizardBatteriesSubHandler;
import org.openhab.binding.homewizard.internal.devices.HomeWizardEnergyMeterHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link HomeWizardP1MeterHandler} implements functionality to handle a HomeWizard P1 Meter.
 *
 * @author Daniël van Os - Initial contribution
 * @author Gearrel Welvaart - Adapted to new structure
 */
@NonNullByDefault
public class HomeWizardP1MeterHandler extends HomeWizardEnergyMeterHandler {
    protected HomeWizardBatteriesSubHandler batteriesHandler;

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
        supportedApiVersions = Arrays.asList(API_V1, API_V2);

        batteriesHandler = new HomeWizardBatteriesSubHandler(this);
    }

    @Override
    protected void retrieveData() {
        super.retrieveData();
        if (config.isUsingApiVersion2()) {
            try {
                batteriesHandler.retrieveBatteriesData();
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-device-offline");
                return;
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            retrieveData();
            return;
        }

        if (channelUID.getIdWithoutGroup().equals(HomeWizardBindingConstants.CHANNEL_BATTERIES_MODE)) {
            batteriesHandler.handleCommand(command);
        } else {
            logger.warn("Unhandled command for channel: {} command: {}", channelUID.getIdWithoutGroup(), command);
        }
    }

    /**
     * Device specific handling of the returned measurement data.
     *
     * @param data The data obtained form the API call
     */
    @Override
    protected void handleMeasurementData(String data) {
        super.handleMeasurementData(data);

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

                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY, HomeWizardBindingConstants.CHANNEL_TARIFF,
                        new DecimalType(payload.getTariff()));

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
