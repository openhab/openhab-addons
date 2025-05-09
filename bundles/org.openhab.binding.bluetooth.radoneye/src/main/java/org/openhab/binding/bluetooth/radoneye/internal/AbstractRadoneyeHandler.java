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
package org.openhab.binding.bluetooth.radoneye.internal;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.ConnectedBluetoothHandler;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractRadoneyeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Peter Obel - Initial contribution
 * @author Jörg Sautter - Use the ConnectedBluetoothHandler the handle the connection state
 */
@NonNullByDefault
public abstract class AbstractRadoneyeHandler extends ConnectedBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(AbstractRadoneyeHandler.class);
    private final AtomicLong isNotifying = new AtomicLong(-1);

    private RadoneyeConfiguration configuration = new RadoneyeConfiguration();
    private @Nullable ScheduledFuture<?> scheduledTask;

    public AbstractRadoneyeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize");
        super.initialize();
        configuration = getConfigAs(RadoneyeConfiguration.class);
        logger.debug("Using configuration: {}", configuration);
        cancelScheduledTask();
        logger.debug("Start scheduled task to read device in every {} seconds", configuration.refreshInterval);
        scheduledTask = scheduler.scheduleWithFixedDelay(this::execute, configuration.refreshInterval,
                configuration.refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Dispose");
        cancelScheduledTask();
        super.dispose();
    }

    private void cancelScheduledTask() {
        ScheduledFuture<?> task = scheduledTask;
        if (task != null) {
            task.cancel(false);
            scheduledTask = null;
        }
    }

    private void execute() {
        try {
            long since = isNotifying.get();

            if (since != -1) {
                logger.debug("Send trigger data to device {}", address);
                writeCharacteristic(getServiceUUID(), getTriggerUUID(), getTriggerData(), false).exceptionally((t) -> {
                    String message = "Failed to send trigger data to device " + address + ", disconnect";
                    logger.warn(message, t);
                    disconnect();
                    return null;
                });
            } else if (device.getConnectionState() == ConnectionState.CONNECTED && device.isServicesDiscovered()) {
                // we can enable the notifications multiple times, this is handled internally
                enableNotifications(getServiceUUID(), getDataUUID()).thenAccept((v) -> {
                    isNotifying.set(System.currentTimeMillis());
                }).exceptionally((t) -> {
                    String message = "Failed to enable notifications on device " + address + ", disconnect";
                    logger.warn(message, t);
                    disconnect();
                    return null;
                });
            } else {
                logger.debug("Device {} state is {}, discovered {}", address, device.getConnectionState(),
                        device.isServicesDiscovered());
            }
        } catch (Exception e) {
            String message = "Failed to execute for device " + address;
            logger.warn(message, e);
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] value) {
        super.onCharacteristicUpdate(characteristic, value);

        if (!getDataUUID().equals(characteristic.getUuid())) {
            return;
        }

        logger.debug("Characteristic {} from device {}: {}", characteristic.getUuid(), address, value);
        updateChannels(value);
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        super.onConnectionStateChange(connectionNotification);
        // stop sending triggers to a probably broken connection
        isNotifying.set(-1);

        if (connectionNotification.getConnectionState() == ConnectionState.CONNECTED) {
            // start discovering when super.onConnectionStateChange does not
            if (device.isServicesDiscovered() && !device.discoverServices()) {
                logger.debug("Error while discovering services");
            }
        }
    }

    /**
     * Provides the configured major firmware version
     *
     * @return the major firmware version configured
     */
    protected int getFwVersion() {
        return configuration.fwVersion;
    }

    /**
     * Provides the UUID of the service, which holds the characteristics
     *
     * @return the UUID of the data characteristic
     */
    protected abstract UUID getServiceUUID();

    /**
     * Provides the UUID of the characteristic, which holds the sensor data
     *
     * @return the UUID of the data characteristic
     */
    protected abstract UUID getDataUUID();

    /**
     * Provides the UUID of the characteristic, that triggers and update of the sensor data
     *
     * @return the UUID of the data characteristic
     */
    protected abstract UUID getTriggerUUID();

    /**
     * Provides the data that sent to the trigger characteristic will update the sensor data
     *
     * @return the trigger data as an byte array
     */
    protected abstract byte[] getTriggerData();

    /**
     * This method parses the content of the bluetooth characteristic and updates the Thing channels accordingly.
     *
     * @param is the content of the bluetooth characteristic
     */
    protected abstract void updateChannels(byte[] is);
}
