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
package org.openhab.binding.bluetooth;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
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
 */
@NonNullByDefault
public class ConnectedBluetoothHandler extends BeaconBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(ConnectedBluetoothHandler.class);
    private @Nullable Future<?> reconnectJob;
    private @Nullable Future<?> pendingDisconnect;

    private boolean connectOnDemand;
    private int idleDisconnectDelay = 1000;

    private @Nullable ScheduledExecutorService connectionTaskExecutor;

    public ConnectedBluetoothHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {

        // super.initialize adds callbacks that might require the connectionTaskExecutor to be present, so we initialize
        // the connectionTaskExecutor first
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("bluetooth-connection" + thing.getThingTypeUID(), true));
        connectionTaskExecutor = executor;

        super.initialize();

        Object connectOnDemandRaw = getConfig().get(BluetoothBindingConstants.CONFIGURATION_CONNECT_ON_DEMAND);
        connectOnDemand = Boolean.TRUE.equals(connectOnDemandRaw);

        Object idleDisconnectDelayRaw = getConfig().get(BluetoothBindingConstants.CONFIGURATION_IDLE_DISCONNECT_DELAY);
        idleDisconnectDelay = 1000;
        if (idleDisconnectDelayRaw instanceof Number) {
            idleDisconnectDelay = ((Number) idleDisconnectDelayRaw).intValue();
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
        pendingDisconnect = getConnectionTaskExecutor().schedule(device::disconnect, idleDisconnectDelay,
                TimeUnit.MILLISECONDS);
    }

    private void connectAndWait() throws ConnectionException, TimeoutException, InterruptedException {
        if (device.getConnectionState() == ConnectionState.CONNECTED) {
            return;
        }
        if (device.getConnectionState() != ConnectionState.CONNECTING) {
            if (device.connect()) {
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

    public CompletableFuture<@Nullable Void> writeCharacteristic(UUID serviceUUID, UUID characteristicUUID,
            byte[] data) {
        CompletableFuture<@Nullable Void> future = new CompletableFuture<>();
        var executor = connectionTaskExecutor;
        if (executor != null) {
            executor.execute(() -> {
                cancel(pendingDisconnect);
                try {
                    BluetoothCharacteristic characteristic = connectAndGetCharacteristic(serviceUUID,
                            characteristicUUID);
                    // now block for completion
                    device.writeCharacteristic(characteristic, data).get();
                    logger.debug("Wrote {} to characteristic {} of device {}", HexUtils.bytesToHex(data),
                            characteristic.getUuid(), address);
                    future.complete(null);
                } catch (InterruptedException e) {
                    future.completeExceptionally(e);
                    return;// we don't want to schedule anything if we receive an interrupt
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    cause = cause != null ? cause : e;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause.getMessage());
                    future.completeExceptionally(cause);
                } catch (BluetoothException e) {
                    future.completeExceptionally(e);
                } catch (TimeoutException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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

    public CompletableFuture<byte[]> readCharacteristic(UUID serviceUUID, UUID characteristicUUID) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        var executor = connectionTaskExecutor;
        if (executor != null) {
            executor.execute(() -> {
                cancel(pendingDisconnect);
                try {
                    BluetoothCharacteristic characteristic = connectAndGetCharacteristic(serviceUUID,
                            characteristicUUID);
                    // now block for completion
                    byte[] data = device.readCharacteristic(characteristic).get();
                    logger.debug("Characteristic {} from {} has been read - value {}", characteristic.getUuid(),
                            address, HexUtils.bytesToHex(data));
                    future.complete(data);
                } catch (InterruptedException e) {
                    future.completeExceptionally(e);
                    return;// we don't want to schedule anything if we receive an interrupt
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    cause = cause != null ? cause : e;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause.getMessage());
                    future.completeExceptionally(cause);
                } catch (BluetoothException e) {
                    future.completeExceptionally(e);
                } catch (TimeoutException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
                getConnectionTaskExecutor().submit(() -> {
                    if (device.getConnectionState() != ConnectionState.CONNECTED) {
                        if (!device.connect()) {
                            logger.debug("Error connecting to device after discovery.");
                        }
                    }
                });
                break;
            case CONNECTED:
                updateStatus(ThingStatus.ONLINE);
                getConnectionTaskExecutor().submit(() -> {
                    if (!device.isServicesDiscovered() && !device.discoverServices()) {
                        logger.debug("Error while discovering services");
                    }
                });
                break;
            case DISCONNECTED:
                if (!connectOnDemand) {
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
}
