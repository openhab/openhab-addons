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
 * An abstract class that represents a Matter Device type
 * 
 * A Matter Device type is a grouping of clusters that represent a single device, like a thermostat, Light,
 * etc. This classification specifies which clusters are mandatory and which are optional for a given device
 * type, although devices can have any number or type of Matter clusters. This is suppose to ease client
 * development by providing a common interface for interacting with common devices types.
 *
 * The DeviceType class coordinates sending openHAB commands to Matter clusters and updating openHAB channels
 * based on Matter cluster events. Some device types like lighting devices require coordination among their
 * clusters, others do not. A DeviceType Class depends on one or more GenericConverter classes to handle the
 * conversion of Matter cluster events to openHAB channel updates and openHAB channel commands to Matter cluster
 * commands.
 *
 * Typically, we map a single openHAB channel or item type, like Color, which accepts multiple command types:
 * HSB,Percent, and OnOff to multiple Matter clusters, like ColorControl and LevelControl and OnOffControl
 *
 * Most Device types need little coordination so the default logic (and GenericType instance) will suffice, but
 * this can be overridden to provide custom logic for more complex devices (like lighting)
 * 
 * @author Dan Cunningham - Initial contribution
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

    /**
     * Handles a openHAB command for a specific channel
     * 
     * @param channelUID The UID of the channel
     * @param command The command to handle
     */
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command for channel: {}", channelUID);
        Optional.ofNullable(channelUIDToConverters.get(channelUID))
                .ifPresent(converter -> converter.handleCommand(channelUID, command));
    }

    /**
     * Inform all cluster converters to refresh their channel state
     */
    public void initState() {
        clusterToConverters.forEach((clusterId, converter) -> converter.initState());
    }

    /**
     * Returns the endpoint number for this device
     * 
     * @return The endpoint number
     */
    public Integer endpointNumber() {
        return endpointNumber;
    }

    /**
     * Create openHAB channels for the device type based on the clusters provided
     *
     * @param endpointNumber The endpoint number that contains the clusters
     * @param clusters The clusters to create channels for
     * @param channelGroupUID The channel group UID
     * @return A list of channels
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
                clusterToConverters.put(cluster.id, converter);
                Map<Channel, @Nullable StateDescription> converterChannels = converter.createChannels(channelGroupUID);
                for (Channel channel : converterChannels.keySet()) {
                    channelUIDToConverters.put(channel.getUID(), converter);
                    channelUIDToStateDescription.put(channel.getUID(), converterChannels.get(channel));
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

    /**
     * Returns an unmodifiable map of channel UID to state description for this device type.
     */
    public Map<ChannelUID, @Nullable StateDescription> getStateDescriptions() {
        return Collections.unmodifiableMap(new HashMap<>(channelUIDToStateDescription));
    }

    /**
     * Calls the pollCluster method on all cluster converters
     */
    public void pollClusters() {
        clusterToConverters.forEach((clusterId, converter) -> {
            converter.pollCluster();
        });
    }

    /**
     * Returns an unmodifiable map of all clusters associated with this device type.
     * The map keys are cluster names
     */
    public Map<String, BaseCluster> getAllClusters() {
        return Collections.unmodifiableMap(allClusters);
    }

    /**
     * Returns an unmodifiable map of cluster converters associated with this device type.
     * The map keys are cluster IDs
     */
    public Map<Integer, GenericConverter<? extends BaseCluster>> getClusterConverters() {
        return Collections.unmodifiableMap(clusterToConverters);
    }

    // This method is designed to be overridden in subclasses
    protected @Nullable GenericConverter<? extends BaseCluster> createConverter(BaseCluster cluster,
            Map<String, BaseCluster> allClusters, String labelPrefix) {
        try {
            return ConverterRegistry.createConverter(cluster, handler, endpointNumber, labelPrefix);
        } catch (ConverterRegistry.NoConverterFoundException e) {
            logger.debug("No converter found for cluster: {}", cluster.id);
            return null;
        } catch (ConverterRegistry.ConverterCreationException e) {
            logger.debug("Error creating converter for cluster: {}", cluster.id, e);
            return null;
        }
    }
}
