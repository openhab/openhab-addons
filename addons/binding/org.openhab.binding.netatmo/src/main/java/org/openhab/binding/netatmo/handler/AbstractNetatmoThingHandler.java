/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoThingConfiguration;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
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
    private List<Integer> signalThresholds = null;
    protected List<String> measuredChannels = new ArrayList<String>();
    protected NAMeasureResponse measures = null;

    final Class<X> configurationClass;
    protected X configuration = null;

    AbstractNetatmoThingHandler(Thing thing, Class<X> configurationClass) {
        super(thing);
        this.configurationClass = configurationClass;
    }

    @Override
    public void initialize() {

        buildMeasurableChannelList();
        configuration = getConfigAs(configurationClass);

        super.initialize();
    }

    private void buildMeasurableChannelList() {
        List<Channel> channels = getThing().getChannels();
        for (Channel channel : channels) {
            addChannelToMeasures(channel.getUID());
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

    private void initializeThresholds() {
        signalThresholds = new ArrayList<Integer>();
        String signalLevels = getProperty(PROPERTY_SIGNAL_LEVELS);
        if (signalLevels != null) {
            List<String> thresholds = Arrays.asList(signalLevels.split(","));
            for (String threshold : thresholds) {
                signalThresholds.add(Integer.parseInt(threshold));
            }
        }

    }

    int getSignalStrength(int signalLevel) {
        // Take in account #3995

        if (signalThresholds == null) {
            initializeThresholds();
        }

        int level;
        for (level = 0; level < signalThresholds.size(); level++) {
            if (signalLevel > signalThresholds.get(level)) {
                break;
            }
        }
        return level;
    }

    protected State getNAThingProperty(String channelId) {
        return (measures != null) ? ChannelTypeUtils.toDecimalType(getMeasureValue(channelId)) : UnDefType.NULL;

    }

    protected void updateChannels(String equipmentId) {
        logger.debug("Updating device channels");

        for (Channel channel : getThing().getChannels()) {
            String channelId = channel.getUID().getId();
            State state = getNAThingProperty(channelId);
            updateState(channel.getUID(), state);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    protected Float getMeasureValue(String channelId) {
        int index = measuredChannels.indexOf(channelId);
        return (index != -1) ? measures.getBody().get(0).getValue().get(0).get(index) : null;

    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        addChannelToMeasures(channelUID);
    }

    /*
     * If this channel value is provided as a measure, then add it
     * in the getMeasure parameter list
     */
    protected void addChannelToMeasures(ChannelUID channelUID) {
        String channel = channelUID.getId();
        if (MEASURABLE_CHANNELS.contains(channel)) {
            if (measuredChannels.indexOf(channel) == -1) {
                measuredChannels.add(channel);
            }
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        String channel = channelUID.getId();
        if (MEASURABLE_CHANNELS.contains(channel)) {
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
        }
    }

    protected NetatmoBridgeHandler getBridgeHandler() {
        return (NetatmoBridgeHandler) this.getBridge().getHandler();
    }

    public X getConfiguration() {
        return configuration;
    }

}
