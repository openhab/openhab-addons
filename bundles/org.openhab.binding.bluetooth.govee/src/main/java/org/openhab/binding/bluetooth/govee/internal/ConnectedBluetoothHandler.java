/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.govee.internal;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a base implementation for more specific thing handlers that require constant connection to bluetooth devices.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @deprecated once CompletableFutures are supported in the actual ConnectedBluetoothHandler, this class can be deleted
 */
@Deprecated
@NonNullByDefault
public class ConnectedBluetoothHandler extends BeaconBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(ConnectedBluetoothHandler.class);

    private final Condition connectionCondition = deviceLock.newCondition();
    private final Condition serviceDiscoveryCondition = deviceLock.newCondition();
    private final Condition charCompleteCondition = deviceLock.newCondition();

    private @Nullable Future<?> reconnectJob;
    private @Nullable Future<?> pendingDisconnect;
    private @Nullable BluetoothCharacteristic ongoingCharacteristic;
    private @Nullable BluetoothCompletionStatus completeStatus;

    private boolean connectOnDemand;
    private int idleDisconnectDelayMs = 1000;

    protected @Nullable ScheduledExecutorService connectionTaskExecutor;
    private volatile boolean servicesDiscovered;

    public ConnectedBluetoothHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {

        // super.initialize adds callbacks that might require the connectionTaskExecutor to be present, so we initialize
        // the connectionTaskExecutor first
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("bluetooth-connection-" + thing.getThingTypeUID(), true));
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setRemoveOnCancelPolicy(true);
        connectionTaskExecutor = executor;

        super.initialize();

        connectOnDemand = true;

        Object idleDisconnectDelayRaw = getConfig().get("idleDisconnectDelay");
        idleDisconnectDelayMs = 1000;
        if (idleDisconnectDelayRaw instanceof Number) {
            idleDisconnectDelayMs = ((Number) idleDisconnectDelayRaw).intValue();
        }

        if (!connectOnDemand) {
            reconnectJob = executor.scheduleWithFixedDelay(() -> {
                try {
                    if (device.getConnectionState() != ConnectionState.CONNECTED) {
                        device.connect();
                        // we do not set the Thing status here, because we will anyhow receive a call to
                        // onConnectionStateChange
                    } else {
                        // just in case it was already connected to begin with
                        updateStatus(ThingStatus.ONLINE);
                        if (!servicesDiscovered && !device.discoverServices()) {
                            logger.debug("Error while discovering services");
                        }
                    }
                } catch (RuntimeException ex) {
                    logger.warn("Unexpected error occurred", ex);
                }
            }, 0, 30, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        cancel(reconnectJob);
        reconnectJob = null;
        cancel(pendingDisconnect);
        pendingDisconnect = null;

        super.dispose();

        shutdown(connectionTaskExecutor);
        connectionTaskExecutor = null;
    }

    private static void cancel(@Nullable Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    private void shutdown(@Nullable ScheduledExecutorService executor) {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private ScheduledExecutorService getConnectionTaskExecutor() {
        var executor = connectionTaskExecutor;
        if (executor == null) {
            throw new IllegalStateException("characteristicScheduler has not been initialized");
        }
        return executor;
    }

    private void scheduleDisconnect() {
        cancel(pendingDisconnect);
        pendingDisconnect = getConnectionTaskExecutor().schedule(device::disconnect, idleDisconnectDelayMs,
                TimeUnit.MILLISECONDS);
    }

    private void connectAndWait() throws ConnectionException, TimeoutException, InterruptedException {
        if (device.getConnectionState() == ConnectionState.CONNECTED) {
            return;
        }
        if (device.getConnectionState() != ConnectionState.CONNECTING) {
            if (!device.connect()) {
                throw new ConnectionException("Failed to start connecting");
            }
        }
        logger.debug("waiting for connection");
        if (!awaitConnection(1, TimeUnit.SECONDS)) {
            throw new TimeoutException("Connection attempt timeout.");
        }
        logger.debug("connection successful");
        if (!servicesDiscovered) {
            logger.debug("discovering services");
            device.discoverServices();
            if (!awaitServiceDiscovery(20, TimeUnit.SECONDS)) {
                throw new TimeoutException("Service discovery timeout");
            }
            logger.debug("service discovery successful");
        }
    }

    private boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException {
        deviceLock.lock();
        try {
            long nanosTimeout = unit.toNanos(timeout);
            while (device.getConnectionState() != ConnectionState.CONNECTED) {
                if (nanosTimeout <= 0L) {
                    return false;
                }
                nanosTimeout = connectionCondition.awaitNanos(nanosTimeout);
            }
        } finally {
            deviceLock.unlock();
        }
        return true;
    }

    private boolean awaitCharacteristicComplete(long timeout, TimeUnit unit) throws InterruptedException {
        deviceLock.lock();
        try {
            long nanosTimeout = unit.toNanos(timeout);
            while (ongoingCharacteristic != null) {
                if (nanosTimeout <= 0L) {
                    return false;
                }
                nanosTimeout = charCompleteCondition.awaitNanos(nanosTimeout);
            }
        } finally {
            deviceLock.unlock();
        }
        return true;
    }

    private boolean awaitServiceDiscovery(long timeout, TimeUnit unit) throws InterruptedException {
        deviceLock.lock();
        try {
            long nanosTimeout = unit.toNanos(timeout);
            while (!servicesDiscovered) {
                if (nanosTimeout <= 0L) {
                    return false;
                }
                nanosTimeout = serviceDiscoveryCondition.awaitNanos(nanosTimeout);
            }
        } finally {
            deviceLock.unlock();
        }
        return true;
    }

    private BluetoothCharacteristic connectAndGetCharacteristic(UUID serviceUUID, UUID characteristicUUID)
            throws BluetoothException, TimeoutException, InterruptedException {
        connectAndWait();
        BluetoothService service = device.getServices(serviceUUID);
        if (service == null) {
            throw new BluetoothException("Service with uuid " + serviceUUID + " could not be found");
        }
        BluetoothCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            throw new BluetoothException("Characteristic with uuid " + characteristicUUID + " could not be found");
        }
        return characteristic;
    }

    private <T> CompletableFuture<T> executeWithConnection(UUID serviceUUID, UUID characteristicUUID,
            CallableFunction<BluetoothCharacteristic, T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        var executor = connectionTaskExecutor;
        if (executor != null) {
            executor.execute(() -> {
                cancel(pendingDisconnect);
                try {
                    BluetoothCharacteristic characteristic = connectAndGetCharacteristic(serviceUUID,
                            characteristicUUID);
                    future.complete(callable.call(characteristic));
                } catch (InterruptedException e) {
                    future.completeExceptionally(e);
                    return;// we don't want to schedule anything if we receive an interrupt
                } catch (TimeoutException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    future.completeExceptionally(e);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
                if (connectOnDemand) {
                    scheduleDisconnect();
                }
            });
        } else {
            future.completeExceptionally(new IllegalStateException("characteristicScheduler has not been initialized"));
        }
        return future;
    }

    public CompletableFuture<@Nullable Void> enableNotifications(UUID serviceUUID, UUID characteristicUUID) {
        return executeWithConnection(serviceUUID, characteristicUUID, characteristic -> {
            if (!device.enableNotifications(characteristic)) {
                throw new BluetoothException(
                        "Failed to start notifications for characteristic: " + characteristic.getUuid());
            }
            return null;
        });
    }

    public CompletableFuture<@Nullable Void> writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] data,
            boolean enableNotification) {
        return executeWithConnection(serviceUUID, characteristicUUID, characteristic -> {
            if (enableNotification) {
                if (!device.enableNotifications(characteristic)) {
                    throw new BluetoothException(
                            "Failed to start characteristic notification" + characteristic.getUuid());
                }
            }
            // now block for completion
            characteristic.setValue(data);
            ongoingCharacteristic = characteristic;
            if (!device.writeCharacteristic(characteristic)) {
                throw new BluetoothException("Failed to start writing characteristic " + characteristic.getUuid());
            }
            if (!awaitCharacteristicComplete(1, TimeUnit.SECONDS)) {
                ongoingCharacteristic = null;
                throw new TimeoutException(
                        "Timeout waiting for characteristic " + characteristic.getUuid() + " write to finish");
            }
            if (completeStatus == BluetoothCompletionStatus.ERROR) {
                throw new BluetoothException("Failed to write characteristic " + characteristic.getUuid());
            }
            logger.debug("Wrote {} to characteristic {} of device {}", HexUtils.bytesToHex(data),
                    characteristic.getUuid(), address);
            return null;
        });
    }

    public CompletableFuture<byte[]> readCharacteristic(UUID serviceUUID, UUID characteristicUUID) {
        return executeWithConnection(serviceUUID, characteristicUUID, characteristic -> {
            // now block for completion
            ongoingCharacteristic = characteristic;
            if (!device.readCharacteristic(characteristic)) {
                throw new BluetoothException("Failed to start reading characteristic " + characteristic.getUuid());
            }
            if (!awaitCharacteristicComplete(1, TimeUnit.SECONDS)) {
                ongoingCharacteristic = null;
                throw new TimeoutException(
                        "Timeout waiting for characteristic " + characteristic.getUuid() + " read to finish");
            }
            if (completeStatus == BluetoothCompletionStatus.ERROR) {
                throw new BluetoothException("Failed to read characteristic " + characteristic.getUuid());
            }
            byte[] data = characteristic.getByteValue();
            logger.debug("Characteristic {} from {} has been read - value {}", characteristic.getUuid(), address,
                    HexUtils.bytesToHex(data));
            return data;
        });
    }

    @Override
    protected void updateStatusBasedOnRssi(boolean receivedSignal) {
        // if there is no signal, we can be sure we are OFFLINE, but if there is a signal, we also have to check whether
        // we are connected.
        if (receivedSignal) {
            if (device.getConnectionState() == ConnectionState.CONNECTED) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                if (!connectOnDemand) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device is not connected.");
                }
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
                if (!connectOnDemand) {
                    getConnectionTaskExecutor().submit(() -> {
                        if (device.getConnectionState() != ConnectionState.CONNECTED) {
                            if (!device.connect()) {
                                logger.debug("Error connecting to device after discovery.");
                            }
                        }
                    });
                }
                break;
            case CONNECTED:
                deviceLock.lock();
                try {
                    connectionCondition.signal();
                } finally {
                    deviceLock.unlock();
                }
                if (!connectOnDemand) {
                    getConnectionTaskExecutor().submit(() -> {
                        if (!servicesDiscovered && !device.discoverServices()) {
                            logger.debug("Error while discovering services");
                        }
                    });
                }
                break;
            case DISCONNECTED:
                var future = pendingDisconnect;
                if (future != null) {
                    future.cancel(false);
                }
                if (!connectOnDemand) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
        super.onCharacteristicReadComplete(characteristic, status);
        deviceLock.lock();
        try {
            if (ongoingCharacteristic != null && ongoingCharacteristic.getUuid().equals(characteristic.getUuid())) {
                completeStatus = status;
                ongoingCharacteristic = null;
                charCompleteCondition.signal();
            }
        } finally {
            deviceLock.unlock();
        }
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
        super.onCharacteristicWriteComplete(characteristic, status);
        deviceLock.lock();
        try {
            if (ongoingCharacteristic != null && ongoingCharacteristic.getUuid().equals(characteristic.getUuid())) {
                completeStatus = status;
                ongoingCharacteristic = null;
                charCompleteCondition.signal();
            }
        } finally {
            deviceLock.unlock();
        }
    }

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();
        deviceLock.lock();
        try {
            this.servicesDiscovered = true;
            serviceDiscoveryCondition.signal();
        } finally {
            deviceLock.unlock();
        }
        logger.debug("Service discovery completed for '{}'", address);
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

    public static class BluetoothException extends Exception {

        public BluetoothException(String message) {
            super(message);
        }
    }

    public static class ConnectionException extends BluetoothException {

        public ConnectionException(String message) {
            super(message);
        }
    }

    @FunctionalInterface
    public static interface CallableFunction<U, R> {
        public R call(U arg) throws Exception;
    }
}
