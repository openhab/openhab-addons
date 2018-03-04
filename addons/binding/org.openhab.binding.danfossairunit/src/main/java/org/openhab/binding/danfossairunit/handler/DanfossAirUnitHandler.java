/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.danfossairunit.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.danfossairunit.internal.DanfossAirUnitConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DanfossAirUnitHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ralf Duckstein - Initial contribution
 */
public class DanfossAirUnitHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DanfossAirUnitHandler.class);
    private DanfossAirUnit airUnit;
    private ScheduledFuture<?> pollingJobSensors;
    private ScheduledFuture<?> pollingJobActors;

    private DanfossAirUnitConfiguration config;

    // channel ids

    // main channels
    private static final String CHANNEL_MODE = "mode";
    private static final String CHANNEL_FAN_SPEED = "fanSpeed";
    private static final String CHANNEL_BOOST = "boost";

    // main temperatures
    private static final String CHANNEL_ROOM_TEMPERATURE = "roomTemperature";
    private static final String CHANNEL_OUTDOOR_TEMPERATURE = "outdoorTemperature";

    // recuperator channels
    private static final String CHANNEL_BYPASS = "bypass";
    private static final String CHANNEL_SUPPLY_TEMPERATURE = "supplyTemperature";
    private static final String CHANNEL_EXTRACT_TEMPERATURE = "extractTemperature";
    private static final String CHANNEL_EXHAUST_TEMPERATURE = "exhaustTemperature";

    // humidity
    private static final String CHANNEL_HUMIDITY = "humidity";

    // service channels
    private static final String CHANNEL_BATTERY_LIFE = "batteryLife";
    private static final String CHANNEL_FILTER_LIFE = "filterLife";

    public DanfossAirUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            updateSensors();
            updateActors();
        } else if (channelUID.getId().equals(CHANNEL_FAN_SPEED)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        logger.trace("Initializing thing");

        config = getConfigAs(DanfossAirUnitConfiguration.class);

        try {
            airUnit = new DanfossAirUnit(InetAddress.getByName(config.host), config.port);
            updateStatus(ThingStatus.ONLINE);
        } catch (UnknownHostException uhe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, uhe.getMessage());
            return;
        } catch (IOException ioe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ioe.getMessage());
            return;
        }

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");

        if (config.sensorPolling > 0) {
            pollingJobSensors = scheduler.scheduleWithFixedDelay(() -> updateSensors(), 0, config.sensorPolling,
                    TimeUnit.SECONDS);
            logger.debug("Sensor polling job scheduled to run every {} sec. for '{}'", config.sensorPolling,
                    getThing().getUID());
        }

        if (config.settingPolling > 0) {
            pollingJobActors = scheduler.scheduleWithFixedDelay(() -> updateActors(), 0, config.settingPolling,
                    TimeUnit.SECONDS);
            logger.debug("Sensor polling job scheduled to run every {} sec. for '{}'", config.settingPolling,
                    getThing().getUID());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Danfoss Air Unit handler '{}'", getThing().getUID());

        if (pollingJobSensors != null) {
            pollingJobSensors.cancel(true);
            pollingJobSensors = null;
        }
        // driver.shutdown();
        // driver = null;
        airUnit.disconnect();
        airUnit = null;
    }

    private synchronized void updateSensors() {
        logger.debug("Updating Danfoss Air Unit Sensor data '{}'", getThing().getUID());

        try {
            updateState(CHANNEL_HUMIDITY, airUnit.getHumidity());
            updateState(CHANNEL_ROOM_TEMPERATURE, airUnit.getRoomTemperature());
            updateState(CHANNEL_OUTDOOR_TEMPERATURE, airUnit.getOutdoorTemperature());
            updateState(CHANNEL_SUPPLY_TEMPERATURE, airUnit.getSupplyTemperature());
            updateState(CHANNEL_EXTRACT_TEMPERATURE, airUnit.getExtractTemperature());
            updateState(CHANNEL_EXHAUST_TEMPERATURE, airUnit.getExhaustTemperature());
            updateState(CHANNEL_BATTERY_LIFE, airUnit.getBatteryLife());
            updateState(CHANNEL_FILTER_LIFE, airUnit.getFilterLife());

            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private synchronized void updateActors() {
        logger.debug("Updating Danfoss Air Unit Actor data '{}'", getThing().getUID());

        try {
            updateState(CHANNEL_MODE, airUnit.getMode());
            updateState(CHANNEL_FAN_SPEED, airUnit.getFanSpeed());
            updateState(CHANNEL_BOOST, airUnit.getBoost());
            updateState(CHANNEL_BYPASS, airUnit.getBypass());

            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
