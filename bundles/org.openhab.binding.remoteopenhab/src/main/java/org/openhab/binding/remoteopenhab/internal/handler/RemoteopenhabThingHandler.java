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
package org.openhab.binding.remoteopenhab.internal.handler;

import static org.openhab.binding.remoteopenhab.internal.RemoteopenhabBindingConstants.*;
import static org.openhab.binding.remoteopenhab.internal.config.RemoteopenhabTriggerChannelConfiguration.CHANNEL_UID;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.remoteopenhab.internal.config.RemoteopenhabThingConfiguration;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabChannel;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabStatusInfo;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabThing;
import org.openhab.binding.remoteopenhab.internal.exceptions.RemoteopenhabException;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabThingsDataListener;
import org.openhab.binding.remoteopenhab.internal.rest.RemoteopenhabRestClient;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteopenhabThingHandler} is responsible for handling status updates associated to
 * any remote thing.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RemoteopenhabThingHandler extends BaseThingHandler implements RemoteopenhabThingsDataListener {

    private final Logger logger = LoggerFactory.getLogger(RemoteopenhabThingHandler.class);

    private @Nullable RemoteopenhabRestClient restClient;

    private @NonNullByDefault({}) RemoteopenhabThingConfiguration config;

    public RemoteopenhabThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No state channel defined for this thing type and so no command to handle
    }

    @Override
    public void initialize() {
        logger.debug("initializing remote openHAB handler for thing {}", getThing().getUID());
        Bridge bridge = getBridge();
        initializeThing(bridge != null ? bridge.getStatus() : null);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        initializeThing(bridgeStatusInfo.getStatus());
    }

    private void initializeThing(@Nullable ThingStatus bridgeStatus) {
        Bridge bridge = getBridge();
        BridgeHandler bridgeHandler = bridge != null ? bridge.getHandler() : null;
        RemoteopenhabRestClient oldClient = this.restClient;
        if (oldClient != null) {
            oldClient.removeThingsDataListener(this);
            this.restClient = null;
        }
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                config = getConfigAs(RemoteopenhabThingConfiguration.class);

                String uid = getConfigThingUID();
                if (uid.length() == 0) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.config-error-undefined-thing-uid");
                } else {
                    RemoteopenhabRestClient client = ((RemoteopenhabBridgeHandler) bridgeHandler).gestRestClient();
                    client.addThingsDataListener(this);
                    this.restClient = client;

                    updateStatus(ThingStatus.UNKNOWN);

                    scheduler.execute(() -> {
                        try {
                            RemoteopenhabThing thing = client.getRemoteThing(uid);
                            createTriggerChannels(thing, config.buildTriggerChannels);
                            RemoteopenhabStatusInfo statusInfo = thing.statusInfo;
                            if (statusInfo != null) {
                                updateThingStatus(uid, statusInfo);
                            }
                        } catch (RemoteopenhabException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
                        }
                    });
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing remote openHAB handler for thing {}", getThing().getUID());
        RemoteopenhabRestClient client = this.restClient;
        if (client != null) {
            client.removeThingsDataListener(this);
            this.restClient = null;
        }
        super.dispose();
    }

    private String getConfigThingUID() {
        return config.thingUID.trim();
    }

    private void createTriggerChannels(RemoteopenhabThing thing, boolean addNewChannels) {
        List<Channel> channels = new ArrayList<>();
        for (RemoteopenhabChannel channelDTO : thing.channels) {
            if (!"TRIGGER".equals(channelDTO.kind)) {
                continue;
            }
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_TRIGGER);
            ChannelUID channelUID = new ChannelUID(getThing().getUID(),
                    channelDTO.uid.replaceAll("[^A-Za-z0-9_]", "_"));
            Configuration channelConfig = new Configuration();
            channelConfig.put(CHANNEL_UID, channelDTO.uid);
            logger.trace("Create the channel {} of type {}", channelUID, channelTypeUID);
            channels.add(ChannelBuilder.create(channelUID, null).withType(channelTypeUID).withKind(ChannelKind.TRIGGER)
                    .withLabel(channelDTO.label).withDescription(channelDTO.description)
                    .withConfiguration(channelConfig).build());
        }
        if (!channels.isEmpty()) {
            ThingBuilder thingBuilder = editThing();
            int nbRemoved = 0;
            for (Channel channel : channels) {
                if (getThing().getChannel(channel.getUID()) != null) {
                    thingBuilder.withoutChannel(channel.getUID());
                    nbRemoved++;
                }
            }
            if (nbRemoved > 0) {
                logger.debug("{} trigger channels removed for the thing {}", nbRemoved, getThing().getUID());
            }
            int nbAdded = 0;
            if (addNewChannels) {
                for (Channel channel : channels) {
                    thingBuilder.withChannel(channel);
                }
                nbAdded = channels.size();
                logger.debug("{} trigger channels added for the thing {}", nbAdded, getThing().getUID());
            }
            if (nbRemoved > 0 || nbAdded > 0) {
                updateThing(thingBuilder.build());
            }
        }
    }

    @Override
    public void onThingStatusUpdated(String thingUID, RemoteopenhabStatusInfo statusInfo) {
        if (thingUID.equals(getConfigThingUID())) {
            updateThingStatus(thingUID, statusInfo);
        }
    }

    @Override
    public void onThingAdded(RemoteopenhabThing thing) {
        // Nothing to do
    }

    @Override
    public void onThingRemoved(RemoteopenhabThing thing) {
        // Nothing to do
    }

    @Override
    public void onChannelTriggered(String channelUID, @Nullable String event) {
        String thingUID = channelUID.substring(0, channelUID.lastIndexOf(":"));
        if (thingUID.equals(getConfigThingUID())) {
            for (Channel channel : getThing().getChannels()) {
                if (channel.getKind() == ChannelKind.TRIGGER
                        && channelUID.equals(channel.getConfiguration().get(CHANNEL_UID))) {
                    if (event == null) {
                        triggerChannel(channel.getUID());
                        logger.debug("triggerChannel {}", channel.getUID());
                    } else {
                        triggerChannel(channel.getUID(), event);
                        logger.debug("triggerChannel {} with event {}", channel.getUID(), event);
                    }
                }
            }
        }
    }

    private void updateThingStatus(String thingUID, RemoteopenhabStatusInfo statusInfo) {
        ThingStatus status = ThingStatus.valueOf(statusInfo.status);
        // All remote status different from UNKNOWN or ONLINE or OFFLINE is considered as OFFLINE
        if (status != ThingStatus.UNKNOWN && status != ThingStatus.ONLINE && status != ThingStatus.OFFLINE) {
            status = ThingStatus.OFFLINE;
        }
        ThingStatusDetail detail = ThingStatusDetail.valueOf(statusInfo.statusDetail);
        updateStatus(status, detail, statusInfo.description);
        logger.debug("updateStatus {} with status {} detail {} description {}", thingUID, status, detail,
                statusInfo.description);
    }
}
