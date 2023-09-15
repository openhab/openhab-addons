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
package org.openhab.binding.solax.internal;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.connectivity.LocalHttpConnector;
import org.openhab.binding.solax.internal.connectivity.rawdata.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterData;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link SolaxLocalAccessHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxLocalAccessHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SolaxLocalAccessHandler.class);

    private static final int INITIAL_SCHEDULE_DELAY_SECONDS = 5;

    private @NonNullByDefault({}) LocalHttpConnector localHttpConnector;

    private @Nullable ScheduledFuture<?> schedule;

    public SolaxLocalAccessHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        SolaxConfiguration config = getConfigAs(SolaxConfiguration.class);
        localHttpConnector = new LocalHttpConnector(config.password, config.hostname);
        int refreshInterval = config.refreshInterval;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        logger.debug("Scheduling regular interval retrieval every {} {}", refreshInterval, timeUnit);
        schedule = scheduler.scheduleWithFixedDelay(this::retrieveData, INITIAL_SCHEDULE_DELAY_SECONDS, refreshInterval,
                timeUnit);
    }

    private void retrieveData() {
        try {
            String rawJsonData = localHttpConnector.retrieveData();
            logger.debug("Raw data retrieved = {}", rawJsonData);

            if (rawJsonData != null && !rawJsonData.isEmpty()) {
                updateData(rawJsonData);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        SolaxBindingConstants.I18N_KEY_OFFLINE_COMMUNICATION_ERROR_JSON_CANNOT_BE_RETRIEVED);
            }
        } catch (IOException e) {
            logger.debug("Exception received while attempting to retrieve data via HTTP", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateData(String rawJsonData) {
        try {
            LocalConnectRawDataBean inverterParsedData = parseJson(rawJsonData);
            updateThing(inverterParsedData);
        } catch (JsonParseException e) {
            logger.debug("Unable to deserialize from JSON.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateThing(LocalConnectRawDataBean inverterParsedData) {
        transferInverterDataToChannels(inverterParsedData);

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private LocalConnectRawDataBean parseJson(String rawJsonData) {
        LocalConnectRawDataBean inverterParsedData = LocalConnectRawDataBean.fromJson(rawJsonData);
        logger.debug("Received a new inverter data object. Data = {}", inverterParsedData.toStringDetailed());
        return inverterParsedData;
    }

    private void transferInverterDataToChannels(InverterData data) {
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, data.getWifiSerial());
        updateProperty(SolaxBindingConstants.PROPERTY_INVERTER_TYPE, data.getInverterType().name());

        updateState(SolaxBindingConstants.INVERTER_OUTPUT_POWER,
                new QuantityType<>(data.getInverterOutputPower(), Units.WATT));
        updateState(SolaxBindingConstants.INVERTER_OUTPUT_CURRENT,
                new QuantityType<>(data.getInverterCurrent(), Units.AMPERE));
        updateState(SolaxBindingConstants.INVERTER_OUTPUT_VOLTAGE,
                new QuantityType<>(data.getInverterVoltage(), Units.VOLT));
        updateState(SolaxBindingConstants.INVERTER_OUTPUT_FREQUENCY,
                new QuantityType<>(data.getInverterFrequency(), Units.HERTZ));

        updateState(SolaxBindingConstants.INVERTER_PV1_POWER, new QuantityType<>(data.getPV1Power(), Units.WATT));
        updateState(SolaxBindingConstants.INVERTER_PV1_CURRENT, new QuantityType<>(data.getPV1Current(), Units.AMPERE));
        updateState(SolaxBindingConstants.INVERTER_PV1_VOLTAGE, new QuantityType<>(data.getPV1Voltage(), Units.VOLT));

        updateState(SolaxBindingConstants.INVERTER_PV2_POWER, new QuantityType<>(data.getPV2Power(), Units.WATT));
        updateState(SolaxBindingConstants.INVERTER_PV2_CURRENT, new QuantityType<>(data.getPV2Current(), Units.AMPERE));
        updateState(SolaxBindingConstants.INVERTER_PV2_VOLTAGE, new QuantityType<>(data.getPV2Voltage(), Units.VOLT));

        updateState(SolaxBindingConstants.INVERTER_PV_TOTAL_POWER,
                new QuantityType<>(data.getPVTotalPower(), Units.WATT));
        updateState(SolaxBindingConstants.INVERTER_PV_TOTAL_CURRENT,
                new QuantityType<>(data.getPVTotalCurrent(), Units.AMPERE));

        updateState(SolaxBindingConstants.BATTERY_POWER, new QuantityType<>(data.getBatteryPower(), Units.WATT));
        updateState(SolaxBindingConstants.BATTERY_CURRENT, new QuantityType<>(data.getBatteryCurrent(), Units.AMPERE));
        updateState(SolaxBindingConstants.BATTERY_VOLTAGE, new QuantityType<>(data.getBatteryVoltage(), Units.VOLT));
        updateState(SolaxBindingConstants.BATTERY_TEMPERATURE,
                new QuantityType<>(data.getBatteryTemperature(), SIUnits.CELSIUS));
        updateState(SolaxBindingConstants.BATTERY_STATE_OF_CHARGE,
                new QuantityType<>(data.getBatterySoC(), Units.PERCENT));

        updateState(SolaxBindingConstants.FEED_IN_POWER, new QuantityType<>(data.getFeedInPower(), Units.WATT));

        updateState(SolaxBindingConstants.TIMESTAMP, new DateTimeType(ZonedDateTime.now()));
        updateState(SolaxBindingConstants.RAW_DATA, new StringType(data.getRawData()));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do here as of now. Maybe implement a REFRESH command in the future.
    }

    @Override
    public void dispose() {
        super.dispose();
        ScheduledFuture<?> schedule = this.schedule;
        if (schedule != null) {
            schedule.cancel(true);
            this.schedule = null;
        }
    }
}
