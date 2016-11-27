/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.AbstractNetatmoThingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractNetatmoThingHandler} is the abstract class that handles
 * common behaviors of all netatmo things
 *
 * @author Ing. Peter Weiss - Initial implementation
 *
 */
abstract class AbstractNetatmoThingHandler<X extends AbstractNetatmoThingConfiguration> extends BaseThingHandler {
    protected static Logger logger = LoggerFactory.getLogger(AbstractNetatmoThingHandler.class);

    final Class<X> configurationClass;
    protected X configuration = null;

    AbstractNetatmoThingHandler(Thing thing, Class<X> configurationClass) {
        super(thing);
        this.configurationClass = configurationClass;
    }

    @Override
    public void initialize() {
        configuration = this.getConfigAs(configurationClass);
        super.initialize();
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

    abstract protected State getNAThingProperty(String channelId);

    protected void updateChannels(String equipmentId) {
        logger.debug("Updating channels");

        for (Channel channel : getThing().getChannels()) {
            String channelId = channel.getUID().getId();
            State state = getNAThingProperty(channelId);
            updateState(channel.getUID(), state);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateChannels(configuration.getId());
        }
    }

    protected NetatmoBridgeHandler<?> getBridgeHandler() {
        return (NetatmoBridgeHandler<?>) this.getBridge().getHandler();
    }

}
