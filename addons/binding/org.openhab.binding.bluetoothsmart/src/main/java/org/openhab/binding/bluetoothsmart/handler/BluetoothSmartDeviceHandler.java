/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bluetoothsmart.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.openhab.binding.bluetoothsmart.BluetoothSmartBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.manager.BluetoothManager;
import org.sputnikdev.bluetooth.manager.BluetoothSmartDeviceListener;
import org.sputnikdev.bluetooth.manager.GattService;

/**
 *
 * 
 * @author Vlad Kolotov - Initial contribution
 */
public class BluetoothSmartDeviceHandler extends GenericBluetoothDeviceHandler
        implements BluetoothSmartDeviceListener {

    private Logger logger = LoggerFactory.getLogger(BluetoothSmartDeviceHandler.class);
    private boolean initialConnectionControl;

    private final BooleanTypeChannelHandler connectedHandler = new BooleanTypeChannelHandler(
            BluetoothSmartDeviceHandler.this, BluetoothSmartBindingConstants.CHANNEL_CONNECTED) {
        @Override Boolean getValue() {
            return getGovernor().isConnected();
        }
    };

    private final SingleChannelHandler<Boolean, OnOffType> connectionControlHandler = new BooleanTypeChannelHandler(
            BluetoothSmartDeviceHandler.this, BluetoothSmartBindingConstants.CHANNEL_CONNECTION_CONTROL, true) {
        @Override Boolean getValue() {
            return getGovernor().getConnectionControl();
        }
        @Override void updateThing(Boolean value) {
            getGovernor().setConnectionControl(value);
        }
        @Override Boolean getDefaultValue() {
            return BluetoothSmartDeviceHandler.this.initialConnectionControl;
        }
    };

    private final BluetoothSmartChannelBuilder channelBuilder;


    public BluetoothSmartDeviceHandler(Thing thing, ItemRegistry itemRegistry,
            BluetoothManager bluetoothManager, BluetoothGattParser gattParser) {
        super(thing, itemRegistry, bluetoothManager, gattParser);
        addChannelHandler(connectedHandler);
        addChannelHandler(connectionControlHandler);
        this.channelBuilder = new BluetoothSmartChannelBuilder(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        getGovernor().addBluetoothSmartDeviceListener(this);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void connected() {
        connectedHandler.updateChannel(true);
    }
    @Override
    public void disconnected() {
        connectedHandler.updateChannel(false);
    }

    @Override
    public void servicesResolved(List<GattService> gattServices) {
        ThingBuilder builder = editThing();

        logger.info("Building channels for services: {}", gattServices.size());
        Map<MultiChannelHandler, List<Channel>> channels = channelBuilder.buildChannels(gattServices);

        for (Map.Entry<MultiChannelHandler, List<Channel>> entry : channels.entrySet()) {
            ChannelHandler channelHandler = entry.getKey();
            addChannelHandler(channelHandler);
            updateChannels(builder, entry.getValue());
        }

        logger.info("Updating the thing with new channels");
        updateThing(builder.build());

        for (MultiChannelHandler channelHandler : channels.keySet()) {
            try {
                channelHandler.initChannels();
            } catch (Exception ex) {
                logger.error("Could not update channel handler: {}", channelHandler.getURL(), ex);
            }
        }
    }

    @Override
    public void servicesUnresolved() { }

    public boolean isInitialConnectionControl() {
        return initialConnectionControl;
    }

    public void setInitialConnectionControl(boolean initialConnectionControl) {
        this.initialConnectionControl = initialConnectionControl;
    }

    private void updateChannels(ThingBuilder builder, Collection<Channel> channels) {
        for (Channel channel : channels) {
            if (getThing().getChannel(channel.getUID().getIdWithoutGroup()) == null) {
                builder.withChannel(channel);
            }
        }
    }

}
