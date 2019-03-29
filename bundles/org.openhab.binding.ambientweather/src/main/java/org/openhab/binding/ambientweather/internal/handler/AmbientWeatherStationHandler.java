/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.ambientweather.internal.handler;

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.ambientweather.internal.config.StationConfig;
import org.openhab.binding.ambientweather.internal.processor.ProcessorFactory;
import org.openhab.binding.ambientweather.internal.processor.ProcessorNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmbientWeatherStationHandler} is responsible for processing
 * info and weather data updates.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AmbientWeatherStationHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(AmbientWeatherStationHandler.class);

    // MAC address for weather station handled by this thing handler
    @Nullable
    private String macAddress;

    // Short name for logging station type
    private String station;

    // Time zone provider representing time zone configured in openHAB config
    TimeZoneProvider timeZoneProvider;

    public AmbientWeatherStationHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);

        this.timeZoneProvider = timeZoneProvider;

        // Name of station thing type used in logging
        String s = thing.getThingTypeUID().getAsString();
        station = s.substring(s.indexOf(':') + 1).toUpperCase();
    }

    @Override
    public void initialize() {
        macAddress = getConfigAs(StationConfig.class).getMacAddress();
        logger.debug("Station {}: Initializing station handler for MAC {}", station, macAddress);
        Thing bridge = getBridge();
        if (bridge != null) {
            logger.debug("Station {}: Set station status to match bridge status: {}", station, bridge.getStatus());
            updateStatus(bridge.getStatus());
        }
    }

    @Override
    public void dispose() {
        macAddress = getConfigAs(StationConfig.class).getMacAddress();
        logger.debug("Station {}: Disposing station handler for MAC {}", station, macAddress);
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        ThingStatus bridgeStatus = bridgeStatusInfo.getStatus();
        logger.debug("Station {}: Detected bridge status changed to '{}', Update my status", station, bridgeStatus);
        if (bridgeStatus == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatus == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /*
     * Handle an update to the station name and location
     */
    public void handleInfoEvent(String mac, String name, String location) {
        logger.debug("Station {}: Update name={} and location={} for MAC {}", station, name, location, macAddress);
        try {
            ProcessorFactory.getProcessor(thing).processInfoUpdate(this, station, name, location);
        } catch (ProcessorNotFoundException e) {
            logger.debug("Unable to process info event: {}", e.getMessage());
        }
    }

    /*
     * Handle an update to the weather data.
     */
    public void handleWeatherDataEvent(String jsonData) {
        logger.debug("Station {}: Processing data event for MAC {}", station, macAddress);
        try {
            ProcessorFactory.getProcessor(thing).processWeatherData(this, station, jsonData);
        } catch (ProcessorNotFoundException e) {
            logger.debug("Unable to process weather data event: {}", e.getMessage());
        }
    }

    /*
     * Helper function called by the processor to update the channel state
     */
    public void updateChannel(String channelId, State state) {
        // Only update channel if it's linked
        if (isLinked(channelId)) {
            updateState(channelId, state);
        }
    }

    /*
     * Helper function called by the processor to get the time zone
     */
    public ZoneId getZoneId() {
        return timeZoneProvider.getTimeZone();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Handler doesn't support any commands
    }
}
