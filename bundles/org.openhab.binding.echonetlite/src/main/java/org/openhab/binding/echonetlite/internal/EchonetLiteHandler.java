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
package org.openhab.binding.echonetlite.internal;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EchonetLiteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class EchonetLiteHandler extends BaseThingHandler implements EchonetDeviceListener {
    private final Logger logger = LoggerFactory.getLogger(EchonetLiteHandler.class);

    private @Nullable EchonetDeviceConfig config;
    private @Nullable InstanceKey instanceKey;
    private final Map<String, State> stateByChannelId = new HashMap<>();

    public EchonetLiteHandler(final Thing thing) {
        super(thing);
    }

    @Nullable
    private EchonetLiteBridgeHandler bridgeHandler() {
        @Nullable
        final Bridge bridge = getBridge();
        if (null == bridge) {
            logger.warn("Unable to handle commands if bridge is null");
            return null;
        }

        @Nullable
        final EchonetLiteBridgeHandler handler = (EchonetLiteBridgeHandler) bridge.getHandler();
        if (null == handler) {
            logger.warn("Unable to handle commands if bridge handler is null");
            return null;
        }

        return handler;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        @Nullable
        final EchonetLiteBridgeHandler handler = bridgeHandler();
        if (null == handler) {
            return;
        }

        if (command instanceof RefreshType) {
            logger.debug("Refreshing: {}", channelUID);

            final State currentState = stateByChannelId.get(channelUID.getId());
            if (null == currentState) {
                handler.refreshDevice(instanceKey, channelUID.getId());
            } else {
                updateState(channelUID, currentState);
            }
        } else if (command instanceof State) {
            logger.debug("Updating: {} to {}", channelUID, command);

            handler.updateDevice(instanceKey, channelUID.getId(), (State) command);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(EchonetDeviceConfig.class);

        if (null == config) {
            logger.warn("config is null");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

        logger.info("Initialising: {}", config);

        updateStatus(ThingStatus.UNKNOWN);

        @Nullable
        final EchonetLiteBridgeHandler bridgeHandler = bridgeHandler();
        if (null == bridgeHandler) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge is null");
            return;
        }

        try {
            // TOOD: Move DNS lookup out to background
            final InetSocketAddress address = new InetSocketAddress(config.hostname, config.port);
            instanceKey = new InstanceKey(address, EchonetClass.resolve(config.groupCode, config.classCode),
                    config.instance);
            updateProperty("instanceKey", instanceKey.representationProperty());
            bridgeHandler.newDevice(instanceKey, config.pollIntervalMs, config.pollIntervalMs, this);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            logger.error("Failed to configure {}", config, e);
        }
    }

    public void handleRemoval() {
        @Nullable
        final EchonetLiteBridgeHandler bridgeHandler = bridgeHandler();
        if (null == bridgeHandler) {
            return;
        }

        bridgeHandler.removeDevice(instanceKey);
    }

    @NonNullByDefault
    public void onInitialised(String identifier, InstanceKey instanceKey, Map<String, String> channelIdAndType) {

        logger.debug("Initialised Channels: {}", channelIdAndType);

        final List<String> toAddChannelFor = new ArrayList<>();

        for (String channelId : channelIdAndType.keySet()) {
            if (null == thing.getChannel(channelId)) {
                toAddChannelFor.add(channelId);
            }
        }

        logger.debug("Adding Channels: {}", toAddChannelFor);

        if (!toAddChannelFor.isEmpty()) {
            final ThingBuilder thingBuilder = editThing();

            for (String channelId : toAddChannelFor) {
                final Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), channelId))
                        .withAcceptedItemType(channelIdAndType.get(channelId))
                        .withType(new ChannelTypeUID(thing.getThingTypeUID().getBindingId(), channelId)).build();
                thingBuilder.withChannel(channel);

                logger.debug("Added Channel: {}", channel);
            }

            updateThing(thingBuilder.build());
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public void onUpdated(final String channelId, final State value) {

        stateByChannelId.put(channelId, value);

        if (ThingStatus.ONLINE != getThing().getStatus()) {
            updateStatus(ThingStatus.ONLINE);
        }
        updateState(channelId, value);
    }

    public void onRemoved() {
        updateStatus(ThingStatus.REMOVED);
    }
}
