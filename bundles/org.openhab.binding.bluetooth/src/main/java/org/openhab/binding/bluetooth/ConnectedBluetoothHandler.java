/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.util.RetryFuture;
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
 */
@NonNullByDefault
public class ConnectedBluetoothHandler extends BeaconBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(ConnectedBluetoothHandler.class);
    private @Nullable Future<?> reconnectJob;
    private @Nullable Future<?> pendingDisconnect;

    private boolean alwaysConnected;
    private int idleDisconnectDelay = 1000;

    // we initially set the to scheduler so that we can keep this field non-null
    private ScheduledExecutorService connectionTaskExecutor = scheduler;

    public ConnectedBluetoothHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // super.initialize adds callbacks that might require the connectionTaskExecutor to be present, so we initialize
        // the connectionTaskExecutor first
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("bluetooth-connection" + thing.getThingTypeUID(), true));
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setRemoveOnCancelPolicy(true);
        connectionTaskExecutor = executor;

        super.initialize();

        if (thing.getStatus() == ThingStatus.OFFLINE) {
            // something went wrong in super.initialize() so we shouldn't initialize further here either
            return;
        }

        Object alwaysConnectRaw = getConfig().get(BluetoothBindingConstants.CONFIGURATION_ALWAYS_CONNECTED);
        alwaysConnected = !Boolean.FALSE.equals(alwaysConnectRaw);

        Object idleDisconnectDelayRaw = getConfig().get(BluetoothBindingConstants.CONFIGURATION_IDLE_DISCONNECT_DELAY);
        idleDisconnectDelay = 1000;
        if (idleDisconnectDelayRaw instanceof Number numberCommand) {
            idleDisconnectDelay = numberCommand.intValue();
        }

        // Start the recurrent job if the device is always connected
        // or if the Services where not yet discovered.
        // If the device is not always connected, the job will be terminated
        // after successful connection and the device disconnected after Service
        // discovery in `onServicesDiscovered()`.
        if (alwaysConnected || !device.isServicesDiscovered()) {
            reconnectJob = connectionTaskExecutor.scheduleWithFixedDelay(() -> {
                try {
                    if (device.getConnectionState() != ConnectionState.CONNECTED) {
                        if (device.connect()) {
                            if (!alwaysConnected) {
                                cancel(reconnectJob, false);
                                reconnectJob = null;
                            }
                        } else {
                            logger.debug("Failed to connect to {}", address);
                        }
                        // we do not set the Thing status here, because we will anyhow receive a call to
                        // onConnectionStateChange
                    } else {
                        // just in case it was already connected to begin with
                        updateStatus(ThingStatus.ONLINE);
                        if (!device.isServicesDiscovered() && !device.discoverServices()) {
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
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void dispose() {
        cancel(reconnectJob, true);
        reconnectJob = null;
        cancel(pendingDisconnect, true);
        pendingDisconnect = null;

        super.dispose();

        // just in case something goes really wrong in the core and it tries to dispose a handler before initializing it
        if (scheduler != connectionTaskExecutor) {
            connectionTaskExecutor.shutdownNow();
        }
    }

    private static void cancel(@Nullable Future<?> future, boolean interrupt) {
        if (future != null) {
            future.cancel(interrupt);
        }
    }

    public void connect() {
        connectionTaskExecutor.execute(() -> {
            if (!device.connect()) {
                logger.debug("Failed to connect to {}", address);
            }
        });
    }

    public void disconnect() {
        connectionTaskExecutor.execute(device::disconnect);
    }

    private void scheduleDisconnect() {
        cancel(pendingDisconnect, false);
        pendingDisconnect = connectionTaskExecutor.schedule(device::disconnect, idleDisconnectDelay,
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
        if (!device.awaitConnection(1, TimeUnit.SECONDS)) {
            throw new TimeoutException("Connection attempt timeout.");
        }
        if (!device.isServicesDiscovered()) {
            device.discoverServices();
            if (!device.awaitServiceDiscovery(10, TimeUnit.SECONDS)) {
                throw new TimeoutException("Service discovery timeout");
            }
        }
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

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private <T> CompletableFuture<T> executeWithConnection(UUID serviceUUID, UUID characteristicUUID,
            Function<BluetoothCharacteristic, CompletableFuture<T>> callable) {
        if (connectionTaskExecutor == scheduler) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("connectionTaskExecutor has not been initialized"));
        }
        if (connectionTaskExecutor.isShutdown()) {
            return CompletableFuture.failedFuture(new IllegalStateException("connectionTaskExecutor is shut down"));
        }
        // we use a RetryFuture because it supports running Callable instances
        return RetryFuture.callWithRetry(() ->
        // we block for completion here so that we keep the lock on the connectionTaskExecutor active.
        callable.apply(connectAndGetCharacteristic(serviceUUID, characteristicUUID)).get(), connectionTaskExecutor)
                // we make this completion async so that operations chained off the returned future
                // will not run on the connectionTaskExecutor
                .whenCompleteAsync((r, th) -> {
                    // we us a while loop here in case the exceptions get nested
                    while (th instanceof CompletionException || th instanceof ExecutionException) {
                        th = th.getCause();
                    }
                    if (th instanceof InterruptedException) {
                        // we don't want to schedule anything if we receive an interrupt
                        return;
                    }
                    if (th instanceof TimeoutException) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, th.getMessage());
                    }
                    if (!alwaysConnected) {
                        scheduleDisconnect();
                    }
                }, scheduler);
    }

    public CompletableFuture<@Nullable Void> enableNotifications(UUID serviceUUID, UUID characteristicUUID) {
        return executeWithConnection(serviceUUID, characteristicUUID, device::enableNotifications);
    }

    public CompletableFuture<@Nullable Void> writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] data,
            boolean enableNotification) {
        var future = executeWithConnection(serviceUUID, characteristicUUID, characteristic -> {
            if (enableNotification) {
                return device.enableNotifications(characteristic)
                        .thenCompose((v) -> device.writeCharacteristic(characteristic, data));
            } else {
                return device.writeCharacteristic(characteristic, data);
            }
        });
        if (logger.isDebugEnabled()) {
            future = future.whenComplete((v, t) -> {
                if (t == null) {
                    logger.debug("Characteristic {} from {} has written value {}", characteristicUUID, address,
                            HexUtils.bytesToHex(data));
                }
            });
        }
        return future;
    }

    public CompletableFuture<byte[]> readCharacteristic(UUID serviceUUID, UUID characteristicUUID) {
        var future = executeWithConnection(serviceUUID, characteristicUUID, device::readCharacteristic);
        if (logger.isDebugEnabled()) {
            future = future.whenComplete((data, t) -> {
                if (t == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Characteristic {} from {} has been read - value {}", characteristicUUID, address,
                                HexUtils.bytesToHex(data));
                    }
                }
            });
        }
        return future;
    }

    @Override
    protected void updateStatusBasedOnRssi(boolean receivedSignal) {
        // if there is no signal, we can be sure we are OFFLINE, but if there is a signal, we also have to check whether
        // we are connected.
        if (receivedSignal) {
            if (alwaysConnected) {
                if (device.getConnectionState() == ConnectionState.CONNECTED) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
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
                if (alwaysConnected) {
                    connectionTaskExecutor.submit(() -> {
                        if (device.getConnectionState() != ConnectionState.CONNECTED) {
                            if (!device.connect()) {
                                logger.debug("Error connecting to device after discovery.");
                            }
                        }
                    });
                }
                break;
            case CONNECTED:
                if (alwaysConnected) {
                    connectionTaskExecutor.submit(() -> {
                        if (!device.isServicesDiscovered() && !device.discoverServices()) {
                            logger.debug("Error while discovering services");
                        }
                    });
                }
                break;
            case DISCONNECTED:
                cancel(pendingDisconnect, false);
                if (alwaysConnected) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] value) {
        super.onCharacteristicUpdate(characteristic, value);
        if (logger.isDebugEnabled()) {
            logger.debug("Recieved update {} to characteristic {} of device {}", HexUtils.bytesToHex(value),
                    characteristic.getUuid(), address);
        }
    }

    @Override
    public void onDescriptorUpdate(BluetoothDescriptor descriptor, byte[] value) {
        super.onDescriptorUpdate(descriptor, value);
        if (logger.isDebugEnabled()) {
            logger.debug("Received update {} to descriptor {} of device {}", HexUtils.bytesToHex(value),
                    descriptor.getUuid(), address);
        }
    }

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();

        if (!alwaysConnected && device.getConnectionState() == ConnectionState.CONNECTED) {
            // disconnect when the device was only connected to discover the Services.
            disconnect();
        }
    }
}
