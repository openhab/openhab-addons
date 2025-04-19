/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.AttributeListener;
import org.openhab.binding.matter.internal.client.EventTriggeredListener;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.controller.devices.converter.ConverterRegistry;
import org.openhab.binding.matter.internal.controller.devices.converter.GenericConverter;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dan Cunningham - Initial contribution
 *
 *         A Matter Device type is a grouping of clusters that represent a single device, like a thermostat, Light,
 *         etc. This classification specifies which clusters are mandatory and which are optional for a given device
 *         type, although devices can have any number or type of Matter clusters. This is suppose to ease client
 *         development by providing a common interface for interacting with common devices types.
 *
 *         The DeviceType class coordinates sending openHAB commands to Matter clusters and updating openHAB channels
 *         based on Matter cluster events. Some device types like lighting devices require coordination among their
 *         clusters, others do not. A DeviceType Class depends on one or more GenericConverter classes to handle the
 *         conversion of Matter cluster events to openHAB channel updates and openHAB channel commands to Matter cluster
 *         commands.
 *
 *         Typically, we map a single openHAB channel or item type, like Color, which accepts multiple command types:
 *         HSB,Percent, and OnOff to multiple Matter clusters, like ColorControl and LevelControl and OnOffControl
 *
 *         Most Device types need little coordination so the default logic (and GenericType instance) will suffice, but
 *         this can be overridden to provide custom logic for more complex devices (like lighting)
 */
@NonNullByDefault
public abstract class DeviceType implements AttributeListener, EventTriggeredListener {
    private final Logger logger = LoggerFactory.getLogger(DeviceType.class);

    protected Integer deviceType;
    protected Integer endpointNumber;
    protected MatterBaseThingHandler handler;

    protected Map<ChannelUID, GenericConverter<? extends BaseCluster>> channelUIDToConverters = new HashMap<>();
    protected Map<ChannelUID, @Nullable StateDescription> channelUIDToStateDescription = new HashMap<>();
    protected Map<Integer, GenericConverter<? extends BaseCluster>> clusterToConverters = new HashMap<>();
    protected Map<String, BaseCluster> allClusters = new HashMap<>();

    public DeviceType(Integer deviceType, MatterBaseThingHandler handler, Integer endpointNumber) {
        this.deviceType = deviceType;
        this.handler = handler;
        this.endpointNumber = endpointNumber;
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        GenericConverter<? extends BaseCluster> converter = clusterToConverters.get(message.path.clusterId);
        if (converter != null) {
            converter.onEvent(message);
        } else {
            logger.debug("onEvent: No converter found for cluster: {}", message.path.clusterId);
        }
    }

    @Override
    public void onEvent(EventTriggeredMessage message) {
        GenericConverter<? extends BaseCluster> converter = clusterToConverters.get(message.path.clusterId);
        if (converter != null) {
            converter.onEvent(message);
        } else {
            logger.debug("onEvent: No converter found for cluster: {}", message.path.clusterId);
        }
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command for channel: {}", channelUID);
        Optional.ofNullable(channelUIDToConverters.get(channelUID))
                .ifPresent(converter -> converter.handleCommand(channelUID, command));
    }

    /**
     * Inform all cluster converters to refresh their channel state
     */
    public void initState() {
        channelUIDToConverters.forEach((channelUID, converter) -> converter.initState());
    }

    public Integer endpointNumber() {
        return endpointNumber;
    }

    /**
     * Create openHAB channels for the device type based on the clusters provided
     *
     * @param clusters
     */
    public final List<Channel> createChannels(Integer endpointNumber, Map<String, BaseCluster> clusters,
            ChannelGroupUID channelGroupUID) {
        logger.debug("createChannels {}", endpointNumber);
        allClusters = clusters;
        List<Channel> channels = new ArrayList<>();
        channelUIDToConverters.clear();
        channelUIDToStateDescription.clear();
        clusterToConverters.clear();
        String label = "";
        // each cluster will create its own channels and add to this device's total channels
        clusters.forEach((clusterName, cluster) -> {
            logger.debug("Creating channels for cluster: {}", clusterName);
            GenericConverter<? extends BaseCluster> converter = createConverter(cluster, clusters, label);
            if (converter != null) {
                logger.debug("Converter found for cluster: {}", clusterName);
                Map<Channel, @Nullable StateDescription> converterChannels = converter.createChannels(channelGroupUID);
                for (Channel channel : converterChannels.keySet()) {
                    channelUIDToConverters.put(channel.getUID(), converter);
                    channelUIDToStateDescription.put(channel.getUID(), converterChannels.get(channel));
                    clusterToConverters.put(cluster.id, converter);
                    boolean hasMatchingUID = channels.stream().anyMatch(c -> channel.getUID().equals(c.getUID()));
                    if (!hasMatchingUID) {
                        channels.add(channel);
                    } else {
                        logger.debug("{} channel already exists: {}", clusterName, channel.getUID());
                    }
                }
            }
        });
        return channels;
    }

    public Map<ChannelUID, @Nullable StateDescription> getStateDescriptions() {
        return Collections.unmodifiableMap(new HashMap<>(channelUIDToStateDescription));
    }

    public void pollClusters() {
        clusterToConverters.forEach((clusterId, converter) -> {
            converter.pollCluster();
        });
    }

    // This method is designed to be overridden in subclasses
    protected @Nullable GenericConverter<? extends BaseCluster> createConverter(BaseCluster cluster,
            Map<String, BaseCluster> allClusters, String labelPrefix) {
        return ConverterRegistry.createConverter(cluster, handler, endpointNumber, labelPrefix);
    }
}
