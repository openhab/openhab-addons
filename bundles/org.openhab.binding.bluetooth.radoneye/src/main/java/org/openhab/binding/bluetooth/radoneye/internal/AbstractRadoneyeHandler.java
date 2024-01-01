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
package org.openhab.binding.bluetooth.radoneye.internal;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.BluetoothUtils;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractRadoneyeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Peter Obel - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractRadoneyeHandler extends BeaconBluetoothHandler {

    private static final int CHECK_PERIOD_SEC = 10;

    private final Logger logger = LoggerFactory.getLogger(AbstractRadoneyeHandler.class);

    private AtomicInteger sinceLastReadSec = new AtomicInteger();
    private RadoneyeConfiguration configuration = new RadoneyeConfiguration();
    private @Nullable ScheduledFuture<?> scheduledTask;

    private volatile int errorConnectCounter;
    private volatile int errorReadCounter;
    private volatile int errorWriteCounter;
    private volatile int errorDisconnectCounter;
    private volatile int errorResolvingCounter;

    private volatile ServiceState serviceState = ServiceState.NOT_RESOLVED;
    private volatile ReadState readState = ReadState.IDLE;

    private enum ServiceState {
        NOT_RESOLVED,
        RESOLVING,
        RESOLVED
    }

    private enum ReadState {
        IDLE,
        READING,
        WRITING
    }

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
        scheduledTask = scheduler.scheduleWithFixedDelay(this::executePeridioc, CHECK_PERIOD_SEC, CHECK_PERIOD_SEC,
                TimeUnit.SECONDS);

        sinceLastReadSec.set(configuration.refreshInterval); // update immediately
    }

    @Override
    public void dispose() {
        logger.debug("Dispose");
        cancelScheduledTask();
        serviceState = ServiceState.NOT_RESOLVED;
        readState = ReadState.IDLE;
        super.dispose();
    }

    private void cancelScheduledTask() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            scheduledTask = null;
        }
    }

    private void executePeridioc() {
        sinceLastReadSec.addAndGet(CHECK_PERIOD_SEC);
        execute();
    }

    private synchronized void execute() {
        ConnectionState connectionState = device.getConnectionState();
        logger.debug("Device {} state is {}, serviceState {}, readState {}", address, connectionState, serviceState,
                readState);

        switch (connectionState) {
            case DISCOVERING:
            case DISCOVERED:
            case DISCONNECTED:
                if (isTimeToRead()) {
                    connect();
                }
                break;
            case CONNECTED:
                read();
                break;
            default:
                break;
        }
    }

    private void connect() {
        logger.debug("Connect to device {}...", address);
        if (!device.connect()) {
            errorConnectCounter++;
            if (errorConnectCounter < 6) {
                logger.debug("Connecting to device {} failed {} times", address, errorConnectCounter);
            } else {
                logger.debug("ERROR:  Controller reset needed.  Connecting to device {} failed {} times", address,
                        errorConnectCounter);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connecting to device failed");
            }
        } else {
            logger.debug("Connected to device {}", address);
            errorConnectCounter = 0;
        }
    }

    private void disconnect() {
        logger.debug("Disconnect from device {}...", address);
        if (!device.disconnect()) {
            errorDisconnectCounter++;
            if (errorDisconnectCounter < 6) {
                logger.debug("Disconnect from device {} failed {} times", address, errorDisconnectCounter);
            } else {
                logger.debug("ERROR:  Controller reset needed.  Disconnect from device {} failed {} times", address,
                        errorDisconnectCounter);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Disconnect from device failed");
            }
        } else {
            logger.debug("Disconnected from device {}", address);
            errorDisconnectCounter = 0;
        }
    }

    private void read() {
        switch (serviceState) {
            case NOT_RESOLVED:
                logger.debug("Discover services on device {}", address);
                discoverServices();
                break;
            case RESOLVED:
                switch (readState) {
                    case IDLE:
                        if (getTriggerUUID() != null) {
                            logger.debug("Send trigger data to device {}...", address);
                            BluetoothCharacteristic characteristic = device.getCharacteristic(getTriggerUUID());
                            if (characteristic != null) {
                                readState = ReadState.WRITING;
                                errorWriteCounter = 0;
                                device.writeCharacteristic(characteristic, getTriggerData()).whenComplete((v, ex) -> {
                                    readSensorData();
                                });
                            } else {
                                errorWriteCounter++;
                                if (errorWriteCounter < 6) {
                                    logger.debug("Read/write data from device {} failed {} times", address,
                                            errorWriteCounter);
                                } else {
                                    logger.debug(
                                            "ERROR:  Controller reset needed.  Read/write data from device {} failed {} times",
                                            address, errorWriteCounter);
                                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                            "Read/write data from device failed");
                                }
                                disconnect();
                            }
                        } else {
                            readSensorData();
                        }

                        break;
                    default:
                        logger.debug("Unhandled Resolved readState {} on device {}", readState, address);
                        break;
                }
                break;
            default: // serviceState RESOLVING
                errorResolvingCounter++;
                if (errorResolvingCounter < 6) {
                    logger.debug("Unhandled serviceState {} on device {}", serviceState, address);
                } else {
                    logger.debug("ERROR:  Controller reset needed.  Unhandled serviceState {} on device {}",
                            serviceState, address);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Service discovery for device failed");
                }
                break;
        }
    }

    private void readSensorData() {
        logger.debug("Read data from device {}...", address);
        BluetoothCharacteristic characteristic = device.getCharacteristic(getDataUUID());
        if (characteristic != null) {
            readState = ReadState.READING;
            errorReadCounter = 0;
            errorResolvingCounter = 0;
            device.readCharacteristic(characteristic).whenComplete((data, ex) -> {
                try {
                    logger.debug("Characteristic {} from device {}: {}", characteristic.getUuid(), address, data);
                    updateStatus(ThingStatus.ONLINE);
                    sinceLastReadSec.set(0);
                    updateChannels(BluetoothUtils.toIntArray(data));
                } finally {
                    readState = ReadState.IDLE;
                    disconnect();
                }
            });
        } else {
            errorReadCounter++;
            if (errorReadCounter < 6) {
                logger.debug("Read data from device {} failed {} times", address, errorReadCounter);
            } else {
                logger.debug("ERROR:  Controller reset needed.  Read data from device {} failed {} times", address,
                        errorReadCounter);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Read data from device failed");
            }
            disconnect();
        }
    }

    private void discoverServices() {
        logger.debug("Discover services for device {}", address);
        serviceState = ServiceState.RESOLVING;
        device.discoverServices();
    }

    @Override
    public void onServicesDiscovered() {
        serviceState = ServiceState.RESOLVED;
        logger.debug("Service discovery completed for device {}", address);
        printServices();
        execute();
    }

    private void printServices() {
        device.getServices().forEach(service -> logger.debug("Device {} Service '{}'", address, service));
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        logger.debug("Connection State Change Event is {}", connectionNotification.getConnectionState());
        switch (connectionNotification.getConnectionState()) {
            case DISCONNECTED:
                if (serviceState == ServiceState.RESOLVING) {
                    serviceState = ServiceState.NOT_RESOLVED;
                }
                readState = ReadState.IDLE;
                break;
            default:
                break;

        }
        execute();
    }

    private boolean isTimeToRead() {
        int sinceLastRead = sinceLastReadSec.get();
        logger.debug("Time since last update: {} sec", sinceLastRead);
        return sinceLastRead >= configuration.refreshInterval;
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
    protected abstract void updateChannels(int[] is);
}
