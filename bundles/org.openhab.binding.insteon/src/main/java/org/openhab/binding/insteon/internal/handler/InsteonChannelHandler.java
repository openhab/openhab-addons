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
package org.openhab.binding.insteon.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.feature.FeatureListener;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link InsteonChannelHandler} represents an insteon channel handler.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonChannelHandler implements FeatureListener {
    private ChannelUID channelUID;
    private InsteonChannelConfiguration config;
    private DeviceFeature feature;
    private InsteonThingHandler handler;

    public InsteonChannelHandler(ChannelUID channelUID, InsteonChannelConfiguration config, DeviceFeature feature,
            InsteonThingHandler handler) {
        this.channelUID = channelUID;
        this.config = config;
        this.feature = feature;
        this.handler = handler;

        feature.registerListener(this);
    }

    public void dispose() {
        feature.unregisterListener(this);
    }

    public void handleCommand(Command command) {
        feature.handleCommand(config, command);
    }

    @Override
    public void stateUpdated(State state) {
        handler.updateState(channelUID, state);
    }

    @Override
    public void eventTriggered(String event) {
        handler.triggerChannel(channelUID, event);
    }

    /**
     * Factory method for creating a InsteonChannelHandler from a channel uid, feature and parameters
     *
     * @param channel the channel
     * @param feature the device feature
     * @param thingHandler the thing handler
     * @return the newly created InsteonChannelHandler
     */
    public static InsteonChannelHandler makeHandler(Channel channel, DeviceFeature feature,
            InsteonThingHandler thingHandler) {
        ChannelUID channelUID = channel.getUID();
        InsteonChannelConfiguration config = channel.getConfiguration().as(InsteonChannelConfiguration.class);
        InsteonChannelHandler channelHandler = new InsteonChannelHandler(channelUID, config, feature, thingHandler);

        State state = feature.getState();
        if (state != UnDefType.NULL) {
            thingHandler.updateState(channelUID, state);
        }

        return channelHandler;
    }
}
