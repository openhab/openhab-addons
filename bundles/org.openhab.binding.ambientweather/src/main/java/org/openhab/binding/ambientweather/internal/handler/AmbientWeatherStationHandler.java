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
package org.openhab.binding.ambientweather.internal.handler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ambientweather.internal.config.StationConfig;
import org.openhab.binding.ambientweather.internal.processor.ProcessorFactory;
import org.openhab.binding.ambientweather.internal.processor.ProcessorNotFoundException;
import org.openhab.core.common.AbstractUID;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
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
    private @Nullable String macAddress;

    // Short name for logging station type
    private String station;

    // Time zone provider representing time zone configured in openHAB config
    TimeZoneProvider timeZoneProvider;

    public AmbientWeatherStationHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);

        this.timeZoneProvider = timeZoneProvider;

        // Name of station thing type used in logging
        String s = thing.getThingTypeUID().getAsString();
        station = s.substring(s.indexOf(AbstractUID.SEPARATOR) + 1).toUpperCase();
    }

    @Override
    public void initialize() {
        macAddress = getConfigAs(StationConfig.class).macAddress;
        logger.debug("Station {}: Initializing station handler for MAC {}", station, macAddress);
        try {
            ProcessorFactory.getProcessor(thing).setChannelGroupId();
            ProcessorFactory.getProcessor(thing).setNumberOfSensors();
        } catch (ProcessorNotFoundException e) {
            logger.warn("Station {}: Unable to set channel group Id and/or number of sensors: {}", station,
                    e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
            return;
        }
        Thing bridge = getBridge();
        if (bridge != null) {
            logger.debug("Station {}: Set station status to match bridge status: {}", station, bridge.getStatus());
            updateStatus(bridge.getStatus());
        }
    }

    @Override
    public void dispose() {
        macAddress = getConfigAs(StationConfig.class).macAddress;
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

    public void updateQuantity(String groupId, String channelId, @Nullable Number value, Unit<?> unit) {
        String channel = groupId + "#" + channelId;
        if (value != null && isLinked(channel)) {
            updateState(channel, new QuantityType<>(value, unit));
        }
    }

    public void updateString(String groupId, String channelId, @Nullable String value) {
        String channel = groupId + "#" + channelId;
        if (value != null && isLinked(channel)) {
            updateState(channel, new StringType(value));
        }
    }

    public void updateNumber(String groupId, String channelId, @Nullable Number value) {
        String channel = groupId + "#" + channelId;
        if (value != null && isLinked(channel)) {
            if (value instanceof Integer) {
                updateState(channel, new DecimalType(value.intValue()));
            } else if (value instanceof Double) {
                updateState(channel, new DecimalType(value.doubleValue()));
            }
        }
    }

    public void updateDate(String groupId, String channelId, @Nullable String date) {
        String channel = groupId + "#" + channelId;
        if (date != null && isLinked(channel)) {
            updateState(channel, getLocalDateTimeType(date, getZoneId()));
        }
    }

    private DateTimeType getLocalDateTimeType(String dateTimeString, ZoneId zoneId) {
        DateTimeType dateTimeType;
        try {
            Instant instant = Instant.parse(dateTimeString);
            ZonedDateTime localDateTime = instant.atZone(zoneId);
            dateTimeType = new DateTimeType(localDateTime);
        } catch (DateTimeParseException e) {
            logger.debug("Error parsing date/time string: {}", e.getMessage());
            dateTimeType = new DateTimeType();
        } catch (IllegalArgumentException e) {
            logger.debug("Error converting to DateTimeType: {}", e.getMessage());
            dateTimeType = new DateTimeType();
        }
        return dateTimeType;
    }

    private ZoneId getZoneId() {
        return timeZoneProvider.getTimeZone();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Handler doesn't support any commands
    }
}
