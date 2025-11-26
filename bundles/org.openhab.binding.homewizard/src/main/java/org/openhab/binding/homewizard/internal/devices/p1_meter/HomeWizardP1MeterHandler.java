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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.devices.HomeWizardEnergyMeterHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link HomeWizardP1MeterHandler} implements functionality to handle a HomeWizard P1 Meter.
 *
 * @author Daniël van Os - Initial contribution
 * @author Gearrel Welvaart - Adapted to new structure
 */
@NonNullByDefault
public class HomeWizardP1MeterHandler extends HomeWizardEnergyMeterHandler {

    private final String BATTERIES_URL = "batteries";

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

    @Override
    protected void retrieveData() {
        super.retrieveData();
        if (config.apiVersion == 2) {
            retrieveBatteriesData();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            retrieveBatteriesData();
            return;
        }

        if (channelUID.getIdWithoutGroup().equals(HomeWizardBindingConstants.CHANNEL_BATTERIES_MODE)) {
            var cmd = String.format("{\"mode\": \"%s\"}", command.toFullString());

            try {
                var response = putDataTo(apiURL + BATTERIES_URL, cmd);
                if (response.getStatus() == HttpStatus.OK_200) {
                    handleBatteriesData(response.getContentAsString());
                } else {
                    logger.warn("Failed to send command {} to {}", command, apiURL + BATTERIES_URL);
                }
            } catch (Exception ex) {
                logger.warn("Failed to send command {} to {}", command, apiURL + BATTERIES_URL);
            }
        } else {
            logger.warn("Should handle {} {}", channelUID.getIdWithoutGroup(), command);
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

    /**
     * Device specific handling of the returned batteries data.
     *
     * @param data The data obtained form the API call
     */
    protected void handleBatteriesData(String data) {
        HomeWizardP1MeterBatteriesPayload payload = null;
        try {
            payload = gson.fromJson(data, HomeWizardP1MeterBatteriesPayload.class);
        } catch (JsonSyntaxException ex) {
            logger.warn("No Batteries data available");
        }
        if (payload != null) {
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_P1_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_MODE, new StringType(payload.getMode()));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_P1_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_POWER,
                    new QuantityType<>(payload.getPower(), Units.WATT));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_P1_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_TARGET_POWER,
                    new QuantityType<>(payload.getTargetPower(), Units.WATT));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_P1_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_MAX_CONSUMPTION,
                    new QuantityType<>(payload.getMaxConsumption(), Units.WATT));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_P1_BATTERIES,
                    HomeWizardBindingConstants.CHANNEL_BATTERIES_MAX_PRODUCTION,
                    new QuantityType<>(payload.getMaxProduction(), Units.WATT));
        }
    }

    protected void retrieveBatteriesData() {
        final String batteriesData;

        try {
            batteriesData = getBatteriesData();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Device is offline or doesn't support the API version"));
            return;
        }

        handleBatteriesData(batteriesData);
    }

    /**
     * @return json response from the batteries api
     * @throws InterruptedException, TimeoutException, ExecutionException
     */
    public String getBatteriesData() throws InterruptedException, TimeoutException, ExecutionException {
        var response = getResponseFrom(apiURL + BATTERIES_URL);
        if (response.getStatus() == HttpStatus.OK_200) {
            return response.getContentAsString();
        } else {
            logger.warn("No Batteries data available");
            return "";
        }
    }
}
