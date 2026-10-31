/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.ConnectedBluetoothHandler;
import org.openhab.binding.bluetooth.grundfosalpha.internal.protocol.GeniMeasurand;
import org.openhab.binding.bluetooth.grundfosalpha.internal.protocol.GeniReadRequest;
import org.openhab.binding.bluetooth.grundfosalpha.internal.protocol.GeniSession;
import org.openhab.binding.bluetooth.grundfosalpha.internal.protocol.ModeSetting;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
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
    private static final Set<String> MOTOR_CHANNELS = Set.of(CHANNEL_VOLTAGE_AC, CHANNEL_POWER, CHANNEL_MOTOR_SPEED,
            CHANNEL_MOTOR_CURRENT);
    private static final Set<String> COUNTER_CHANNELS = Set.of(CHANNEL_OPERATING_TIME, CHANNEL_START_COUNT);

    private static final int DEFAULT_REFRESH_INTERVAL_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(GrundfosAlpha3Handler.class);
    private final ScheduledExecutorService requestScheduler;

    private volatile @Nullable GeniSession session;
    private volatile boolean disposed = true;
    private volatile long sessionGeneration;

    private @Nullable ScheduledFuture<?> refreshFuture;

    public GrundfosAlpha3Handler(Thing thing) {
        this(thing, null);
    }

    GrundfosAlpha3Handler(Thing thing, @Nullable ScheduledExecutorService requestScheduler) {
        super(thing);
        this.requestScheduler = requestScheduler == null ? scheduler : requestScheduler;
    }

    @Override
    public synchronized void initialize() {
        long generation = ++sessionGeneration;
        session = new GeniSession(requestScheduler, this::writePacket, values -> updateChannels(generation, values),
                this::disconnect);
        disposed = false;
        super.initialize();
    }

    @Override
    public synchronized void dispose() {
        disposed = true;
        GeniSession session = this.session;
        if (session != null) {
            session.close();
            this.session = null;
        }
        cancelFuture();
        super.dispose();
    }

    private CompletionStage<?> writePacket(byte[] packet) {
        BluetoothDevice device = this.device;
        if (disposed || device == null || device.getConnectionState() != ConnectionState.CONNECTED) {
            return CompletableFuture.failedFuture(new IllegalStateException("Device is not connected"));
        }
        BluetoothCharacteristic characteristic = device.getCharacteristic(UUID_CHARACTERISTIC_GENI);
        if (characteristic == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("GENI characteristic is missing"));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Writing characteristic {}: {}", UUID_CHARACTERISTIC_GENI, HexUtils.bytesToHex(packet));
        }
        return device.writeCharacteristic(characteristic, packet);
    }

    private void scheduleFuture(GeniSession session) {
        cancelFuture();

        Object refreshIntervalRaw = getConfig().get(CONFIGURATION_REFRESH_INTERVAL);
        int refreshInterval = DEFAULT_REFRESH_INTERVAL_SECONDS;
        if (refreshIntervalRaw instanceof Number number) {
            refreshInterval = number.intValue();
        }

        refreshFuture = requestScheduler.scheduleWithFixedDelay(() -> refreshChannels(session), 0, refreshInterval,
                TimeUnit.SECONDS);
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
        GeniSession session;
        synchronized (this) {
            session = this.session;
            if (disposed || session == null) {
                return;
            }
            super.handleCommand(channelUID, command);
        }

        BluetoothDevice device = this.device;
        if (disposed || device == null || device.getConnectionState() != ConnectionState.CONNECTED) {
            logger.debug("Cannot send command {} because device is not connected", command);
            return;
        }

        ModeSetting setting = switch (channelUID.getId()) {
            case CHANNEL_OPERATING_MODE -> ModeSetting.OperatingMode;
            case CHANNEL_CONTROL_MODE -> ModeSetting.ControlMode;
            default -> null;
        };

        if (setting != null) {
            if (command == RefreshType.REFRESH) {
                session.read(setting.readRequest());
            } else if (command instanceof StringType) {
                setting.protocolValue(command.toString()).ifPresentOrElse(value -> session.write(setting, value),
                        () -> logger.debug("Ignoring unsupported {} value {}", setting, command));
            }
            return;
        }
        if (command != RefreshType.REFRESH) {
            return;
        }

        if (FLOW_HEAD_CHANNELS.contains(channelUID.getId())) {
            session.read(GeniReadRequest.FlowHead);
        } else if (MOTOR_CHANNELS.contains(channelUID.getId())) {
            session.read(GeniReadRequest.Motor);
        } else if (CHANNEL_ENERGY.equals(channelUID.getId())) {
            session.read(GeniReadRequest.Energy);
        } else if (COUNTER_CHANNELS.contains(channelUID.getId())) {
            session.read(GeniReadRequest.Counters);
        }
    }

    @Override
    public synchronized void onServicesDiscovered() {
        logger.debug("onServicesDiscovered");
        GeniSession session = this.session;
        BluetoothDevice device = this.device;
        if (disposed || session == null || device == null || device.getConnectionState() != ConnectionState.CONNECTED) {
            return;
        }
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
                        session.start();
                        scheduleFuture(session);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        if (disposed) {
            return;
        }
        logger.debug("onScanRecordReceived");

        // Avoid calling super method when it would set Thing status to online.
        // Instead, we set the status in onServicesDiscovered when we have discovered
        // the GENI service and characteristic, which means we have a pairing connection.
        if (Integer.MIN_VALUE != scanNotification.getRssi()) {
            super.onScanRecordReceived(scanNotification);
        }
    }

    @Override
    public synchronized void onAdapterChanged(BluetoothAdapter adapter) {
        if (!disposed) {
            super.onAdapterChanged(adapter);
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] value) {
        GeniSession session = this.session;
        super.onCharacteristicUpdate(characteristic, value);

        if (!disposed && session != null && UUID_CHARACTERISTIC_GENI.equals(characteristic.getUuid())) {
            session.accept(value);
        }
    }

    private void refreshChannels(GeniSession session) {
        BluetoothDevice device = this.device;
        if (disposed || !session.equals(this.session) || device == null
                || device.getConnectionState() != ConnectionState.CONNECTED) {
            return;
        }

        if (FLOW_HEAD_CHANNELS.stream().anyMatch(this::isLinked)) {
            session.read(GeniReadRequest.FlowHead);
        }

        if (MOTOR_CHANNELS.stream().anyMatch(this::isLinked)) {
            session.read(GeniReadRequest.Motor);
        }

        if (isLinked(CHANNEL_ENERGY)) {
            session.read(GeniReadRequest.Energy);
        }

        if (COUNTER_CHANNELS.stream().anyMatch(this::isLinked)) {
            session.read(GeniReadRequest.Counters);
        }

        if (isLinked(CHANNEL_OPERATING_MODE)) {
            session.read(GeniReadRequest.OperatingMode);
        }

        if (isLinked(CHANNEL_CONTROL_MODE)) {
            session.read(GeniReadRequest.ControlMode);
        }
    }

    private void updateChannels(long generation, Map<GeniMeasurand, BigDecimal> values) {
        for (Entry<GeniMeasurand, BigDecimal> entry : values.entrySet()) {
            // A state callback can dispose and reinitialize the handler before the remaining values are published.
            if (disposed || generation != sessionGeneration) {
                return;
            }
            BigDecimal stateValue = entry.getValue();
            QuantityType<?> quantity = new QuantityType<>(stateValue, entry.getKey().unit());
            switch (entry.getKey()) {
                case Flow -> updateState(CHANNEL_FLOW_RATE, quantity);
                case Head -> updateState(CHANNEL_PUMP_HEAD, quantity);
                case VoltageAC -> updateState(CHANNEL_VOLTAGE_AC, quantity);
                case MotorCurrent -> updateState(CHANNEL_MOTOR_CURRENT, quantity);
                case MotorSpeed -> updateState(CHANNEL_MOTOR_SPEED, quantity);
                case PowerConsumption -> updateState(CHANNEL_POWER, quantity);
                case Energy -> updateState(CHANNEL_ENERGY, quantity);
                case OperatingTime -> updateState(CHANNEL_OPERATING_TIME, quantity);
                case StartCount -> updateState(CHANNEL_START_COUNT, new DecimalType(stateValue));
                case OperatingMode -> {
                    String option = ModeSetting.OperatingMode.option(stateValue.intValue()).orElse(null);
                    updateState(CHANNEL_OPERATING_MODE, option != null ? new StringType(option) : UnDefType.UNDEF);
                }
                case ControlMode -> {
                    String option = ModeSetting.ControlMode.option(stateValue.intValue()).orElse(null);
                    updateState(CHANNEL_CONTROL_MODE, option != null ? new StringType(option) : UnDefType.UNDEF);
                }
            }
        }
    }

    @Override
    public synchronized void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        GeniSession session = this.session;
        if (disposed || session == null) {
            return;
        }
        if (connectionNotification.getConnectionState() != ConnectionState.CONNECTED) {
            session.stop();
            cancelFuture();
        }
        super.onConnectionStateChange(connectionNotification);
        logger.debug("{}", connectionNotification.getConnectionState());
        BluetoothDevice device = this.device;
        if (connectionNotification.getConnectionState() == ConnectionState.CONNECTED && device != null
                && device.isServicesDiscovered()) {
            onServicesDiscovered();
        }
    }
}
