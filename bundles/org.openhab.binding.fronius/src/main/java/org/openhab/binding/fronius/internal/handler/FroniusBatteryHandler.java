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
package org.openhab.binding.fronius.internal.handler;

import java.time.Instant;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fronius.internal.FroniusBaseDeviceConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.api.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageController;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageDetails;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageRealtimeBody;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageRealtimeBodyData;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageRealtimeResponse;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FroniusBatteryHandler} polls the GetStorageRealtimeData endpoint and
 * maps fields from the Controller node to channels and thing properties.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class FroniusBatteryHandler extends FroniusBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusBatteryHandler.class);

    private @Nullable StorageController controller;
    private @Nullable FroniusBaseDeviceConfiguration config;

    public FroniusBatteryHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getDescription() {
        return "Fronius Battery";
    }

    @Override
    public void initialize() {
        config = getConfigAs(FroniusBaseDeviceConfiguration.class);
        super.initialize();
    }

    @Override
    protected void handleRefresh(FroniusBridgeConfiguration bridgeConfiguration) throws FroniusCommunicationException {
        FroniusBaseDeviceConfiguration config = this.config;
        if (config == null) {
            logger.warn("config is null in handleRefresh(), this is a bug, please report it.");
            return;
        }
        updateData(bridgeConfiguration, config);
        updateChannels();
        updateProperties();
    }

    @Override
    protected @Nullable State getValue(String channelId) {
        StorageController local = controller;
        if (local == null) {
            return null;
        }

        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }
        final String fieldName = fields[0];

        return switch (fieldName) {
            case FroniusBindingConstants.BATTERY_CAPACITY_MAXIMUM ->
                new QuantityType<>(local.getCapacityMaximum(), Units.WATT_HOUR);
            case FroniusBindingConstants.BATTERY_DESIGNED_CAPACITY ->
                new QuantityType<>(local.getDesignedCapacity(), Units.WATT_HOUR);
            case FroniusBindingConstants.BATTERY_CURRENT_DC -> new QuantityType<>(local.getCurrentDC(), Units.AMPERE);
            case FroniusBindingConstants.BATTERY_VOLTAGE_DC -> new QuantityType<>(local.getVoltageDC(), Units.VOLT);
            case FroniusBindingConstants.BATTERY_STATE_OF_CHARGE ->
                new QuantityType<>(local.getStateOfChargeRelative(), Units.PERCENT);
            case FroniusBindingConstants.BATTERY_ENABLE -> new DecimalType(local.getEnable());
            case FroniusBindingConstants.BATTERY_STATUS_BATTERY_CELL -> new StringType(local.getStatusBatteryCell());
            case FroniusBindingConstants.BATTERY_TEMPERATURE_CELL ->
                new QuantityType<>(local.getTemperatureCell(), SIUnits.CELSIUS);
            case FroniusBindingConstants.BATTERY_TIMESTAMP ->
                new DateTimeType(Instant.ofEpochSecond((long) local.getTimeStamp()));
            default -> null;
        };
    }

    private void updateData(FroniusBridgeConfiguration bridgeConfiguration, FroniusBaseDeviceConfiguration config)
            throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getBatteryDataUrl(bridgeConfiguration.scheme,
                bridgeConfiguration.hostname, config.deviceId);
        StorageRealtimeResponse response = collectDataFromUrl(StorageRealtimeResponse.class, location);

        this.controller = Optional.ofNullable(response) //
                .map(StorageRealtimeResponse::getBody) //
                .map(StorageRealtimeBody::getData) //
                .map(StorageRealtimeBodyData::getController) //
                .orElse(null);
    }

    private void updateProperties() {
        StorageController local = controller;
        if (local == null) {
            return;
        }
        StorageDetails details = local.getDetails();
        if (details == null) {
            return;
        }

        var properties = editProperties();
        properties.put(Thing.PROPERTY_VENDOR, details.getManufacturer());
        properties.put(Thing.PROPERTY_MODEL_ID, details.getModel());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, details.getSerial());
        updateProperties(properties);
    }
}
