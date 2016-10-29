/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoThingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NAMeasureResponse;

/**
 * {@link AbstractNetatmoThingHandler} is the abstract class that handles
 * common behaviors of both Devices and Modules
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
abstract class AbstractNetatmoThingHandler<X extends NetatmoThingConfiguration> extends BaseThingHandler {
    private static Logger logger = LoggerFactory.getLogger(AbstractNetatmoThingHandler.class);
    private final List<Integer> signalThresholds = new ArrayList<Integer>();
    protected List<String> measuredChannels = new ArrayList<String>();
    protected NAMeasureResponse measures = null;

    final Class<X> configurationClass;
    protected X configuration;

    AbstractNetatmoThingHandler(Thing thing, Class<X> configurationClass) {
        super(thing);
        this.configurationClass = configurationClass;
        String signalLevels = getProperty(PROPERTY_SIGNAL_LEVELS);
        if (signalLevels != null) {
            List<String> thresholds = Arrays.asList(signalLevels.split(","));
            for (String threshold : thresholds) {
                signalThresholds.add(Integer.parseInt(threshold));
            }
        }
    }

    // Protects property loading from missing entries Issue 1137
    String getProperty(String propertyName) {
        final Map<String, String> properties = thing.getProperties();
        if (properties.containsKey(propertyName)) {
            return properties.get(propertyName);
        } else {
            logger.warn("Unable to load property {}", propertyName);
            return null;
        }
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

    protected State getNAThingProperty(String channelId) {
        if (measures != null) {
            return toDecimalType(getMeasureValue(channelId));
        } else {
            return null;
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            configuration = this.getConfigAs(configurationClass);
        }
    }

    protected void updateChannels(String equipmentId) {
        logger.debug("Updating device channels");

        for (Channel channel : getThing().getChannels()) {
            String channelId = channel.getUID().getId();
            State state = getNAThingProperty(channelId);
            if (state != null) {
                updateState(channel.getUID(), state);
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    protected DateTimeType toDateTimeType(Integer netatmoTS) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(netatmoTS * 1000L);
        return new DateTimeType(calendar);
    }

    protected DecimalType toDecimalType(float value) {
        BigDecimal decimal = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
        return new DecimalType(decimal);
    }

    protected DecimalType toDecimalType(double value) {
        BigDecimal decimal = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
        return new DecimalType(decimal);
    }

    protected int getMeasureParamIndex(String channel) {
        return measuredChannels.indexOf(channel);
    }

    protected Float getMeasureValue(String channelId) {
        Float result = null;
        int index = getMeasureParamIndex(channelId);
        if (index != -1) {
            result = measures.getBody().get(0).getValue().get(0).get(index);
        }
        return result;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        String channel = channelUID.getId();
        if (MEASURE_CHANNELS.contains(channel)) {
            if (measuredChannels.indexOf(channel) == -1) {
                measuredChannels.add(channel);
            }
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        String channel = channelUID.getId();
        if (MEASURE_CHANNELS.contains(channel)) {
            if (measuredChannels.indexOf(channel) != -1) {
                measuredChannels.remove(channel);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateChannels(configuration.getEquipmentId());
        } else {
            logger.warn("This Thing is read-only and can only handle REFRESH command");
        }
    }

}
