/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zigbee.handler.cluster;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bubblecloud.zigbee.api.Device;
import org.bubblecloud.zigbee.api.ZigBeeApiConstants;
import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zigbee.ZigBeeBindingConstants;
import org.openhab.binding.zigbee.handler.ZigBeeCoordinatorHandler;
import org.openhab.binding.zigbee.handler.ZigBeeThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZigBeeClusterConverter class. Base class for all converters that convert between ZigBee clusters and openHAB
 * channels.
 *
 * @author Chris Jackson
 */
public abstract class ZigBeeClusterHandler {
    private static Logger logger = LoggerFactory.getLogger(ZigBeeClusterHandler.class);

    protected ZigBeeThingHandler thing = null;
    // protected ZigBeeThingChannel channel = null;
    protected ZigBeeCoordinatorHandler coordinator = null;

    protected ChannelUID channelUID = null;
    protected String address = null;

    private static Map<Integer, Class<? extends ZigBeeClusterHandler>> clusterMap = null;

    /**
     * Constructor. Creates a new instance of the {@link ZWaveCommandClassConverter} class.
     *
     */
    public ZigBeeClusterHandler() {
        super();
    }

    public void createConverter(ZigBeeThingHandler thing, ChannelUID channelUID, ZigBeeCoordinatorHandler coordinator,
            String address) {
        this.thing = thing;
        this.channelUID = channelUID;
        this.address = address;
        this.coordinator = coordinator;
    }

    public abstract void initializeConverter();

    public void disposeConverter() {
    }

    /**
     * Execute refresh method. This method is called every time a binding item is refreshed and the corresponding node
     * should be sent a message.
     *
     * @param channel the {@link ZigBeeThingChannel}
     */
    public void handleRefresh() {
    }

    /**
     * Receives a command from openHAB and translates it to an operation on the Z-Wave network.
     *
     * @param channel the {@link ZigBeeThingChannel}
     * @param command the {@link Command} to send
     */
    public void handleCommand(Command command) {
    }

    public abstract List<Channel> getChannels(ThingUID thingUID, Device device);

    /**
     *
     * @param clusterId
     * @return
     */
    public static ZigBeeClusterHandler getConverter(int clusterId) {
        if (clusterMap == null) {
            clusterMap = new HashMap<Integer, Class<? extends ZigBeeClusterHandler>>();

            // Add all the handlers into the map...
            clusterMap.put(ZigBeeApiConstants.CLUSTER_ID_ON_OFF, ZigBeeOnOffClusterHandler.class);
            clusterMap.put(ZigBeeApiConstants.CLUSTER_ID_LEVEL_CONTROL, ZigBeeLevelClusterHandler.class);
            clusterMap.put(ZigBeeApiConstants.CLUSTER_ID_COLOR_CONTROL, ZigBeeColorClusterHandler.class);
            clusterMap.put(ZigBeeApiConstants.CLUSTER_ID_RELATIVE_HUMIDITY_MEASUREMENT,
                    ZigBeeRelativeHumidityMeasurementClusterHandler.class);
            clusterMap.put(ZigBeeApiConstants.CLUSTER_ID_TEMPERATURE_MEASUREMENT,
                    ZigBeeTemperatureMeasurementClusterHandler.class);
        }

        Constructor<? extends ZigBeeClusterHandler> constructor;
        try {
            if (clusterMap.get(clusterId) == null) {
                logger.warn("Cluster converter for cluster {}({})is not implemented!",
                        ZigBeeApiConstants.getClusterName(clusterId), clusterId);
                return null;
            }
            constructor = clusterMap.get(clusterId).getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            // logger.error("Command processor error");
        }

        return null;
    }

    protected void updateChannelState(State state) {
        thing.setChannelState(channelUID, state);
    }

    /**
     * Gets the configuration descriptions required for this cluster
     *
     * @return {@link ConfigDescription} null if no config is provided
     */
    public ConfigDescription getConfigDescription() {
        return null;
    }

    protected Channel createChannel(Device device, ThingUID thingUID, String channelType, String itemType,
            String label) {
        String endpointRef = "_" + device.getEndPointAddress();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ZigBeeBindingConstants.CHANNEL_PROPERTY_ADDRESS, device.getEndpointId());
        properties.put(ZigBeeBindingConstants.CHANNEL_PROPERTY_CLUSTER, Integer.toString(getClusterId()));
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(ZigBeeBindingConstants.BINDING_ID, channelType);

        return ChannelBuilder.create(new ChannelUID(thingUID, channelType + "_" + endpointRef), itemType)
                .withType(channelTypeUID).withLabel(label).withProperties(properties).build();
    }

    public abstract int getClusterId();
}
