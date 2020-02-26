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
package org.openhab.binding.bluetooth;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.bluetooth.BluetoothCharacteristic.GattCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a handler for generic Bluetooth devices in connected mode, which at the same time can be used
 * as a base implementation for more specific thing handlers.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE, DefaultLocation.ARRAY_CONTENTS,
        DefaultLocation.TYPE_ARGUMENT, DefaultLocation.TYPE_BOUND, DefaultLocation.TYPE_PARAMETER })
public class ConnectedBluetoothHandler extends BeaconBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(ConnectedBluetoothHandler.class);
    private ScheduledFuture<?> connectionJob;

    // internal flag for the service resolution status
    protected volatile Boolean resolved = false;

    protected final Set<BluetoothCharacteristic> deviceCharacteristics = new CopyOnWriteArraySet<>();

    public ConnectedBluetoothHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        connectionJob = scheduler.scheduleWithFixedDelay(() -> {
            if (device.getConnectionState() != ConnectionState.CONNECTED) {
                device.connect();
                // we do not set the Thing status here, because we will anyhow receive a call to onConnectionStateChange
            }
            updateRSSI();
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (connectionJob != null) {
            connectionJob.cancel(true);
            connectionJob = null;
        }
        scheduler.submit(() -> {
            try {
                deviceLock.lock();
                if (device != null) {
                    device.removeListener(this);
                    device.disconnect();
                    device = null;
                }
            } finally {
                deviceLock.unlock();
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        // Handle REFRESH
        if (command == RefreshType.REFRESH) {
            for (BluetoothCharacteristic characteristic : deviceCharacteristics) {
                if (characteristic.getGattCharacteristic() != null
                        && channelUID.getId().equals(characteristic.getGattCharacteristic().name())) {
                    device.readCharacteristic(characteristic);
                    break;
                }
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
    }

    @Override
    protected void updateStatusBasedOnRssi(boolean receivedSignal) {
        // if there is no signal, we can be sure we are OFFLINE, but if there is a signal, we also have to check whether
        // we are connected.
        if (receivedSignal) {
            if (device.getConnectionState() == ConnectionState.CONNECTED) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device is not connected.");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        switch (connectionNotification.getConnectionState()) {
            case CONNECTED:
                updateStatus(ThingStatus.ONLINE);
                scheduler.submit(() -> device.discoverServices().whenComplete((context, th) -> {
                    if (th != null) {
                        logger.debug("Error while discovering services", th);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        return;
                    }
                    logger.debug("Service discovery completed for '{}'", address);
                    for (BluetoothService service : context.getServices()) {
                        for (BluetoothCharacteristic characteristic : service.getCharacteristics()) {
                            if (characteristic.getGattCharacteristic() != null) {
                                if (characteristic.getGattCharacteristic().equals(GattCharacteristic.BATTERY_LEVEL)) {
                                    activateChannel(characteristic,
                                            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_BATTERY_LEVEL.getUID(),
                                            this::updateBatteryLevel);
                                    continue;
                                }
                                logger.debug("Added GATT characteristic '{}'",
                                        characteristic.getGattCharacteristic().name());
                            }
                        }
                    }
                }));
                break;
            case DISCONNECTED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                break;
            default:
                break;
        }

    }

    protected void updateBatteryLevel(byte[] value) {
        // the byte has values from 0-255, which we need to map to 0-100
        Double level = (value[0] & 0xFF) / 2.55;
        updateState(BluetoothCharacteristic.GattCharacteristic.BATTERY_LEVEL.name(), new DecimalType(level.intValue()));
    }

    protected void activateChannel(@Nullable BluetoothCharacteristic characteristic, ChannelTypeUID channelTypeUID,
            Consumer<byte[]> handler, @Nullable String name) {
        if (characteristic == null) {
            logger.debug("Characteristic is null - not activating any channel.");
            return;
        }
        String channelId = name != null ? name : characteristic.getGattCharacteristic().name();
        if (channelId == null) {
            // use the type id as a fallback
            channelId = channelTypeUID.getId();
        }
        if (getThing().getChannel(channelId) == null) {
            // the channel does not exist yet, so let's add it
            ThingBuilder updatedThing = editThing();
            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), channelId), "Number")
                    .withType(channelTypeUID).build();
            updatedThing.withChannel(channel);
            updateThing(updatedThing.build());
            logger.debug("Added channel '{}' to Thing '{}'", channelId, getThing().getUID());
        }
        if (deviceCharacteristics.add(characteristic)) {
            device.enableNotifications(characteristic, handler);
        }

        if (isLinked(channelId)) {
            device.readCharacteristic(characteristic).thenAccept(handler);
        }
    }

    protected void activateChannel(@Nullable BluetoothCharacteristic characteristic, ChannelTypeUID channelTypeUID,
            Consumer<byte[]> handler) {
        activateChannel(characteristic, channelTypeUID, handler, null);
    }

}
