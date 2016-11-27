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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.AbstractNetatmoClimateThingConfiguration;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;

import io.swagger.client.model.NAMeasureResponse;

/**
 * {@link AbstractNetatmoClimateThingHandler} is the abstract class that handles
 * common behaviors of both Devices and Modules
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Ing. Peter Weiss
 */
abstract class AbstractNetatmoClimateThingHandler<X extends AbstractNetatmoClimateThingConfiguration>
        extends AbstractNetatmoThingHandler<X> {
    private final List<Integer> signalThresholds = new ArrayList<Integer>();
    protected List<String> measuredChannels = new ArrayList<String>();
    protected NAMeasureResponse measures = null;

    AbstractNetatmoClimateThingHandler(Thing thing, Class<X> configurationClass) {
        super(thing, configurationClass);
    }

    @Override
    public void initialize() {
        String signalLevels = getProperty(PROPERTY_SIGNAL_LEVELS);
        if (signalLevels != null) {
            List<String> thresholds = Arrays.asList(signalLevels.split(","));
            for (String threshold : thresholds) {
                signalThresholds.add(Integer.parseInt(threshold));
            }
        }

        buildMeasurableChannelList();
        super.initialize();
    }

    private void buildMeasurableChannelList() {
        List<Channel> channels = getThing().getChannels();
        for (Channel channel : channels) {
            addChannelToMeasures(channel.getUID());
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

    @Override
    protected State getNAThingProperty(String channelId) {
        return (measures != null) ? ChannelTypeUtils.toDecimalType(getMeasureValue(channelId)) : UnDefType.NULL;

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
}
