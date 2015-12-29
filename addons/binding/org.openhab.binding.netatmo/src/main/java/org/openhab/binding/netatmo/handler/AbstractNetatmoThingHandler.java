/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NADashboardData;

/**
 * {@link AbstractNetatmoThingHandler} is the abstract class that handles
 * common behaviors of both Devices and Modules
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
abstract class AbstractNetatmoThingHandler extends BaseThingHandler {
    private static Logger logger = LoggerFactory.getLogger(AbstractNetatmoThingHandler.class);
    private final List<Integer> signalThresholds = new ArrayList<Integer>();
    protected final String actualApp;

    protected NetatmoBridgeHandler bridgeHandler;
    protected NADashboardData dashboard;

    AbstractNetatmoThingHandler(Thing thing) {
        super(thing);
        Map<String, String> properties = thing.getProperties();
        List<String> thresholds = Arrays.asList(properties.get(PROPERTY_SIGNAL_LEVELS).split(","));
        for (String threshold : thresholds) {
            signalThresholds.add(Integer.parseInt(threshold));
        }
        actualApp = properties.get(PROPERTY_ACTUAL_APP);
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        super.bridgeHandlerInitialized(thingHandler, bridge);
        bridgeHandler = (NetatmoBridgeHandler) thingHandler;
    }

    int getSignalStrength(int signalLevel) {
        // Take in account #3995
        int level;
        for (level = 0; level < signalThresholds.size(); level++) {
            if (signalLevel > signalThresholds.get(level)) {
                break;
            }
        }
        return level;
    }

    protected State getNAThingProperty(String chanelId) {
        switch (chanelId) {
            case CHANNEL_TIMEUTC:
                return new DateTimeType(timestampToCalendar(dashboard.getTimeUtc()));
            case CHANNEL_TEMPERATURE:
                return new DecimalType(dashboard.getTemperature());
            case CHANNEL_DATE_MAX_TEMP:
                return new DateTimeType(timestampToCalendar(dashboard.getDateMaxTemp()));
            case CHANNEL_DATE_MIN_TEMP:
                return new DateTimeType(timestampToCalendar(dashboard.getDateMinTemp()));
            case CHANNEL_MAX_TEMP:
                return new DecimalType(dashboard.getMaxTemp());
            case CHANNEL_MIN_TEMP:
                return new DecimalType(dashboard.getMinTemp());
            case CHANNEL_HUMIDEX:
                return new DecimalType(WeatherUtils.getHumidex(dashboard.getTemperature(), dashboard.getHumidity()));
            case CHANNEL_DEWPOINT:
                return new DecimalType(WeatherUtils.getDewPoint(dashboard.getTemperature(), dashboard.getHumidity()));
            case CHANNEL_DEWPOINTDEP:
                Double dewpoint = WeatherUtils.getDewPoint(dashboard.getTemperature(), dashboard.getHumidity());
                return new DecimalType(WeatherUtils.getDewPointDep(dashboard.getTemperature(), dewpoint));
            case CHANNEL_HEATINDEX:
                return new DecimalType(WeatherUtils.getHeatIndex(dashboard.getTemperature(), dashboard.getHumidity()));
            case CHANNEL_PRESSURE:
                return new DecimalType(dashboard.getPressure());
            case CHANNEL_ABSOLUTE_PRESSURE:
                return new DecimalType(dashboard.getAbsolutePressure());
            case CHANNEL_CO2:
                return new DecimalType(dashboard.getCO2());
            case CHANNEL_HUMIDITY:
                return new PercentType(dashboard.getHumidity().intValue());
            case CHANNEL_NOISE:
                return new DecimalType(dashboard.getNoise());

        }
        return null;
    }

    protected void updateChannels() {
        logger.debug("Updating device channels");

        for (Channel channel : getThing().getChannels()) {
            String chanelId = channel.getUID().getId();
            State state = getNAThingProperty(chanelId);
            if (state != null) {
                updateState(channel.getUID(), state);
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    protected Calendar timestampToCalendar(Integer netatmoTS) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(netatmoTS * 1000L);
        return calendar;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateChannels();
        } else {
            logger.warn("This Thing is read-only and can only handle REFRESH command");
        }
    }

}
