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
package org.openhab.binding.bluetooth.grundfosalpha.internal.handler;

import static org.openhab.binding.bluetooth.grundfosalpha.internal.GrundfosAlphaBindingConstants.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.ConnectedBluetoothHandler;
import org.openhab.binding.bluetooth.grundfosalpha.internal.CharacteristicRequest;
import org.openhab.binding.bluetooth.grundfosalpha.internal.protocol.MessageType;
import org.openhab.binding.bluetooth.grundfosalpha.internal.protocol.ResponseMessage;
import org.openhab.binding.bluetooth.grundfosalpha.internal.protocol.SensorDataType;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GrundfosAlpha3Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class GrundfosAlpha3Handler extends ConnectedBluetoothHandler {

    private static final UUID UUID_SERVICE_GENI = UUID.fromString("0000fe5d-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHARACTERISTIC_GENI = UUID.fromString("859cffd1-036e-432a-aa28-1a0085b87ba9");

    private static final Set<String> FLOW_HEAD_CHANNELS = Set.of(CHANNEL_FLOW_RATE, CHANNEL_PUMP_HEAD);
    private static final Set<String> POWER_CHANNELS = Set.of(CHANNEL_VOLTAGE_AC, CHANNEL_POWER, CHANNEL_MOTOR_SPEED);

    private static final int DEFAULT_REFRESH_INTERVAL_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(GrundfosAlpha3Handler.class);
    private final Lock stateLock = new ReentrantLock();
    private final BlockingQueue<CharacteristicRequest> sendQueue = new LinkedBlockingQueue<>();

    private @Nullable ScheduledFuture<?> refreshFuture;
    private @Nullable WriteCharacteristicThread senderThread;
    private ResponseMessage responseMessage = new ResponseMessage();

    public GrundfosAlpha3Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        WriteCharacteristicThread senderThread = this.senderThread = new WriteCharacteristicThread(device);
        senderThread.start();
    }

    @Override
    public void dispose() {
        WriteCharacteristicThread senderThread = this.senderThread;
        if (senderThread != null) {
            senderThread.interrupt();
            this.senderThread = null;
        }
        cancelFuture();
        super.dispose();
    }

    private void scheduleFuture() {
        cancelFuture();

        Object refreshIntervalRaw = getConfig().get(CONFIGURATION_REFRESH_INTERVAL);
        int refreshInterval = DEFAULT_REFRESH_INTERVAL_SECONDS;
        if (refreshIntervalRaw instanceof Number number) {
            refreshInterval = number.intValue();
        }

        refreshFuture = scheduler.scheduleWithFixedDelay(this::refreshChannels, 0, refreshInterval, TimeUnit.SECONDS);
    }

    private void cancelFuture() {
        ScheduledFuture<?> refreshFuture = this.refreshFuture;
        if (refreshFuture != null) {
            refreshFuture.cancel(true);
            this.refreshFuture = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (command != RefreshType.REFRESH) {
            // Currently no writable channels
            return;
        }

        if (device.getConnectionState() != ConnectionState.CONNECTED) {
            logger.info("Cannot send command {} because device is not connected", command);
            return;
        }

        if (FLOW_HEAD_CHANNELS.contains(channelUID.getId())) {
            sendQueue.add(new CharacteristicRequest(UUID_CHARACTERISTIC_GENI, MessageType.FlowHead));
        } else if (POWER_CHANNELS.contains(channelUID.getId())) {
            sendQueue.add(new CharacteristicRequest(UUID_CHARACTERISTIC_GENI, MessageType.Power));
        }
    }

    @Override
    public void onServicesDiscovered() {
        logger.debug("onServicesDiscovered");
        super.onServicesDiscovered();

        for (BluetoothService service : device.getServices()) {
            logger.debug("Supported service for {}: {}", device.getName(), service.getUuid());
            if (UUID_SERVICE_GENI.equals(service.getUuid())) {
                List<BluetoothCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothCharacteristic characteristic : characteristics) {
                    if (UUID_CHARACTERISTIC_GENI.equals(characteristic.getUuid())) {
                        logger.debug("Characteristic {} found for service {}", UUID_CHARACTERISTIC_GENI,
                                UUID_SERVICE_GENI);
                        device.enableNotifications(characteristic);
                        String deviceName = device.getName();
                        if (deviceName != null) {
                            updateProperty(Thing.PROPERTY_MODEL_ID, deviceName);
                        }
                        updateStatus(ThingStatus.ONLINE);
                        scheduleFuture();
                    }
                }
            }
        }
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        logger.debug("onScanRecordReceived");

        // Avoid calling super method when it would set Thing status to online.
        // Instead, we set the status in onServicesDiscovered when we have discovered
        // the GENI service and characteristic, which means we have a pairing connection.
        if (Integer.MIN_VALUE != scanNotification.getRssi()) {
            super.onScanRecordReceived(scanNotification);
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] value) {
        super.onCharacteristicUpdate(characteristic, value);

        stateLock.lock();
        try {
            if (!UUID_CHARACTERISTIC_GENI.equals(characteristic.getUuid())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Received update {} to unknown characteristic {} of device {}",
                            HexUtils.bytesToHex(value), characteristic.getUuid(), address);
                }
                return;
            }

            if (responseMessage.addPacket(value)) {
                Map<SensorDataType, BigDecimal> values = responseMessage.decode();
                updateChannels(values);
                responseMessage = new ResponseMessage();
            }
        } finally {
            stateLock.unlock();
        }
    }

    private void refreshChannels() {
        if (device.getConnectionState() != ConnectionState.CONNECTED) {
            return;
        }

        if (FLOW_HEAD_CHANNELS.stream().anyMatch(this::isLinked)) {
            sendQueue.add(new CharacteristicRequest(UUID_CHARACTERISTIC_GENI, MessageType.FlowHead));
        }

        if (POWER_CHANNELS.stream().anyMatch(this::isLinked)) {
            sendQueue.add(new CharacteristicRequest(UUID_CHARACTERISTIC_GENI, MessageType.Power));
        }
    }

    private void updateChannels(Map<SensorDataType, BigDecimal> values) {
        for (Entry<SensorDataType, BigDecimal> entry : values.entrySet()) {
            BigDecimal stateValue = entry.getValue();
            switch (entry.getKey()) {
                case Flow -> updateState(CHANNEL_FLOW_RATE, new QuantityType<>(stateValue, Units.CUBICMETRE_PER_HOUR));
                case Head -> updateState(CHANNEL_PUMP_HEAD, new QuantityType<>(stateValue, SIUnits.METRE));
                case VoltageAC -> updateState(CHANNEL_VOLTAGE_AC, new QuantityType<>(stateValue, Units.VOLT));
                case PowerConsumption -> updateState(CHANNEL_POWER, new QuantityType<>(stateValue, Units.WATT));
                case MotorSpeed -> updateState(CHANNEL_MOTOR_SPEED, new QuantityType<>(stateValue, Units.RPM));
            }
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        super.onConnectionStateChange(connectionNotification);
        logger.debug("{}", connectionNotification.getConnectionState());
    }

    private class WriteCharacteristicThread extends Thread {
        private static final int REQUEST_DELAY_MS = 700;

        private final BluetoothDevice device;

        public WriteCharacteristicThread(BluetoothDevice device) {
            super("OH-binding-" + getThing().getUID() + "-WriteCharacteristicThread");
            this.device = device;
        }

        @Override
        public void run() {
            logger.debug("Starting sender thread");
            while (!interrupted()) {
                try {
                    processQueue();
                    Thread.sleep(REQUEST_DELAY_MS);
                } catch (InterruptedException e) {
                    break;
                }
            }
            logger.debug("Sender thread finished");
        }

        private void processQueue() throws InterruptedException {
            logger.trace("Processing/await queue, size: {}", sendQueue.size());
            CharacteristicRequest request = sendQueue.take();
            if (logger.isDebugEnabled()) {
                logger.debug("Writing characteristic {}: {}", request.getUUID(),
                        HexUtils.bytesToHex(request.getValue()));
            }
            if (request.send(device)) {
                removeDuplicates(request);
            }
        }

        private void removeDuplicates(CharacteristicRequest request) {
            int duplicates = 0;
            while (sendQueue.remove(request)) {
                duplicates++;
            }
            if (duplicates > 0 && logger.isDebugEnabled()) {
                logger.debug("Removed {} duplicate characteristic requests for '{}' from queue", duplicates, request);
            }
        }
    }
}
