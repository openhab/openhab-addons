/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
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
import org.openhab.binding.fronius.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.api.ValueUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Handler class for all Fronius services.
 *
 * @author Gerrit Beine - Initial contribution
 * @author Thomas Rokohl - Refactoring to merge the concepts
 */
public abstract class FroniusBaseThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusBaseThingHandler.class);
    private final String serviceDescription;
    private FroniusBridgeHandler bridgeHandler;

    public FroniusBaseThingHandler(Thing thing) {
        super(thing);
        serviceDescription = getDescription();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        if (getFroniusBridgeHandler() == null) {
            logger.debug("Initializing {} Service is only supported within a bridge", serviceDescription);
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        logger.debug("Initializing {} Service", serviceDescription);
        getFroniusBridgeHandler().registerService(this);
    }

    private synchronized FroniusBridgeHandler getFroniusBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof FroniusBridgeHandler) {
                this.bridgeHandler = (FroniusBridgeHandler) handler;
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
    }

    /**
     * Update all Channels
     */
    protected void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            updateChannel(channel.getUID().getId());
        }
    }

    /**
     * Update the channel from the last data
     *
     * @param channelId the id identifying the channel to be updated
     */
    protected void updateChannel(String channelId) {
        if (!isLinked(channelId)) {
            return;
        }
        Object value = getValue(channelId);

        State state = null;
        if (value instanceof BigDecimal) {
            state = new DecimalType((BigDecimal) value);
        } else if (value instanceof Integer) {
            state = new DecimalType(BigDecimal.valueOf(((Integer) value).longValue()));
        } else if (value instanceof Double) {
            state = new DecimalType((double) value);
        } else if (value instanceof ValueUnit) {
            state = new DecimalType(((ValueUnit) value).getValue());
        } else {
            logger.warn("Update channel {}: Unsupported value type {}", channelId, value.getClass().getSimpleName());
        }
        logger.debug("Update channel {} with state {} ({})", channelId, (state == null) ? "null" : state.toString(),
                value.getClass().getSimpleName());

        // Update the channel
        if (state != null) {
            updateState(channelId, state);
        }
    }

    /**
     * return an internal description for logging
     *
     * @return the description of the thing
     */
    protected abstract String getDescription();

    /**
     * get the "new" associated value for a channelId
     *
     * @param channelId the id identifying the channel
     * @return the "new" associated value
     */
    protected abstract Object getValue(String channelId);

    /**
     * do whatever a thing must do to update the values
     * this function is called from the bridge in a given interval
     *
     * @param bridgeConfiguration the connected bridge configuration
     */
    public abstract void refresh(FroniusBridgeConfiguration bridgeConfiguration);

}
