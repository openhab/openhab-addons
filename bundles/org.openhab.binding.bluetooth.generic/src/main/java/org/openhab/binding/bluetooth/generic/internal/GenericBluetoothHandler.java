/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.generic.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.ConnectedBluetoothHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;

/**
 * This is a handler for generic connected bluetooth devices that dynamically generates
 * channels based off of a bluetooth device's GATT characteristics.
 *
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public class GenericBluetoothHandler extends ConnectedBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(GenericBluetoothHandler.class);
    private final Map<BluetoothCharacteristic, GattChannelHandler> channelHandlers = new ConcurrentHashMap<>();

    private final ChannelCallback channelCallback = new ChannelCallback();
    private final BluetoothGattParser gattParser;
    private @Nullable Future<?> readCharacteristicJob = null;

    public GenericBluetoothHandler(Thing thing, BluetoothGattParser gattParser) {
        super(thing);
        this.gattParser = gattParser;
    }

    @Override
    public void initialize() {
        super.initialize();

        readCharacteristicJob = scheduler.scheduleWithFixedDelay(() -> {
            if (device.getConnectionState() == ConnectionState.CONNECTED) {
                if (resolved) {
                    for (BluetoothCharacteristic characteristic : deviceCharacteristics) {
                        device.readCharacteristic(characteristic);
                    }
                } else {
                    // if we are connected and still haven't been able to resolve the services, try disconnecting and
                    // trying connecting again
                    device.disconnect();
                }
            }
        }, 15, 30, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();

        Future<?> future = readCharacteristicJob;
        if (future != null) {
            future.cancel(true);
        }
    }

    @Override
    public void onServicesDiscovered() {
        if (!resolved) {
            resolved = true;
            logger.warn("Service discovery completed for '{}'", address);
            updateThingChannels();
        }
    }

    @Override
    public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
        super.onCharacteristicReadComplete(characteristic, status);
        if (status == BluetoothCompletionStatus.SUCCESS) {
            byte[] data = characteristic.getByteValue();
            getGattChannelHandler(characteristic).handleCharacteristicUpdate(data);
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        super.onCharacteristicUpdate(characteristic);
        byte[] data = characteristic.getByteValue();
        getGattChannelHandler(characteristic).handleCharacteristicUpdate(data);
    }

    private void updateThingChannels() {
        List<Channel> channels = device.getServices().stream()//
                .flatMap(service -> service.getCharacteristics().stream())//
                .flatMap(characteristic -> {
                    logger.trace("{} processing characteristic {}", address, characteristic.getUuid());
                    GattChannelHandler handler = getGattChannelHandler(characteristic);
                    if (handler.canRead()) {
                        deviceCharacteristics.add(characteristic);
                    }
                    return handler.buildChannels().stream();
                })//
                .collect(Collectors.toList());

        ThingBuilder builder = editThing();
        boolean changed = false;
        for (Channel channel : channels) {
            logger.trace("{} attempting to add channel {}", address, channel.getLabel());
            // we only want to add each channel, not replace all of them
            if (getThing().getChannel(channel.getUID()) == null) {
                changed = true;
                builder.withChannel(channel);
            }
        }
        if (changed) {
            updateThing(builder.build());
        }
    }

    private GattChannelHandler getGattChannelHandler(BluetoothCharacteristic characteristic) {
        return channelHandlers.computeIfAbsent(characteristic,
                ch -> new GattChannelHandler(channelCallback, gattParser, ch));
    }

    private class ChannelCallback implements ChannelHandlerCallback {

        @Override
        public ThingUID getThingUID() {
            return getThing().getUID();
        }

        @Override
        public void updateState(ChannelUID channelUID, State state) {
            GenericBluetoothHandler.this.updateState(channelUID, state);
        }

        @Override
        public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
            GenericBluetoothHandler.this.updateStatus(status, statusDetail, description);
        }

        @Override
        public boolean writeCharacteristic(BluetoothCharacteristic characteristic, byte[] data) {
            characteristic.setValue(data);
            return device.writeCharacteristic(characteristic);
        }
    }
}
