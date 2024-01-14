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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.SUB_ADDRESS;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velbus.internal.VelbusModuleAddress;
import org.openhab.binding.velbus.internal.VelbusPacketListener;
import org.openhab.binding.velbus.internal.config.VelbusThingConfig;
import org.openhab.binding.velbus.internal.packets.VelbusReadMemoryBlockPacket;
import org.openhab.binding.velbus.internal.packets.VelbusReadMemoryPacket;
import org.openhab.binding.velbus.internal.packets.VelbusStatusRequestPacket;
import org.openhab.binding.velbus.internal.packets.VelbusWriteMemoryPacket;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base ThingHandler for all Velbus handlers.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public abstract class VelbusThingHandler extends BaseThingHandler implements VelbusPacketListener {
    protected final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected @Nullable VelbusThingConfig velbusThingConfig;
    private @Nullable VelbusBridgeHandler velbusBridgeHandler;
    private @NonNullByDefault({}) VelbusModuleAddress velbusModuleAddress;

    private int numberOfSubAddresses;

    public VelbusThingHandler(Thing thing, int numberOfSubAddresses) {
        super(thing);

        this.numberOfSubAddresses = numberOfSubAddresses;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing velbus handler.");

        this.velbusThingConfig = getConfigAs(VelbusThingConfig.class);

        Bridge bridge = getBridge();
        initializeThing(bridge == null ? ThingStatus.OFFLINE : bridge.getStatus());
        initializeChannelNames();
        initializeChannelStates();
    }

    @Override
    public void handleRemoval() {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();

        if (velbusBridgeHandler != null && velbusModuleAddress != null) {
            byte[] activeAddresses = velbusModuleAddress.getActiveAddresses();

            for (int i = 0; i < activeAddresses.length; i++) {
                velbusBridgeHandler.unregisterRelayStatusListener(activeAddresses[i]);
            }
        }

        super.handleRemoval();
    }

    protected VelbusModuleAddress getModuleAddress() {
        return velbusModuleAddress;
    }

    protected void updateChannelLabel(ChannelUID channelUID, String channelName) {
        Channel existingChannel = thing.getChannel(channelUID.getId());
        if (existingChannel != null) {
            String acceptedItem = existingChannel.getAcceptedItemType();
            Configuration configuration = existingChannel.getConfiguration();
            Set<String> defaultTags = existingChannel.getDefaultTags();
            String description = existingChannel.getDescription();
            ChannelKind kind = existingChannel.getKind();
            Map<String, String> properties = existingChannel.getProperties();
            ChannelTypeUID type = existingChannel.getChannelTypeUID();

            ThingBuilder thingBuilder = editThing();
            Channel channel = ChannelBuilder.create(channelUID, acceptedItem).withConfiguration(configuration)
                    .withDefaultTags(defaultTags).withDescription(description != null ? description : "").withKind(kind)
                    .withLabel(channelName).withProperties(properties).withType(type).build();
            thingBuilder.withoutChannel(channelUID).withChannel(channel);
            updateThing(thingBuilder.build());
        }
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        this.velbusModuleAddress = createVelbusModuleAddress(numberOfSubAddresses);

        if (this.velbusModuleAddress != null) {
            logger.debug("initializeThing thing {} with address {} bridge status {}", getThing().getUID(),
                    velbusModuleAddress.getAddress(), bridgeStatus);

            // note: this call implicitly registers our handler as a listener on
            // the bridge
            if (getVelbusBridgeHandler() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Address is not known!");
        }
    }

    protected @Nullable VelbusModuleAddress createVelbusModuleAddress(int numberOfSubAddresses) {
        final VelbusThingConfig velbusThingConfig = this.velbusThingConfig;
        if (velbusThingConfig != null) {
            byte address = hexToByte(velbusThingConfig.address);

            byte[] subAddresses = new byte[numberOfSubAddresses];
            for (int i = 0; i < numberOfSubAddresses; i++) {
                String propertyKey = SUB_ADDRESS + (i + 1);
                String subAddress = getThing().getProperties().get(propertyKey);
                if (subAddress != null) {
                    subAddresses[i] = hexToByte(subAddress);
                } else {
                    subAddresses[i] = (byte) 0xFF;
                }
            }

            return new VelbusModuleAddress(address, subAddresses);
        }

        return null;
    }

    private void initializeChannelNames() {
        List<Channel> channels = this.getThing().getChannels();
        for (int i = 0; i < channels.size(); i++) {
            Channel channel = channels.get(i);
            String channelUID = channel.getUID().getIdWithoutGroup();

            if (getConfig().containsKey(channelUID)) {
                String channelName = getConfig().get(channelUID).toString();
                if (!channelName.equals(channel.getLabel())) {
                    updateChannelLabel(channel.getUID(), channelName);
                }
            }
        }
    }

    private void initializeChannelStates() {
        VelbusBridgeHandler velbusBridgeHandler = this.velbusBridgeHandler;
        if (velbusBridgeHandler != null) {
            VelbusStatusRequestPacket packet = new VelbusStatusRequestPacket(getModuleAddress().getAddress());
            byte[] packetBytes = packet.getBytes();

            velbusBridgeHandler.sendPacket(packetBytes);
        }
    }

    protected byte hexToByte(String hexString) {
        if (hexString.length() > 2) {
            throw new IllegalArgumentException("hexString contains more than one byte: " + hexString);
        }

        return HexUtils.hexToBytes(hexString)[0];
    }

    protected void sendReadMemoryBlockPacket(VelbusBridgeHandler velbusBridgeHandler, int memoryAddress) {
        VelbusReadMemoryBlockPacket packet = new VelbusReadMemoryBlockPacket(getModuleAddress().getAddress(),
                memoryAddress);
        byte[] packetBytes = packet.getBytes();
        velbusBridgeHandler.sendPacket(packetBytes);
    }

    protected void sendReadMemoryPacket(VelbusBridgeHandler velbusBridgeHandler, int memoryAddress) {
        VelbusReadMemoryPacket packet = new VelbusReadMemoryPacket(getModuleAddress().getAddress(), memoryAddress);
        byte[] packetBytes = packet.getBytes();
        velbusBridgeHandler.sendPacket(packetBytes);
    }

    protected void sendWriteMemoryPacket(VelbusBridgeHandler velbusBridgeHandler, int memoryAddress, byte data) {
        VelbusWriteMemoryPacket packet = new VelbusWriteMemoryPacket(getModuleAddress().getAddress(), memoryAddress,
                data);
        byte[] packetBytes = packet.getBytes();
        velbusBridgeHandler.sendPacket(packetBytes);
    }

    protected @Nullable synchronized VelbusBridgeHandler getVelbusBridgeHandler() {
        if (this.velbusBridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof VelbusBridgeHandler velbusBridgeHandler) {
                this.velbusBridgeHandler = velbusBridgeHandler;

                if (velbusModuleAddress != null) {
                    byte[] activeAddresses = velbusModuleAddress.getActiveAddresses();

                    for (int i = 0; i < activeAddresses.length; i++) {
                        velbusBridgeHandler.registerPacketListener(activeAddresses[i], this);
                    }
                }
            }
        }

        return this.velbusBridgeHandler;
    }
}
