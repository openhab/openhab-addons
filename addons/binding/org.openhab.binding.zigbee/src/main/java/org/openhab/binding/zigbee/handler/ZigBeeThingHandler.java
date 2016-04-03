/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zigbee.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bubblecloud.zigbee.api.Device;
import org.bubblecloud.zigbee.network.ZigBeeNode;
import org.bubblecloud.zigbee.network.ZigBeeNodeDescriptor;
import org.bubblecloud.zigbee.network.ZigBeeNodePowerDescriptor;
import org.bubblecloud.zigbee.network.impl.ZigBeeNetworkManagerException;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zigbee.ZigBeeBindingConstants;
import org.openhab.binding.zigbee.handler.cluster.ZigBeeClusterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ZigBeeThingHandler extends BaseThingHandler {
    private HashMap<ChannelUID, ZigBeeClusterHandler> channels = new HashMap<ChannelUID, ZigBeeClusterHandler>();

    private String nodeAddress;

    private Logger logger = LoggerFactory.getLogger(ZigBeeThingHandler.class);

    private ZigBeeCoordinatorHandler coordinatorHandler;

    private ScheduledFuture<?> pollingJob;

    public ZigBeeThingHandler(Thing zigbeeDevice) {
        super(zigbeeDevice);
    }

    @Override
    public void initialize() {
        final String configAddress = (String) getConfig().get(ZigBeeBindingConstants.PARAMETER_MACADDRESS);
        logger.debug("Initializing ZigBee thing handler {}.", configAddress);

        if (configAddress == null || configAddress.length() == 0) {
            logger.debug("Can't initializing ZigBee thing handler without address{}.");
            this.updateStatus(ThingStatus.OFFLINE);
            return;
        }
        nodeAddress = configAddress;

        // If the bridgeHandler hasn't initialised yet, then return
        if (coordinatorHandler == null) {
            return;
        }

        // Load the node information
        coordinatorHandler.deserializeNode(nodeAddress);
        ZigBeeNode node = coordinatorHandler.getNode(nodeAddress);

        // Update some properties from the device descriptors - in case they have changed
        ZigBeeNodeDescriptor nodeDescriptor = node.getNodeDescriptor();
        ZigBeeNodePowerDescriptor powerDescriptor = node.getPowerDescriptor();

        // First, check if this device is in the database
        // checkProductDatabase(node);

        // Create the channels from the device
        createChannelsFromDevice(node);

        // Create the channels list to simplify processing incoming events
        for (Channel channel : getThing().getChannels()) {
            // Process the channel properties
            Map<String, String> properties = channel.getProperties();

            String strCluster = properties.get(ZigBeeBindingConstants.CHANNEL_PROPERTY_CLUSTER);
            if (strCluster == null) {
                logger.warn("No cluster set for {}", channel.getUID());
                return;
            }
            Integer intCluster = Integer.parseInt(strCluster);

            ZigBeeClusterHandler handler = ZigBeeClusterHandler.getConverter(intCluster);
            if (handler == null) {
                logger.warn("No handler found for {}", channel.getUID());
                continue;
            }

            handler.createConverter(this, channel.getUID(), coordinatorHandler,
                    properties.get(ZigBeeBindingConstants.CHANNEL_PROPERTY_ADDRESS));

            handler.initializeConverter();

            channels.put(channel.getUID(), handler);
        }

        logger.debug("{}: Initializing ZigBee thing handler", nodeAddress);

        Runnable pollingRunnable = new Runnable() {
            @Override
            public void run() {
                logger.debug("{}: Polling...", nodeAddress);

                for (ChannelUID channel : channels.keySet()) {
                    ZigBeeClusterHandler handler = channels.get(channel);
                    if (handler == null) {
                        logger.debug("{}: Polling aborted as no handler found for {}", nodeAddress, channel);
                        continue;
                    }

                    logger.debug("{}: Polling {}", nodeAddress, channel);
                    handler.handleRefresh();
                }
            }
        };

        pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 60, 60, TimeUnit.SECONDS);
    }

    private void createChannelsFromDevice(ZigBeeNode node) {
        // Now process all the endpoints for this device and add all channels
        List<Channel> channels = new ArrayList<Channel>();
        for (Device device : coordinatorHandler.getNodeDevices(node)) {
            for (int cluster : device.getInputClusters()) {
                ZigBeeClusterHandler handler = ZigBeeClusterHandler.getConverter(cluster);
                if (handler != null) {
                    channels.addAll(handler.getChannels(getThing().getUID(), device));
                }
            }
        }

        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channels).withConfiguration(getConfig());
        updateThing(thingBuilder.build());
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        coordinatorHandler = (ZigBeeCoordinatorHandler) thingHandler;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {

    }

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        if (nodeAddress != null) {
            if (coordinatorHandler != null) {
                // coordinatorHandler.unsubscribeEvents(nodeAddress, this);
            }
            nodeAddress = null;
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        // Sanity check
        if (configurationParameters == null) {
            logger.warn("{}: No configuration parameters provided.", this.thing.getUID());
            return;
        }

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            String[] cfg = configurationParameter.getKey().split("_");
            if ("config".equals(cfg[0])) {
                if (cfg.length != 4) {
                    logger.warn("{}: Configuration invalid {}", this.thing.getUID(), configurationParameter.getKey());
                    continue;
                }

                int endpoint = Integer.parseInt(cfg[1]);
                int cluster = Integer.parseInt(cfg[2]);
                String address = nodeAddress + "/" + endpoint;

                final Device device = coordinatorHandler.getDevice(address);
                try {
                    if (device.bindToLocal(cluster) == false) {
                        logger.warn("{}: Error adding binding for cluster {}", address, cluster);
                    }
                } catch (ZigBeeNetworkManagerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                logger.warn("{}: Configuration invalid {}", this.thing.getUID(), configurationParameter.getKey());
            }
        }

        // Persist changes
        updateConfiguration(configuration);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Check that we have a coordinator to work through
        if (coordinatorHandler == null) {
            logger.warn("Coordinator handler not found. Cannot handle command without coordinator.");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        ZigBeeClusterHandler handler = channels.get(channelUID);

        if (handler == null) {
            logger.warn("No handler found for {}", channelUID);
            return;
        }

        handler.handleCommand(command);
    }

    public void setChannelState(ChannelUID channel, State state) {
        updateState(channel, state);
        this.updateStatus(ThingStatus.ONLINE);
    }
}
