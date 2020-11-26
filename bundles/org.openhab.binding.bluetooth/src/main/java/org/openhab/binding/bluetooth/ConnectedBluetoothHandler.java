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

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a base implementation for more specific thing handlers that require constant connection to bluetooth devices.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE, DefaultLocation.ARRAY_CONTENTS,
        DefaultLocation.TYPE_ARGUMENT, DefaultLocation.TYPE_BOUND, DefaultLocation.TYPE_PARAMETER })
public class ConnectedBluetoothHandler extends BeaconBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(ConnectedBluetoothHandler.class);
    private ScheduledFuture<?> connectionJob;

    // internal flag for the service resolution status
    protected volatile boolean resolved = false;

    protected final Set<BluetoothCharacteristic> deviceCharacteristics = new CopyOnWriteArraySet<>();

    public ConnectedBluetoothHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        connectionJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (device.getConnectionState() != ConnectionState.CONNECTED) {
                    device.connect();
                    // we do not set the Thing status here, because we will anyhow receive a call to
                    // onConnectionStateChange
                } else {
                    // just in case it was already connected to begin with
                    updateStatus(ThingStatus.ONLINE);
                    if (!resolved && !device.discoverServices()) {
                        logger.debug("Error while discovering services");
                    }
                }
            } catch (RuntimeException ex) {
                logger.warn("Unexpected error occurred", ex);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (connectionJob != null) {
            connectionJob.cancel(true);
            connectionJob = null;
        }
        super.dispose();
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
        super.onConnectionStateChange(connectionNotification);
        switch (connectionNotification.getConnectionState()) {
            case DISCOVERED:
                // The device is now known on the Bluetooth network, so we can do something...
                scheduler.submit(() -> {
                    if (device.getConnectionState() != ConnectionState.CONNECTED) {
                        if (!device.connect()) {
                            logger.debug("Error connecting to device after discovery.");
                        }
                    }
                });
                break;
            case CONNECTED:
                updateStatus(ThingStatus.ONLINE);
                scheduler.submit(() -> {
                    if (!resolved && !device.discoverServices()) {
                        logger.debug("Error while discovering services");
                    }
                });
                break;
            case DISCONNECTED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                break;
            default:
                break;
        }
    }

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();
        if (!resolved) {
            resolved = true;
            logger.debug("Service discovery completed for '{}'", address);
        }
    }

    @Override
    public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
        super.onCharacteristicReadComplete(characteristic, status);
        if (status == BluetoothCompletionStatus.SUCCESS) {
            if (logger.isDebugEnabled()) {
                logger.debug("Characteristic {} from {} has been read - value {}", characteristic.getUuid(), address,
                        HexUtils.bytesToHex(characteristic.getByteValue()));
            }
        } else {
            logger.debug("Characteristic {} from {} has been read - ERROR", characteristic.getUuid(), address);
        }
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
        super.onCharacteristicWriteComplete(characteristic, status);
        if (logger.isDebugEnabled()) {
            logger.debug("Wrote {} to characteristic {} of device {}: {}",
                    HexUtils.bytesToHex(characteristic.getByteValue()), characteristic.getUuid(), address, status);
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        super.onCharacteristicUpdate(characteristic);
        if (logger.isDebugEnabled()) {
            logger.debug("Recieved update {} to characteristic {} of device {}",
                    HexUtils.bytesToHex(characteristic.getByteValue()), characteristic.getUuid(), address);
        }
    }

    @Override
    public void onDescriptorUpdate(BluetoothDescriptor descriptor) {
        super.onDescriptorUpdate(descriptor);
        if (logger.isDebugEnabled()) {
            logger.debug("Received update {} to descriptor {} of device {}", HexUtils.bytesToHex(descriptor.getValue()),
                    descriptor.getUuid(), address);
        }
    }
}
