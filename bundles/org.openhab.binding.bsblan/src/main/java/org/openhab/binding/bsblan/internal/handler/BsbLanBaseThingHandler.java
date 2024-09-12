/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.bsblan.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bsblan.internal.api.BsbLanApiCaller;
import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Handler class for all BSB-LAN services.
 *
 * @author Peter Schraffl - Initial contribution
 */
@NonNullByDefault
public abstract class BsbLanBaseThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BsbLanBaseThingHandler.class);
    private @Nullable BsbLanBridgeHandler bridgeHandler;

    public BsbLanBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
        } else {
            setChannel(channelUID.getId(), command);
        }
    }

    @Override
    public void initialize() {
        BsbLanBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("Initializing '{}': thing is only supported within a bridge", getDescription());
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        logger.trace("Initializing '{}' thing", getDescription());
        bridgeHandler.registerThing(this);
    }

    protected synchronized @Nullable BsbLanBridgeHandler getBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof BsbLanBridgeHandler lanBridgeHandler) {
                this.bridgeHandler = lanBridgeHandler;
            }
        }
        return this.bridgeHandler;
    }

    /**
     * Update all channels
     */
    protected void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            updateChannel(channel.getUID().getId());
        }
    }

    protected @Nullable BsbLanApiCaller getApiCaller() {
        // use a local variable to avoid the build warning "Potential null pointer access"
        BsbLanBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler == null) {
            return null;
        }
        return new BsbLanApiCaller(localBridgeHandler.getBridgeConfiguration());
    }

    /**
     * Update the channel from the last data
     *
     * @param channelId the id identifying the channel to be updated
     */
    protected abstract void updateChannel(String channelId);

    /**
     * Set new value for channel
     *
     * @param channelId the id identifying the channel
     */
    protected abstract void setChannel(String channelId, Command command);

    /**
     * return an internal description for logging
     *
     * @return the description of the thing
     */
    protected abstract String getDescription();

    /**
     * do whatever a thing must do to update the values
     * this function is called from the bridge in a given interval
     *
     * @param bridgeConfiguration the connected bridge configuration
     */
    public abstract void refresh(BsbLanBridgeConfiguration bridgeConfiguration);
}
