/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.action.NABridgeActions;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.handler.capability.CapabilityMap;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NABridgeHandler} is the base class for all Netatmo bridges
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NABridgeHandler extends BaseBridgeHandler implements NACommonInterface {
    private final Logger logger = LoggerFactory.getLogger(NABridgeHandler.class);
    public CapabilityMap capabilities = new CapabilityMap();
    protected final ApiBridge apiBridge;

    public NABridgeHandler(Bridge bridge, ApiBridge apiBridge) {
        super(bridge);
        this.apiBridge = apiBridge;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for bridge {}", getThing().getUID());
        commonInitialize(apiBridge, scheduler);
    }

    @Override
    public void dispose() {
        capabilities.values().forEach(cap -> cap.dispose());
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        commonHandleCommand(channelUID, command);
    }

    @Override
    public void removeChannels(List<Channel> channels) {
        ThingBuilder builder = editThing().withoutChannels(channels);
        updateThing(builder.build());
    }

    @Override
    public void setThingStatus(ThingStatus thingStatus, @Nullable String thingStatusReason) {
        updateStatus(thingStatus, ThingStatusDetail.NONE, thingStatusReason);
    }

    @Override
    public CapabilityMap getCapabilities() {
        return capabilities;
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override
    public boolean isLinked(ChannelUID channelUID) {
        return super.isLinked(channelUID);
    }

    @Override
    public @Nullable Bridge getBridge() {
        return super.getBridge();
    }

    @Override
    public void triggerChannel(String channelID, String event) {
        super.triggerChannel(channelID, event);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(NABridgeActions.class);
    }

    public void reconnectApi() {
        apiBridge.openConnection(null);
    }
}
